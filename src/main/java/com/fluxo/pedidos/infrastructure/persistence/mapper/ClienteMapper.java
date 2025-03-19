package com.fluxo.pedidos.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fluxo.pedidos.presentation.dto.response.ClienteResponseDTO;
import com.fluxo.pedidos.infrastructure.persistence.entity.Cliente;

@Mapper(componentModel = "spring")
public interface ClienteMapper {
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "revendaId", source = "revenda.id")
    ClienteResponseDTO toResponseDTO(Cliente entity);
} 