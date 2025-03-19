package com.fluxo.pedidos.infrastructure.camel.mock;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fluxo.pedidos.infrastructure.camel.mock.dto.fornecedor.PedidoFornecedorRequest;
import com.fluxo.pedidos.infrastructure.camel.mock.dto.fornecedor.PedidoFornecedorResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(name = "camel.springboot.auto-startup", havingValue = "true", matchIfMissing = true)
public class RotaProcessamentoPedidoMock extends RouteBuilder {
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${fornecedor.pedido.quantidade.minima:1000}")
    private int quantidadeMinima;
    
    @Value("${fornecedor.revenda.codigo.valido:REV001}")
    private String codigoRevendaValido;
    
    @Value("${camel.rabbitmq.pedidos-pendentes-queue}")
    private String filaPedidosPendentes;
    
    @Value("${camel.rabbitmq.pedidos-processamento-exchange}")
    private String exchangePedidos;
    
    @Value("${camel.rabbitmq.pedidos-processamento-max-tentativas}")
    private int maxTentativas;
    
    @Value("${camel.rabbitmq.pedidos-processamento-dead-letter-queue}")
    private String filaDeadLetter;

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
                .to("direct:processarPedidoFornecedor");
        
        // Configurar Circuit Breaker para lidar com falhas transitórias
        from("direct:processarPedidoFornecedor")
            .routeId("processarPedidoFornecedor")
            .log(LoggingLevel.INFO, "Recebido pedido para processamento: ${body}")
            .circuitBreaker()
                .resilience4jConfiguration()
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(10000)
                    .slidingWindowSize(10)
                .end()
                .to("direct:processarPedidoInterno")
            .onFallback()
                .log(LoggingLevel.WARN, "Circuit breaker ativo ou falha no processamento, enviando para fila de reprocessamento")
                .to("direct:salvarPedidoPendente")
            .end();
        
        // Rota interna para processar o pedido
        from("direct:processarPedidoInterno")
            .routeId("processarPedidoInterno")
            // Validar condições do pedido
            .choice()
                // Se código da revenda for inválido
                .when(simple("${body.codigoRevenda} != '" + codigoRevendaValido + "'"))
                    .log(LoggingLevel.WARN, "Código de revenda inválido: ${body.codigoRevenda}")
                    .process(exchange -> {
                        PedidoFornecedorResponse response = new PedidoFornecedorResponse();
                        response.setStatus("REJEITADO");
                        response.setMensagem("Código de revenda inválido");
                        exchange.getMessage().setBody(response);
                    })
                    .setHeader("Content-Type", constant(MediaType.APPLICATION_JSON_VALUE))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                
                // Se algum item não atingir a quantidade mínima
                .when(simple("${body.itens.?[getQuantidade() < " + quantidadeMinima + "].size()} > 0"))
                    .log(LoggingLevel.WARN, "Um ou mais itens não atingem a quantidade mínima de " + quantidadeMinima)
                    .process(exchange -> {
                        PedidoFornecedorResponse response = new PedidoFornecedorResponse();
                        response.setStatus("REJEITADO");
                        response.setMensagem("Um ou mais itens não atingem a quantidade mínima");
                        exchange.getMessage().setBody(response);
                    })
                    .setHeader("Content-Type", constant(MediaType.APPLICATION_JSON_VALUE))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                
                // Caso contrário, processar normalmente
                .otherwise()
                    .log(LoggingLevel.INFO, "Processando pedido...")
                    .process(exchange -> {
                        PedidoFornecedorRequest request = exchange.getMessage().getBody(PedidoFornecedorRequest.class);
                        
                        // Simular processamento do pedido
                        PedidoFornecedorResponse response = new PedidoFornecedorResponse();
                        response.setNumeroPedido("PED-" + System.currentTimeMillis());
                        response.setStatus("PROCESSADO");
                        response.setMensagem("Pedido processado com sucesso");
                        response.setItensConfirmados(request.getItens());
                        
                        exchange.getMessage().setBody(response);
                    })
                    .setHeader("Content-Type", constant(MediaType.APPLICATION_JSON_VALUE))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
            .end();
            
        // Rota para salvar pedidos pendentes no RabbitMQ quando fornecedor está indisponível
        from("direct:salvarPedidoPendente")
            .routeId("salvarPedidoPendente")
            .log("Salvando pedido para processamento posterior via RabbitMQ: ${body}")
            .process(exchange -> {
                // Registrar tentativa para fins de rastreamento
                PedidoFornecedorRequest request = exchange.getMessage().getBody(PedidoFornecedorRequest.class);
                exchange.setProperty("tentativas", 0);
                log.info("Pedido da revenda {} com {} itens enviado para fila de reprocessamento", 
                      request.getCodigoRevenda(), request.getItens().size());
                
                // Importante: Manter o body como o objeto request original
                // Preparamos a resposta, mas não a colocamos como body ainda
                PedidoFornecedorResponse response = new PedidoFornecedorResponse();
                response.setStatus("PENDENTE");
                response.setMensagem("Pedido enviado para processamento assíncrono");
                exchange.setProperty("response", response);
            })
            .marshal().json() // Serializa o PedidoFornecedorRequest
            .to("rabbitmq://localhost:5672/"+exchangePedidos+"?queue="+filaPedidosPendentes+
                "&autoDelete=false&declare=true&durable=true")
            .process(exchange -> {
                // Agora, depois de enviar para o RabbitMQ, definimos a resposta para o cliente
                PedidoFornecedorResponse response = exchange.getProperty("response", PedidoFornecedorResponse.class);
                exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 202); // Accepted
                exchange.getMessage().setBody(response);
            });
        
        // Rota para consumir pedidos da fila e tentar reprocessar
        from("rabbitmq://localhost:5672/"+exchangePedidos+"?queue="+filaPedidosPendentes+
             "&autoDelete=false&declare=true&durable=true")
            .routeId("reprocessarPedidosPendentes")
            .log("Reprocessando pedido pendente da fila: ${body}")
            .unmarshal().json(PedidoFornecedorRequest.class)
            .process(exchange -> {
                // Adicionar contador de tentativas
                PedidoFornecedorRequest request = exchange.getMessage().getBody(PedidoFornecedorRequest.class);
                Integer tentativas = exchange.getProperty("CamelRedeliveryCounter", 0, Integer.class);
                log.info("Tentativa {} de processar pedido da revenda {}", 
                      tentativas + 1, request.getCodigoRevenda());
                exchange.setProperty("tentativas", tentativas + 1);
            })
            .doTry()
                .to("direct:processarPedidoInterno")
                .log("Reprocessamento bem-sucedido após ${exchangeProperty.tentativas} tentativas")
            .doCatch(Exception.class)
                .log(LoggingLevel.ERROR, "Erro no reprocessamento: ${exception.message}")
                .choice()
                    .when(simple("${exchangeProperty.tentativas} >= " + maxTentativas))
                        .log("Número máximo de tentativas excedido, enviando para Dead Letter Queue")
                        .to("rabbitmq://localhost:5672/"+exchangePedidos+"?queue="+filaDeadLetter+
                            "&autoDelete=false&declare=true&durable=true")
                    .otherwise()
                        .log("Programando nova tentativa de processamento")
                        .delay(5000) // Espera 5 segundos antes de reenviar
                        .to("rabbitmq://localhost:5672/"+exchangePedidos+"?queue="+filaPedidosPendentes+
                            "&autoDelete=false&declare=true&durable=true")
                .end()
            .end();
            
        // Rota para consumir pedidos da Dead Letter Queue (possível alerta/notificação)
        from("rabbitmq://localhost:5672/"+exchangePedidos+"?queue="+filaDeadLetter+
             "&autoDelete=false&declare=true&durable=true")
            .routeId("processarDeadLetterQueue")
            .log(LoggingLevel.ERROR, "Processando pedido da Dead Letter Queue: ${body}")
            .unmarshal().json(PedidoFornecedorRequest.class)
            .process(exchange -> {
                // Registrar falha permanente
                PedidoFornecedorRequest request = exchange.getMessage().getBody(PedidoFornecedorRequest.class);
                log.error("ALERTA: Pedido da revenda {} com {} itens falhou permanentemente", 
                        request.getCodigoRevenda(), 
                        request.getItens().size());
            });
    }
}
