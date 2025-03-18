package com.fluxo.pedidos.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluxo.pedidos.dto.fornecedor.ItemPedidoFornecedor;
import com.fluxo.pedidos.dto.fornecedor.PedidoFornecedorRequest;
import com.fluxo.pedidos.dto.fornecedor.PedidoFornecedorResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MockFornecedorController.class)
public class MockFornecedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    public void testReceberPedidoSucesso() throws Exception {
        PedidoFornecedorRequest request = createValidRequest();
        
        // Create a spy of the controller to replace its random behavior
        MockFornecedorController controller = new MockFornecedorController();
        MockFornecedorController spy = Mockito.spy(controller);
        
        // Use doReturn instead of when for mocking the Random behavior
        doReturn(5).when(spy).getRandomValue(anyInt());
        
        // Configure mockMvc to use our spy
        MockMvc mockMvcWithSpy = MockMvcBuilders.standaloneSetup(spy).build();
        
        MvcResult result = mockMvcWithSpy.perform(post("/api/fornecedor/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        
        PedidoFornecedorResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), PedidoFornecedorResponse.class);
        
        assertNotNull(response);
        assertNotNull(response.getNumeroPedido());
        assertEquals("CONFIRMADO", response.getStatus());
        assertEquals(request.getItens().size(), response.getItensConfirmados().size());
    }

    @Test
    public void testReceberPedidoRevendaInvalida() throws Exception {
        PedidoFornecedorRequest request = createValidRequest();
        request.setRevendaId(null);
        
        mockMvc.perform(post("/api/fornecedor/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        request = createValidRequest();
        request.setCodigoRevenda(null);
        
        mockMvc.perform(post("/api/fornecedor/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testReceberPedidoQuantidadeMinima() throws Exception {
        PedidoFornecedorRequest request = createValidRequest();
        request.getItens().get(0).setQuantidade(999); // Menos que o mínimo de 1000
        
        mockMvc.perform(post("/api/fornecedor/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testReceberPedidoInstabilidade() throws Exception {
        PedidoFornecedorRequest request = createValidRequest();
        
        // Create a spy of the controller to replace its random behavior
        MockFornecedorController controller = new MockFornecedorController();
        MockFornecedorController spy = Mockito.spy(controller);
        
        // Use doReturn instead of when for mocking the Random behavior
        doReturn(2).when(spy).getRandomValue(anyInt());
        
        // Configure mockMvc to use our spy
        MockMvc mockMvcWithSpy = MockMvcBuilders.standaloneSetup(spy).build();
        
        mockMvcWithSpy.perform(post("/api/fornecedor/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable());
    }
} 