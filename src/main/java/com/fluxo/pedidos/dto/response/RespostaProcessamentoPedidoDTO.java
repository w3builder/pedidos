package com.fluxo.pedidos.dto.response;

import org.springframework.http.HttpStatusCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespostaProcessamentoPedidoDTO {
    private Object resposta;
    private HttpStatusCode status;
} 