package com.fluxo.pedidos.camel.mock;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fluxo.pedidos.camel.mock.dto.fornecedor.ItemPedidoFornecedor;
import com.fluxo.pedidos.camel.mock.dto.fornecedor.PedidoFornecedorRequest;
import com.fluxo.pedidos.camel.mock.dto.fornecedor.PedidoFornecedorResponse;

@Component
public class RotaProcessamentoPedidoMock extends RouteBuilder {
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${fornecedor.pedido.quantidade.minima:1000}")
    private int quantidadeMinima;
    
    @Value("${fornecedor.revenda.codigo.valido:REV001}")
    private String codigoRevendaValido;
    
    @Override
    public void configure() throws Exception {
        // Tratamento de erros global
        errorHandler(defaultErrorHandler()
            .maximumRedeliveries(3)
            .redeliveryDelay(1000)
            .logRetryAttempted(true)
            .retryAttemptedLogLevel(LoggingLevel.WARN));
            
        // Configuração do componente REST (simplificada, sem OpenAPI)
        restConfiguration()
            .component("servlet")
            .bindingMode(RestBindingMode.json)
            .dataFormatProperty("prettyPrint", "true")
            .contextPath("/camel");
        
        // Definir o endpoint REST
        rest("/mock/fornecedor")
            .description("API de integração com fornecedor")
            
            .post("/pedidos")
                .description("Enviar pedido para o fornecedor")
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .type(PedidoFornecedorRequest.class)
                .outType(PedidoFornecedorResponse.class)
                .to("direct:validarPedidoFornecedor");
        
        // Validação inicial do pedido
        from("direct:validarPedidoFornecedor")
            .routeId("validarPedidoFornecedor")
            .log("Validando pedido recebido: ${body}")
            .choice()
                // Verificar se o código da revenda é válido
                .when(simple("${body.codigoRevenda} != '" + codigoRevendaValido + "'"))
                    .setHeader("Content-Type", constant(MediaType.APPLICATION_JSON_VALUE))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                    .setBody(constant("{\"erro\": \"Revenda não autorizada. Apenas a revenda " 
                            + codigoRevendaValido + " pode enviar pedidos.\"}"))
                // Verificar quantidade mínima
                .when(simple("${body.itens.size()} > 0"))
                    .process(exchange -> {
                        PedidoFornecedorRequest request = exchange.getMessage().getBody(PedidoFornecedorRequest.class);
                        int quantidadeTotal = request.getItens().stream()
                                .mapToInt(ItemPedidoFornecedor::getQuantidade)
                                .sum();
                        exchange.setProperty("quantidadeTotal", quantidadeTotal);
                    })
                    .choice()
                        .when(simple("${exchangeProperty.quantidadeTotal} < " + quantidadeMinima))
                            .setHeader("Content-Type", constant(MediaType.APPLICATION_JSON_VALUE))
                            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                            .setBody(constant("{\"erro\": \"Quantidade mínima não atingida. O pedido deve ter no mínimo " 
                                    + quantidadeMinima + " unidades no total.\"}"))
                        .otherwise()
                            .to("direct:processarPedidoFornecedor")
                    .endChoice()
                .otherwise()
                    .setHeader("Content-Type", constant(MediaType.APPLICATION_JSON_VALUE))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                    .setBody(constant("{\"erro\": \"Pedido deve conter pelo menos um item.\"}"))
            .end();
                
        // Processamento normal do pedido quando o fornecedor está disponível
        from("direct:processarPedidoFornecedor")
            .routeId("processarPedidoFornecedor")
            .log("Processando pedido para fornecedor: ${body}")
            // Simular falha aleatória (30% de chance)
            .choice()
                .when(simple("${random(10)} < 3"))
                    .log(LoggingLevel.ERROR, "Fornecedor temporariamente indisponível!")
                    .to("direct:salvarPedidoPendente")
                    .setHeader("Content-Type", constant(MediaType.APPLICATION_JSON_VALUE))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(202))
                    .setBody(constant("{\"status\": \"PENDENTE\", \"mensagem\": \"Pedido recebido e será processado quando o fornecedor estiver disponível.\"}"))
                .otherwise()
                    // Simular processamento e criação de resposta
                    .process(exchange -> {
                        PedidoFornecedorRequest request = exchange.getMessage().getBody(PedidoFornecedorRequest.class);
                        
                        // Criar resposta com número de pedido e itens confirmados
                        PedidoFornecedorResponse response = new PedidoFornecedorResponse();
                        response.setNumeroPedido("FORN-" + System.currentTimeMillis());
                        response.setStatus("CONFIRMADO");
                        // Mantém os mesmos itens da solicitação na resposta (itens confirmados)
                        response.setItensConfirmados(request.getItens());
                        
                        exchange.getMessage().setBody(response);
                    })
                    .setHeader("Content-Type", constant(MediaType.APPLICATION_JSON_VALUE))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
            .end();
            
        // Rota para salvar pedidos pendentes quando fornecedor está indisponível
        from("direct:salvarPedidoPendente")
            .routeId("salvarPedidoPendente")
            .log("Salvando pedido para processamento posterior: ${body}")
            // Em uma implementação real, salvaria em um banco de dados ou fila
            // Aqui apenas logamos para demonstração
            .process(exchange -> {
                PedidoFornecedorRequest request = exchange.getMessage().getBody(PedidoFornecedorRequest.class);
                log.info("Pedido pendente salvo: Revenda {} com {} itens", 
                        request.getCodigoRevenda(), 
                        request.getItens().size());
            });
    }
}
