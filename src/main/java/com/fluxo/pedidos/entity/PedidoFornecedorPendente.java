package com.fluxo.pedidos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos_fornecedor_pendentes")
public class PedidoFornecedorPendente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long pedidoId;
    
    @Column(nullable = false)
    private LocalDateTime dataCriacao;
    
    @Column(nullable = false)
    private LocalDateTime ultimaTentativa;
    
    @Column(nullable = false)
    private Integer numeroTentativas;
    
    @Column(length = 20)
    private String status;
    
    // Constructor, Getters e Setters
    public PedidoFornecedorPendente() {
        this.dataCriacao = LocalDateTime.now();
        this.ultimaTentativa = LocalDateTime.now();
        this.numeroTentativas = 0;
        this.status = "PENDENTE";
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getUltimaTentativa() {
        return ultimaTentativa;
    }

    public void setUltimaTentativa(LocalDateTime ultimaTentativa) {
        this.ultimaTentativa = ultimaTentativa;
    }

    public Integer getNumeroTentativas() {
        return numeroTentativas;
    }

    public void setNumeroTentativas(Integer numeroTentativas) {
        this.numeroTentativas = numeroTentativas;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 