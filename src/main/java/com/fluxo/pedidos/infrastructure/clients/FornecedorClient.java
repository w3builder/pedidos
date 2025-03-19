package com.fluxo.pedidos.infrastructure.clients;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fluxo.pedidos.infrastructure.camel.mock.dto.fornecedor.PedidoFornecedorRequest;
import com.fluxo.pedidos.presentation.dto.response.RespostaProcessamentoPedidoDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FornecedorClient {

    @Value("${camel.mock.fornecedor.url}")
    private String urlFornecedor;
    
    private final ProducerTemplate producerTemplate;

    public RespostaProcessamentoPedidoDTO enviarPedidoFornecedor(PedidoFornecedorRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            ResponseEntity<Object> camelResponse = restTemplate.postForEntity(
                    urlFornecedor, 
                    request, 
                    Object.class);
            
            return new RespostaProcessamentoPedidoDTO(
                camelResponse.getBody(),
                camelResponse.getStatusCode()
            );
        } catch (HttpClientErrorException e) {
            // Erros controlados do servidor (400, 404, etc)
            return new RespostaProcessamentoPedidoDTO(
                e.getResponseBodyAsString(),
                e.getStatusCode()
            );  
        } catch (Exception e) {
            // Erro de conexão ou outro erro grave - enviar para fila de reprocessamento
            log.error("Erro ao processar pedido: {}. Enviando para fila de reprocessamento", e.getMessage());
            
            try {
                // Enviar para o endpoint do Camel que salvará na fila RabbitMQ
                producerTemplate.sendBody("direct:salvarPedidoPendente", request);
                
                return new RespostaProcessamentoPedidoDTO(
                    "Pedido enviado para processamento assíncrono devido a erro: " + e.getMessage(),
                    HttpStatus.ACCEPTED  // 202 - Indica que foi aceito para processamento posterior
                );
            } catch (Exception ex) {
                log.error("Erro ao enviar pedido para fila: {}", ex.getMessage());
                return new RespostaProcessamentoPedidoDTO(
                    "Erro crítico ao processar pedido: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        }
    }
}
