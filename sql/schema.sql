PRAGMA foreign_keys = ON;

CREATE TABLE paises (
    pai_id INTEGER PRIMARY KEY AUTOINCREMENT,
    pai_nome VARCHAR(100) NOT NULL,
    pai_sigla CHAR(2) NOT NULL UNIQUE
);

CREATE TABLE estados (
    est_id INTEGER PRIMARY KEY AUTOINCREMENT,
    est_nome VARCHAR(100) NOT NULL,
    est_uf CHAR(2) NOT NULL UNIQUE,
    est_pai_id INTEGER NOT NULL,
    CONSTRAINT fk_estado_pais FOREIGN KEY (est_pai_id) REFERENCES paises (pai_id)
);

CREATE TABLE tipos_logradouro (
    tpl_id INTEGER PRIMARY KEY AUTOINCREMENT,
    tpl_descricao VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tipos_telefone (
    tpt_id INTEGER PRIMARY KEY AUTOINCREMENT,
    tpt_descricao VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE categorias_inativacao (
    cin_id INTEGER PRIMARY KEY AUTOINCREMENT,
    cin_descricao VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE categorias_ativacao (
    cat_id INTEGER PRIMARY KEY AUTOINCREMENT,
    cat_descricao VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE enderecos (
    end_id INTEGER PRIMARY KEY AUTOINCREMENT,
    end_tpl_id INTEGER NOT NULL,
    end_logradouro VARCHAR(150) NOT NULL,
    end_numero VARCHAR(20) NOT NULL,
    end_complemento VARCHAR(100) NULL,
    end_bairro VARCHAR(100) NOT NULL,
    end_cep CHAR(8) NOT NULL,
    end_cidade VARCHAR(100) NOT NULL,
    end_est_id INTEGER NOT NULL,
    CONSTRAINT fk_endereco_tipo_logradouro FOREIGN KEY (end_tpl_id) REFERENCES tipos_logradouro (tpl_id),
    CONSTRAINT fk_endereco_estado FOREIGN KEY (end_est_id) REFERENCES estados (est_id)
);

CREATE TABLE telefones (
    tel_id INTEGER PRIMARY KEY AUTOINCREMENT,
    tel_tpt_id INTEGER NOT NULL,
    tel_ddd CHAR(2) NOT NULL,
    tel_numero VARCHAR(9) NOT NULL,
    CONSTRAINT fk_telefone_tipo FOREIGN KEY (tel_tpt_id) REFERENCES tipos_telefone (tpt_id)
);

CREATE TABLE fornecedores (
    for_id INTEGER PRIMARY KEY AUTOINCREMENT,
    for_codigo VARCHAR(20) NOT NULL,
    for_razao_social VARCHAR(150) NOT NULL,
    for_nome_fantasia VARCHAR(150) NOT NULL,
    for_cnpj CHAR(14) NOT NULL,
    for_email VARCHAR(150) NOT NULL,
    for_status VARCHAR(10) NOT NULL DEFAULT 'ATIVO',
    for_tel_id INTEGER NOT NULL UNIQUE,
    for_end_id INTEGER NOT NULL UNIQUE,
    CONSTRAINT uk_fornecedor_codigo UNIQUE (for_codigo),
    CONSTRAINT uk_fornecedor_cnpj UNIQUE (for_cnpj),
    CONSTRAINT chk_fornecedor_status CHECK (for_status IN ('ATIVO', 'INATIVO')),
    CONSTRAINT fk_fornecedor_telefone FOREIGN KEY (for_tel_id) REFERENCES telefones (tel_id) ON DELETE RESTRICT,
    CONSTRAINT fk_fornecedor_endereco FOREIGN KEY (for_end_id) REFERENCES enderecos (end_id) ON DELETE RESTRICT
);

CREATE INDEX idx_fornecedores_razao_social ON fornecedores (for_razao_social);
CREATE INDEX idx_fornecedores_nome_fantasia ON fornecedores (for_nome_fantasia);
CREATE INDEX idx_fornecedores_status ON fornecedores (for_status);
CREATE INDEX idx_enderecos_cidade_estado ON enderecos (end_cidade, end_est_id);

CREATE TABLE historico_status_fornecedor (
    hsf_id INTEGER PRIMARY KEY AUTOINCREMENT,
    hsf_for_id INTEGER NOT NULL,
    hsf_status_aplicado VARCHAR(10) NOT NULL,
    hsf_justificativa VARCHAR(500) NOT NULL,
    hsf_cin_id INTEGER NULL,
    hsf_cat_id INTEGER NULL,
    hsf_data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hsf_fornecedor FOREIGN KEY (hsf_for_id) REFERENCES fornecedores (for_id),
    CONSTRAINT fk_hsf_cat_inativacao FOREIGN KEY (hsf_cin_id) REFERENCES categorias_inativacao (cin_id),
    CONSTRAINT fk_hsf_cat_ativacao FOREIGN KEY (hsf_cat_id) REFERENCES categorias_ativacao (cat_id),
    CONSTRAINT chk_hsf_status CHECK (hsf_status_aplicado IN ('ATIVO', 'INATIVO')),
    CONSTRAINT chk_hsf_categoria_obrigatoria CHECK (
        (hsf_status_aplicado = 'INATIVO' AND hsf_cin_id IS NOT NULL AND hsf_cat_id IS NULL) OR
        (hsf_status_aplicado = 'ATIVO' AND hsf_cat_id IS NOT NULL AND hsf_cin_id IS NULL)
    )
);

CREATE TABLE logs_transacao (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    log_for_id INTEGER NOT NULL,
    log_operacao VARCHAR(30) NOT NULL,
    log_data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    log_usuario_responsavel VARCHAR(100) NOT NULL,
    log_dados_alterados TEXT NOT NULL,
    CONSTRAINT fk_log_fornecedor FOREIGN KEY (log_for_id) REFERENCES fornecedores (for_id),
    CONSTRAINT chk_log_operacao CHECK (log_operacao IN ('INCLUSAO', 'ALTERACAO', 'INATIVACAO', 'ATIVACAO'))
);

INSERT INTO paises (pai_nome, pai_sigla) VALUES ('Brasil', 'BR');

INSERT INTO estados (est_nome, est_uf, est_pai_id) VALUES
('Acre', 'AC', 1), ('Alagoas', 'AL', 1), ('Amapá', 'AP', 1), ('Amazonas', 'AM', 1),
('Bahia', 'BA', 1), ('Ceará', 'CE', 1), ('Distrito Federal', 'DF', 1), ('Espírito Santo', 'ES', 1),
('Goiás', 'GO', 1), ('Maranhão', 'MA', 1), ('Mato Grosso', 'MT', 1), ('Mato Grosso do Sul', 'MS', 1),
('Minas Gerais', 'MG', 1), ('Pará', 'PA', 1), ('Paraíba', 'PB', 1), ('Paraná', 'PR', 1),
('Pernambuco', 'PE', 1), ('Piauí', 'PI', 1), ('Rio de Janeiro', 'RJ', 1), ('Rio Grande do Norte', 'RN', 1),
('Rio Grande do Sul', 'RS', 1), ('Rondônia', 'RO', 1), ('Roraima', 'RR', 1), ('Santa Catarina', 'SC', 1),
('São Paulo', 'SP', 1), ('Sergipe', 'SE', 1), ('Tocantins', 'TO', 1);

INSERT INTO tipos_logradouro (tpl_descricao) VALUES
('Rua'), ('Avenida'), ('Rodovia'), ('Alameda'), ('Praça'), ('Travessa'), ('Estrada');

INSERT INTO tipos_telefone (tpt_descricao) VALUES
('Fixo'), ('Celular'), ('Comercial');

INSERT INTO categorias_inativacao (cin_descricao) VALUES
('Encerramento de Contrato'),
('Inadimplência / Problemas Fiscais'),
('Atraso Recorrente nas Entregas'),
('Baixa Qualidade dos Produtos'),
('Solicitação do Próprio Fornecedor');

INSERT INTO categorias_ativacao (cat_descricao) VALUES
('Renovação de Contrato'),
('Regularização Fiscal / Financeira'),
('Acordo Comercial Restabelecido'),
('Reavaliação de Qualidade Aprovada');