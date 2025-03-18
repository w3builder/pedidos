package com.fluxo.pedidos.repository;

import com.fluxo.pedidos.entity.PedidoFornecedorPendente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoFornecedorPendenteRepository extends JpaRepository<PedidoFornecedorPendente, Long> {
    List<PedidoFornecedorPendente> findByStatusOrderByUltimaTentativaAsc(String status);
} 