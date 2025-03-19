# Fluxo de Pedidos

Sistema de gerenciamento de pedidos para revendas, com integração para fornecedores, desenvolvido com Spring Boot.

## 📋 Funcionalidades

- Cadastro e gerenciamento de revendas
- Cadastro e consulta de pedidos
- Integração com fornecedores
- Tratamento resiliente de falhas (Circuit Breaker e Retry)
- Reprocessamento automático de pedidos pendentes

## 🛠️ Tecnologias

- Java 17
- Spring Boot 3.2.3
- Spring Data JPA
- PostgreSQL
- Resilience4j
- Zipkin (tracing)
- RabbitMQ
- Lombok
- Maven

## 🚀 Configuração e Execução

### Pré-requisitos

- Java 17+
- Docker e Docker Compose
- Maven 3.6+

### Com Docker

O projeto inclui um arquivo docker-compose.yml para facilitar a execução:

```bash
# Iniciar os serviços necessários (PostgreSQL e Zipkin)
docker-compose up -d

# Compilar o projeto
mvn clean install

# Executar a aplicação
mvn spring-boot:run
```

## 🔍 APIs Disponíveis

### API de Revendas

- `GET /api/revendas` - Lista todas as revendas
- `GET /api/revendas/{id}` - Busca revenda por ID
- `GET /api/revendas/cnpj/{cnpj}` - Busca revenda por CNPJ
- `POST /api/revendas` - Cria uma nova revenda
- `PUT /api/revendas/{id}` - Atualiza uma revenda
- `DELETE /api/revendas/{id}` - Remove uma revenda

### API de Pedidos

- `GET /api/revendas/{revendaId}/pedidos` - Lista pedidos da revenda
- `GET /api/revendas/{revendaId}/pedidos/{id}` - Busca pedido por ID
- `GET /api/revendas/{revendaId}/pedidos/numero/{numero}` - Busca pedido por número
- `GET /api/revendas/{revendaId}/pedidos/cliente/{clienteId}` - Lista pedidos de um cliente
- `POST /api/revendas/{revendaId}/pedidos` - Cria um novo pedido
- `PUT /api/revendas/{revendaId}/pedidos/{id}/cancelar` - Cancela um pedido

### API Mock de Fornecedor (para testes)

- `POST /api/fornecedor/pedidos` - Simula envio de pedido para fornecedor

## 📊 Monitoramento

O projeto utiliza Zipkin para rastreamento de requisições.
Interface Web do Zipkin disponível em: http://localhost:9411