package com.fluxo.pedidos.camel.mock.dto.fornecedor;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoFornecedor {
    private String codigoProduto;
    private int quantidade;
    private BigDecimal precoUnitario;
} 