CREATE TABLE logs_uso_ia (
    id         BIGSERIAL PRIMARY KEY,
    professor_id UUID NOT NULL REFERENCES usuarios(id),
    escola_id  BIGINT NOT NULL REFERENCES escolas(id),
    endpoint   VARCHAR(100) NOT NULL,
    tokens_usados INTEGER NOT NULL,
    criado_em  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_logs_ia_professor_hora ON logs_uso_ia(professor_id, criado_em);
