package com.fluxo.pedidos.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fluxo.pedidos.camel.mock.dto.fornecedor.PedidoFornecedorRequest;
import com.fluxo.pedidos.dto.response.RespostaProcessamentoPedidoDTO;

@Component
public class FornecedorClient {

    @Value("${camel.mock.fornecedor.url}")
    private String urlFornecedor;

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
            return new RespostaProcessamentoPedidoDTO(
                e.getResponseBodyAsString(),
                e.getStatusCode()
            );  
        } catch (Exception e) {
            return new RespostaProcessamentoPedidoDTO(
                "Erro ao processar pedido: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
