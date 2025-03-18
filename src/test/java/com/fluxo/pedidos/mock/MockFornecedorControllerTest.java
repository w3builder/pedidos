package com.fluxo.pedidos.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluxo.pedidos.dto.fornecedor.ItemPedidoFornecedor;
import com.fluxo.pedidos.dto.fornecedor.PedidoFornecedorRequest;
import com.fluxo.pedidos.dto.fornecedor.PedidoFornecedorResponse;
import com.fluxo.pedidos.exception.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
public class MockFornecedorControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private MockFornecedorController mockFornecedorController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        // Configuração do MockMvc manual como no RevendaControllerTest
        mockMvc = MockMvcBuilders.standaloneSetup(mockFornecedorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve receber pedido com sucesso")
    public void testReceberPedidoSucesso() throws Exception {
        // Arrange
        PedidoFornecedorRequest request = createValidRequest();
        
        // Cria um spy do controller para substituir o comportamento aleatório
        MockFornecedorController controllerSpy = spy(mockFornecedorController);
        doReturn(5).when(controllerSpy).getRandomValue(anyInt());
        
        // Configura o mockMvc para usar o spy
        MockMvc mockMvcWithSpy = MockMvcBuilders.standaloneSetup(controllerSpy)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // Act
        MvcResult result = mockMvcWithSpy.perform(post("/api/fornecedor/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        
        // Assert
        PedidoFornecedorResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), PedidoFornecedorResponse.class);
        
        assertNotNull(response);
        assertNotNull(response.getNumeroPedido());
        assertEquals("CONFIRMADO", response.getStatus());
        assertEquals(request.getItens().size(), response.getItensConfirmados().size());
    }

    @Test
    @DisplayName("Deve retornar erro ao receber pedido com revenda inválida")
    public void testReceberPedidoRevendaInvalida() throws Exception {
        // Arrange - Revenda ID ausente
        PedidoFornecedorRequest request = createValidRequest();
        request.setRevendaId(null);
        
        // Act & Assert
        mockMvc.perform(post("/api/fornecedor/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        // Arrange - Código revenda ausente
        request = createValidRequest();
        request.setCodigoRevenda(null);
        
        // Act & Assert
        mockMvc.perform(post("/api/fornecedor/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro ao receber pedido com quantidade abaixo do mínimo")
    public void testReceberPedidoQuantidadeMinima() throws Exception {
        // Arrange
        PedidoFornecedorRequest request = createValidRequest();
        request.getItens().get(0).setQuantidade(999); // Menos que o mínimo de 1000
        
        // Act & Assert
        mockMvc.perform(post("/api/fornecedor/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve simular instabilidade no serviço do fornecedor")
    public void testReceberPedidoInstabilidade() throws Exception {
        // Arrange
        PedidoFornecedorRequest request = createValidRequest();
        
        // Cria um spy do controller para simular instabilidade
        MockFornecedorController controllerSpy = spy(mockFornecedorController);
        doReturn(2).when(controllerSpy).getRandomValue(anyInt());
        
        // Configura o mockMvc para usar o spy
        MockMvc mockMvcWithSpy = MockMvcBuilders.standaloneSetup(controllerSpy)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // Act & Assert
        mockMvcWithSpy.perform(post("/api/fornecedor/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable());
    }
    
    // Método auxiliar para criar uma requisição válida
    private PedidoFornecedorRequest createValidRequest() {
        PedidoFornecedorRequest request = new PedidoFornecedorRequest();
        request.setRevendaId(1L);
        request.setCodigoRevenda("REV001");
        
        List<ItemPedidoFornecedor> itens = new ArrayList<>();
        ItemPedidoFornecedor item = new ItemPedidoFornecedor();
        item.setProdutoId(1L);
        item.setQuantidade(1000);
        itens.add(item);
        
        request.setItens(itens);
        return request;
    }
} 