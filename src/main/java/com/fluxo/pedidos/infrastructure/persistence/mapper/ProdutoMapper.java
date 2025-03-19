package com.fluxo.pedidos.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fluxo.pedidos.presentation.dto.response.ProdutoResponseDTO;
import com.fluxo.pedidos.infrastructure.persistence.entity.Produto;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {
    
    @Mapping(target = "id", source = "id")
    ProdutoResponseDTO toResponseDTO(Produto entity);
} 