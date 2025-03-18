package com.fluxo.pedidos.dto.fornecedor;

public class ItemPedidoFornecedor {
    private Long produtoId;
    private Integer quantidade;
    
    // Getters and setters
    public Long getProdutoId() {
        return produtoId;
    }
    
    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }
    
    public Integer getQuantidade() {
        return quantidade;
    }
    
    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
} 