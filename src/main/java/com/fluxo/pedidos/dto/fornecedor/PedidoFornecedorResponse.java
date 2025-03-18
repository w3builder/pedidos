package com.fluxo.pedidos.dto.fornecedor;

import java.util.List;

public class PedidoFornecedorResponse {
    private String numeroPedido;
    private List<ItemPedidoFornecedor> itensConfirmados;
    private String status;
    
    // Getters e Setters
    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public List<ItemPedidoFornecedor> getItensConfirmados() {
        return itensConfirmados;
    }

    public void setItensConfirmados(List<ItemPedidoFornecedor> itensConfirmados) {
        this.itensConfirmados = itensConfirmados;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 