package com.fluxo.pedidos.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fluxo.pedidos.presentation.dto.response.PedidoResponseDTO;
import com.fluxo.pedidos.infrastructure.persistence.entity.Pedido;

@Mapper(componentModel = "spring", uses = {ClienteMapper.class, ItemPedidoMapper.class})
public interface PedidoMapper {
    
    @Mapping(target = "cliente", source = "cliente")
    @Mapping(target = "itens", source = "itens")
    PedidoResponseDTO toResponseDTO(Pedido entity);
} 