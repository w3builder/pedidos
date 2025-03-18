package com.fluxo.pedidos.dto.fornecedor;

public class ItemPedidoFornecedor {
    private String codigoProduto;
    private Integer quantidade;
    
    // Getters e Setters
    public String getCodigoProduto() {
        return codigoProduto;
    }

    public void setCodigoProduto(String codigoProduto) {
        this.codigoProduto = codigoProduto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
} 