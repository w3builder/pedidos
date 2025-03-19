package com.fluxo.pedidos.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fluxo.pedidos.infrastructure.persistence.entity.Pedido;
import com.fluxo.pedidos.infrastructure.persistence.entity.Revenda;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    List<Pedido> findByRevenda(Revenda revenda);
    
    Optional<Pedido> findByNumero(String numero);
    
    List<Pedido> findByRevendaId(Long revendaId);
    
    List<Pedido> findByClienteId(Long clienteId);
} 