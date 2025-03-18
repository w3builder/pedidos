-- Dados de teste para o sistema de pedidos

-- Limpar tabelas na ordem correta (respeitando as dependências de chaves estrangeiras)
TRUNCATE TABLE itens_pedido CASCADE;
TRUNCATE TABLE pedidos CASCADE;
TRUNCATE TABLE produtos CASCADE;
TRUNCATE TABLE clientes CASCADE;
TRUNCATE TABLE telefones CASCADE;
TRUNCATE TABLE contatos CASCADE;
TRUNCATE TABLE enderecos CASCADE;
TRUNCATE TABLE revendas CASCADE;

-- Reiniciar as sequências para evitar conflitos de ID
ALTER SEQUENCE revendas_id_seq RESTART WITH 1;
ALTER SEQUENCE telefones_id_seq RESTART WITH 1;
ALTER SEQUENCE contatos_id_seq RESTART WITH 1;
ALTER SEQUENCE enderecos_id_seq RESTART WITH 1;
ALTER SEQUENCE clientes_id_seq RESTART WITH 1;
ALTER SEQUENCE produtos_id_seq RESTART WITH 1;
ALTER SEQUENCE pedidos_id_seq RESTART WITH 1;
ALTER SEQUENCE itens_pedido_id_seq RESTART WITH 1;

-- Inserção de Revendas
INSERT INTO revendas (id, cnpj, razao_social, nome_fantasia, email) VALUES 
(1, '12.345.678/0001-99', 'Revenda Principal LTDA', 'Revenda Principal', 'contato@revendaprincipal.com.br'),
(2, '98.765.432/0001-88', 'Revenda Secundária LTDA', 'Revenda Secundária', 'contato@revendasecundaria.com.br');

-- Inserção de Clientes
INSERT INTO clientes (id, nome, cpf_cnpj, email, telefone, revenda_id) VALUES 
(1, 'Cliente Empresa A', '11.222.333/0001-44', 'contato@empresaa.com.br', '(11) 5555-1111', 1),
(2, 'Cliente Pessoa B', '222.333.444-55', 'clienteb@email.com', '(11) 99222-3333', 1),
(3, 'Cliente Empresa C', '33.444.555/0001-66', 'contato@empresac.com.br', '(21) 5555-2222', 2),
(4, 'Cliente Pessoa D', '444.555.666-77', 'cliented@email.com', '(21) 99444-5555', 2);

-- Inserção de Produtos
INSERT INTO produtos (id, codigo, nome, descricao, preco, revenda_id) VALUES 
(1, 'PROD-001', 'Produto Premium', 'Produto de alta qualidade', 199.90, 1),
(2, 'PROD-002', 'Produto Standard', 'Produto padrão para uso diário', 99.90, 1),
(3, 'PROD-003', 'Produto Basic', 'Produto básico e econômico', 49.90, 1),
(4, 'PROD-101', 'Produto Gold', 'Produto premium linha ouro', 299.90, 2),
(5, 'PROD-102', 'Produto Silver', 'Produto intermediário linha prata', 149.90, 2),
(6, 'PROD-103', 'Produto Bronze', 'Produto básico linha bronze', 79.90, 2);