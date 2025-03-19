package com.fluxo.pedidos.camel.config;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "!test")
public class CamelServletConfig {

    @Bean
    public ServletRegistrationBean<CamelHttpTransportServlet> camelServlet() {
        CamelHttpTransportServlet servlet = new CamelHttpTransportServlet();
        ServletRegistrationBean<CamelHttpTransportServlet> registration = 
            new ServletRegistrationBean<>();
        registration.setServlet(servlet);
        registration.addUrlMappings("/camel/*");
        registration.setName("CamelServlet");
        return registration;
    }
} 