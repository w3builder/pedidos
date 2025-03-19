package com.fluxo.pedidos.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fluxo.pedidos.presentation.dto.response.ItemPedidoResponseDTO;
import com.fluxo.pedidos.infrastructure.persistence.entity.ItemPedido;

@Mapper(componentModel = "spring", uses = {ProdutoMapper.class})
public interface ItemPedidoMapper {
    
    @Mapping(target = "produto", source = "produto")
    ItemPedidoResponseDTO toResponseDTO(ItemPedido entity);
} 