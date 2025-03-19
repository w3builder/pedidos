package com.fluxo.pedidos.presentation.dto.response;

import java.util.List;

import com.fluxo.pedidos.presentation.dto.request.ContatoDTO;
import com.fluxo.pedidos.presentation.dto.request.EnderecoDTO;
import com.fluxo.pedidos.presentation.dto.request.TelefoneDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevendaResponseDTO {
    
    private Long id;
    private String cnpj;
    private String razaoSocial;
    private String nomeFantasia;
    private String email;
    private List<TelefoneDTO> telefones;
    private List<ContatoDTO> contatos;
    private List<EnderecoDTO> enderecos;
} 