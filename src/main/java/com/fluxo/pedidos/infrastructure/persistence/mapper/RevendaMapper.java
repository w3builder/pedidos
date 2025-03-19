package com.fluxo.pedidos.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.fluxo.pedidos.presentation.dto.request.RevendaDTO;
import com.fluxo.pedidos.presentation.dto.response.RevendaResponseDTO;
import com.fluxo.pedidos.infrastructure.persistence.entity.Revenda; 

@Mapper(componentModel = "spring", uses = {TelefoneMapper.class, ContatoMapper.class, EnderecoMapper.class})
public interface RevendaMapper {
    
    @Mapping(target = "id", ignore = true)
    Revenda toEntity(RevendaDTO dto);
    
    RevendaResponseDTO toResponseDTO(Revenda entity);

    @Mapping(target = "id", ignore = true)
    Revenda updateEntity(@MappingTarget Revenda entity, RevendaDTO dto);
} 