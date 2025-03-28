package com.fluxo.pedidos.domain.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fluxo.pedidos.application.exception.BusinessException;
import com.fluxo.pedidos.application.exception.ResourceNotFoundException;
import com.fluxo.pedidos.domain.service.RevendaService;
import com.fluxo.pedidos.presentation.dto.request.ContatoDTO;
import com.fluxo.pedidos.presentation.dto.request.RevendaDTO;
import com.fluxo.pedidos.presentation.dto.response.RevendaResponseDTO;
import com.fluxo.pedidos.infrastructure.persistence.entity.Revenda;
import com.fluxo.pedidos.infrastructure.persistence.mapper.RevendaMapper;
import com.fluxo.pedidos.infrastructure.persistence.repository.RevendaRepository;
import com.fluxo.pedidos.presentation.validator.CNPJValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RevendaServiceImpl implements RevendaService {
    
    private final RevendaRepository revendaRepository;
    private final RevendaMapper revendaMapper;
    private final CNPJValidator cnpjValidator;
    
    @Override
    @Transactional
    public RevendaResponseDTO criarRevenda(RevendaDTO revendaDTO) {

        validarRevenda(revendaDTO);
        
        Revenda revenda = revendaMapper.toEntity(revendaDTO);
        
        if (revenda.getTelefones() != null) {
            revenda.getTelefones().forEach(telefone -> telefone.setRevenda(revenda));
        }
        
        if (revenda.getContatos() != null) {
            revenda.getContatos().forEach(contato -> contato.setRevenda(revenda));
        }
        
        if (revenda.getEnderecos() != null) {
            revenda.getEnderecos().forEach(endereco -> endereco.setRevenda(revenda));
        }
        
        Revenda revendaSalva = revendaRepository.save(revenda);
        
        return revendaMapper.toResponseDTO(revendaSalva);
    }
    
    @Override
    @Transactional(readOnly = true)
    public RevendaResponseDTO buscarPorId(Long id) {
        Revenda revenda = revendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revenda não encontrada com id: " + id));
        
        return revendaMapper.toResponseDTO(revenda);
    }
    
    @Override
    @Transactional(readOnly = true)
    public RevendaResponseDTO buscarPorCnpj(String cnpj) {
        Revenda revenda = revendaRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new ResourceNotFoundException("Revenda não encontrada com CNPJ: " + cnpj));
        
        return revendaMapper.toResponseDTO(revenda);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RevendaResponseDTO> listarTodas() {
        return revendaRepository.findAll().stream()
                .map(revendaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public RevendaResponseDTO atualizarRevenda(Long id, RevendaDTO revendaDTO) {
        Revenda revenda = revendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revenda não encontrada com id: " + id));
        
        if (!revenda.getCnpj().equals(revendaDTO.getCnpj())) {
            throw new BusinessException("O CNPJ não pode ser alterado");
        }
        
        revendaMapper.updateEntity(revenda, revendaDTO);
        
        if (revenda.getTelefones() != null) {
            revenda.getTelefones().forEach(telefone -> telefone.setRevenda(revenda));
        }
        
        if (revenda.getContatos() != null) {
            revenda.getContatos().forEach(contato -> contato.setRevenda(revenda));
        }
        
        if (revenda.getEnderecos() != null) {
            revenda.getEnderecos().forEach(endereco -> endereco.setRevenda(revenda));
        }
        
        Revenda revendaAtualizada = revendaRepository.save(revenda);
        
        return revendaMapper.toResponseDTO(revendaAtualizada);
    }
    
    @Override
    @Transactional
    public void deletarRevenda(Long id) {
        if (!revendaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Revenda não encontrada com id: " + id);
        }
        
        revendaRepository.deleteById(id);
    }
    
    private void validarRevenda(RevendaDTO revendaDTO) {

        if (!cnpjValidator.isValid(revendaDTO.getCnpj())) {
            throw new BusinessException("CNPJ inválido");
        }
        
        if (revendaRepository.existsByCnpj(revendaDTO.getCnpj())) {
            throw new BusinessException("Já existe uma revenda cadastrada com este CNPJ");
        }
        
        if (revendaRepository.existsByEmail(revendaDTO.getEmail())) {
            throw new BusinessException("Já existe uma revenda cadastrada com este email");
        }
        
        boolean temContatoPrincipal = revendaDTO.getContatos().stream()
                .anyMatch(ContatoDTO::isPrincipal);
        
        if (!temContatoPrincipal) {
            throw new BusinessException("Deve haver pelo menos um contato principal");
        }
    }
} 