package com.fluxo.pedidos.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fluxo.pedidos.infrastructure.persistence.entity.Produto;
import com.fluxo.pedidos.infrastructure.persistence.entity.Revenda;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    
    List<Produto> findByRevenda(Revenda revenda);
    
    Optional<Produto> findByCodigo(String codigo);
    
    List<Produto> findByRevendaId(Long revendaId);
} 