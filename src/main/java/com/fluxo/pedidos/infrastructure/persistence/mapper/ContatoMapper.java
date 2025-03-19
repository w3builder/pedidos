package com.fluxo.pedidos.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fluxo.pedidos.infrastructure.persistence.entity.Contato;
import com.fluxo.pedidos.presentation.dto.request.ContatoDTO;

@Mapper(componentModel = "spring")
public interface ContatoMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "revenda", ignore = true)
    Contato toEntity(ContatoDTO dto);
    
    ContatoDTO toDTO(Contato entity);
} 