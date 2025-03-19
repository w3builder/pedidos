package com.fluxo.pedidos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "camel.springboot.auto-startup=false"
})
class FluxoPedidosApplicationTests {

	@Test
	void contextLoads() {
	}

}
