CREATE TABLE push_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    endpoint TEXT NOT NULL UNIQUE,
    p256dh VARCHAR(500) NOT NULL,
    auth VARCHAR(200) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ultimo_uso TIMESTAMPTZ
);

CREATE INDEX idx_push_usuario ON push_subscriptions(usuario_id);

CREATE TABLE preferencias_notificacao (
    usuario_id UUID PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
    falta_aluno BOOLEAN NOT NULL DEFAULT TRUE,
    queda_frequencia BOOLEAN NOT NULL DEFAULT TRUE,
    prazo_prova BOOLEAN NOT NULL DEFAULT TRUE
);
