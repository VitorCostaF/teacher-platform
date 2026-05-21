CREATE TABLE push_subscriptions (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id VARCHAR(36) NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    endpoint   TEXT NOT NULL,
    p256dh     VARCHAR(500) NOT NULL,
    auth       VARCHAR(200) NOT NULL,
    criado_em  DATETIME NOT NULL DEFAULT NOW(),
    ultimo_uso DATETIME,
    CONSTRAINT uq_push_endpoint UNIQUE (endpoint(500))
);

CREATE INDEX idx_push_usuario ON push_subscriptions(usuario_id);

CREATE TABLE preferencias_notificacao (
    usuario_id       VARCHAR(36) PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
    falta_aluno      BOOLEAN NOT NULL DEFAULT TRUE,
    queda_frequencia BOOLEAN NOT NULL DEFAULT TRUE,
    prazo_prova      BOOLEAN NOT NULL DEFAULT TRUE
);
