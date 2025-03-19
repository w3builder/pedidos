package com.fluxo.pedidos.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fluxo.pedidos.infrastructure.persistence.entity.Endereco;
import com.fluxo.pedidos.presentation.dto.request.EnderecoDTO;

@Mapper(componentModel = "spring")
public interface EnderecoMapper {
    
    @Mapping(target = "id", ignore = true)
    Endereco toEntity(EnderecoDTO dto);
    
    EnderecoDTO toDTO(Endereco entity);
} 