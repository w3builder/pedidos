package com.fluxo.pedidos.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fluxo.pedidos.infrastructure.persistence.entity.Telefone;
import com.fluxo.pedidos.presentation.dto.request.TelefoneDTO;

@Mapper(componentModel = "spring")
public interface TelefoneMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "revenda", ignore = true)
    Telefone toEntity(TelefoneDTO dto);
    
    TelefoneDTO toDTO(Telefone entity);
} 