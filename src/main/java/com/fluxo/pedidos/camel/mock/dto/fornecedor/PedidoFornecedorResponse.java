package com.fluxo.pedidos.camel.mock.dto.fornecedor;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoFornecedorResponse {
    private String numeroPedido;
    private String status;
    private List<ItemPedidoFornecedor> itensConfirmados;
} 