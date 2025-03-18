package com.fluxo.pedidos.mock;

import com.fluxo.pedidos.dto.fornecedor.ItemPedidoFornecedor;
import com.fluxo.pedidos.dto.fornecedor.PedidoFornecedorRequest;
import com.fluxo.pedidos.dto.fornecedor.PedidoFornecedorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/fornecedor")
public class MockFornecedorController {
    private static final Logger logger = LoggerFactory.getLogger(MockFornecedorController.class);
    private static final Random random = new Random();
    
    @PostMapping("/pedidos")
    public ResponseEntity<PedidoFornecedorResponse> receberPedido(
            @RequestBody PedidoFornecedorRequest request) {
        
        logger.info("Recebido pedido da revenda: {}", request.getCodigoRevenda());
        
        // Validação da revenda
        if (request.getRevendaId() == null || request.getCodigoRevenda() == null) {
            logger.error("Revenda não identificada");
            return ResponseEntity.badRequest().build();
        }
        
        // Validação de quantidade mínima
        int quantidadeTotal = request.getItens().stream()
                .mapToInt(ItemPedidoFornecedor::getQuantidade)
                .sum();
                
        if (quantidadeTotal < 1000) {
            logger.error("Quantidade mínima não atingida: {}", quantidadeTotal);
            return ResponseEntity.badRequest().build();
        }
        
        // Simular instabilidade (30% de chance de falha)
        if (random.nextInt(10) < 3) {
            logger.warn("Simulando falha no serviço do fornecedor");
            return ResponseEntity.status(503).build();
        }
        
        // Criar resposta
        PedidoFornecedorResponse response = new PedidoFornecedorResponse();
        response.setNumeroPedido("FORN-" + UUID.randomUUID().toString().substring(0, 8));
        response.setItensConfirmados(request.getItens());
        response.setStatus("CONFIRMADO");
        
        logger.info("Pedido confirmado com número: {}", response.getNumeroPedido());
        
        return ResponseEntity.ok(response);
    }
} 