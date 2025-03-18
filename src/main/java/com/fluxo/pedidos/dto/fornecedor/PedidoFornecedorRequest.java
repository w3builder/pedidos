package com.fluxo.pedidos.dto.fornecedor;

import java.util.List;

public class PedidoFornecedorRequest {
    private Long revendaId;
    private String codigoRevenda;
    private List<ItemPedidoFornecedor> itens;
    
    // Getters e Setters
    public Long getRevendaId() {
        return revendaId;
    }

    public void setRevendaId(Long revendaId) {
        this.revendaId = revendaId;
    }

    public String getCodigoRevenda() {
        return codigoRevenda;
    }

    public void setCodigoRevenda(String codigoRevenda) {
        this.codigoRevenda = codigoRevenda;
    }

    public List<ItemPedidoFornecedor> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoFornecedor> itens) {
        this.itens = itens;
    }
} 