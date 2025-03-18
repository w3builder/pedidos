package com.fluxo.pedidos.service;

import com.fluxo.pedidos.dto.fornecedor.ItemPedidoFornecedor;
import com.fluxo.pedidos.dto.fornecedor.PedidoFornecedorRequest;
import com.fluxo.pedidos.dto.fornecedor.PedidoFornecedorResponse;
import com.fluxo.pedidos.entity.Pedido;
import com.fluxo.pedidos.entity.PedidoFornecedorPendente;
import com.fluxo.pedidos.repository.PedidoFornecedorPendenteRepository;
import com.fluxo.pedidos.repository.PedidoRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FornecedorService {
    private static final Logger logger = LoggerFactory.getLogger(FornecedorService.class);
    
    private final RestTemplate restTemplate;
    private final PedidoRepository pedidoRepository;
    private final PedidoFornecedorPendenteRepository pendenteRepository;
    
    @Value("${fornecedor.api.url:http://localhost:8081/api/fornecedor/pedidos}")
    private String fornecedorApiUrl;
    
    @Value("${fornecedor.pedido.quantidade.minima:1000}")
    private int quantidadeMinimaPedido;
    
    @Autowired
    public FornecedorService(RestTemplate restTemplate, 
                             PedidoRepository pedidoRepository,
                             PedidoFornecedorPendenteRepository pendenteRepository) {
        this.restTemplate = restTemplate;
        this.pedidoRepository = pedidoRepository;
        this.pendenteRepository = pendenteRepository;
    }
    
    @CircuitBreaker(name = "fornecedorService", fallbackMethod = "fallbackEnviarPedido")
    @Retry(name = "fornecedorService")
    public PedidoFornecedorResponse enviarPedido(Long pedidoId) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        
        if (!pedidoOpt.isPresent()) {
            logger.error("Pedido não encontrado: {}", pedidoId);
            throw new RuntimeException("Pedido não encontrado");
        }
        
        Pedido pedido = pedidoOpt.get();
        
        // Verificar se atende à quantidade mínima
        int quantidadeTotal = pedido.getItens().stream()
                .mapToInt(item -> item.getQuantidade())
                .sum();
                
        if (quantidadeTotal < quantidadeMinimaPedido) {
            logger.error("Quantidade total do pedido ({}) não atende ao mínimo exigido ({})", 
                        quantidadeTotal, quantidadeMinimaPedido);
            throw new RuntimeException("Pedido não atende à quantidade mínima de " + quantidadeMinimaPedido + " unidades");
        }
        
        // Construir o request
        PedidoFornecedorRequest request = construirPedidoRequest(pedido);
        
        try {
            ResponseEntity<PedidoFornecedorResponse> response = 
                restTemplate.postForEntity(fornecedorApiUrl, request, PedidoFornecedorResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Pedido {} enviado com sucesso para o fornecedor", pedidoId);
                return response.getBody();
            } else {
                logger.error("Erro ao enviar pedido para fornecedor. Status: {}", response.getStatusCode());
                salvarPedidoPendente(pedidoId);
                throw new RuntimeException("Erro ao enviar pedido para fornecedor");
            }
        } catch (Exception e) {
            logger.error("Exceção ao enviar pedido para fornecedor", e);
            salvarPedidoPendente(pedidoId);
            throw e;
        }
    }
    
    private PedidoFornecedorRequest construirPedidoRequest(Pedido pedido) {
        PedidoFornecedorRequest request = new PedidoFornecedorRequest();
        request.setRevendaId(pedido.getRevenda().getId());
        
        // Mapear os itens do pedido para o formato esperado pelo fornecedor
        List<ItemPedidoFornecedor> itens = pedido.getItens().stream()
                .<ItemPedidoFornecedor>map(item -> {
                    ItemPedidoFornecedor itemFornecedor = new ItemPedidoFornecedor();
                    itemFornecedor.setProdutoId(item.getProduto().getId());
                    itemFornecedor.setQuantidade(item.getQuantidade());
                    return itemFornecedor;
                })
                .collect(java.util.stream.Collectors.toList());
                
        request.setItens(itens);
        return request;
    }
    
    @SuppressWarnings("unused")
    private PedidoFornecedorResponse fallbackEnviarPedido(Long pedidoId, Throwable t) {
        logger.warn("Executando fallback para pedido {}", pedidoId);
        salvarPedidoPendente(pedidoId);
        
        PedidoFornecedorResponse response = new PedidoFornecedorResponse();
        response.setNumeroPedido(null);
        response.setStatus("PENDENTE");
        return response;
    }
    
    private void salvarPedidoPendente(Long pedidoId) {
        Optional<PedidoFornecedorPendente> pendenteOpt = pendenteRepository.findById(pedidoId);
        
        if (pendenteOpt.isPresent()) {
            PedidoFornecedorPendente pendente = pendenteOpt.get();
            pendente.setUltimaTentativa(LocalDateTime.now());
            pendente.setNumeroTentativas(pendente.getNumeroTentativas() + 1);
            pendenteRepository.save(pendente);
        } else {
            PedidoFornecedorPendente pendente = new PedidoFornecedorPendente();
            pendente.setPedidoId(pedidoId);
            pendenteRepository.save(pendente);
        }
    }
    
    @Scheduled(fixedDelay = 60000) // Tenta a cada 1 minuto
    public void reprocessarPedidosPendentes() {
        logger.info("Iniciando reprocessamento de pedidos pendentes");
        List<PedidoFornecedorPendente> pendentes = 
                pendenteRepository.findByStatusOrderByUltimaTentativaAsc("PENDENTE");
        
        pendentes.forEach(pendente -> {
            try {
                PedidoFornecedorResponse response = enviarPedido(pendente.getPedidoId());
                if (response != null && response.getNumeroPedido() != null) {
                    pendente.setStatus("PROCESSADO");
                    pendenteRepository.save(pendente);
                }
            } catch (Exception e) {
                logger.error("Erro ao reprocessar pedido pendente {}", pendente.getPedidoId(), e);
            }
        });
    }
} 