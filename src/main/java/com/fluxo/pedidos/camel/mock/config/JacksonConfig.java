package com.fluxo.pedidos.camel.mock.config;

import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluxo.pedidos.camel.mock.dto.fornecedor.PedidoFornecedorRequest;

@Configuration
@ConditionalOnProperty(name = "camel.springboot.enabled", havingValue = "true", matchIfMissing = true)
public class JacksonConfig {

    @Bean("pedidoFornecedorJacksonFormat")
    public JacksonDataFormat pedidoFornecedorJacksonFormat() {
        // Criar o ObjectMapper explicitamente primeiro
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Passar o ObjectMapper já configurado para o JacksonDataFormat
        JacksonDataFormat format = new JacksonDataFormat();
        format.setObjectMapper(objectMapper);
        format.setUnmarshalType(PedidoFornecedorRequest.class);
        
        return format;
    }
} 