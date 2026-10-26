-- Operadores (JdbcUserDetailsManager) - {noop} = senha sem criptografia
INSERT INTO users (username, password, enabled) VALUES ('admin', '{noop}admin', TRUE);
INSERT INTO users (username, password, enabled) VALUES ('manutencao', '{noop}manutencao', TRUE);

INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
INSERT INTO authorities (username, authority) VALUES ('manutencao', 'ROLE_MANUTENCAO');

-- Usuarios do sistema (validados no POST /login)
INSERT INTO usuarios (username, senha) VALUES ('joao', '123456');
INSERT INTO usuarios (username, senha) VALUES ('maria', 'abc123');
