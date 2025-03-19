package com.fluxo.pedidos.domain.service;

import java.util.List;

import com.fluxo.pedidos.domain.enums.StatusPedido;
import com.fluxo.pedidos.presentation.dto.request.PedidoDTO;
import com.fluxo.pedidos.presentation.dto.response.PedidoResponseDTO;
import com.fluxo.pedidos.presentation.dto.response.RespostaProcessamentoPedidoDTO;

public interface PedidoService {
    
    PedidoResponseDTO criarPedido(Long revendaId, PedidoDTO pedidoDTO);
    
    PedidoResponseDTO buscarPorId(Long id);
    
    PedidoResponseDTO buscarPorNumero(String numero);

    List<PedidoResponseDTO> listarPorRevenda(Long revendaId);
    
    List<PedidoResponseDTO> listarPorCliente(Long clienteId);
    
    PedidoResponseDTO cancelarPedido(Long id);
    
    PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedido status);
    
    RespostaProcessamentoPedidoDTO processarPedidoFornecedor(Long pedidoId, Long revendaId);
} 