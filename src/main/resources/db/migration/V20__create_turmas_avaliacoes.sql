CREATE TABLE turmas_avaliacoes (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    avaliacao_id BIGINT NOT NULL REFERENCES avaliacoes(id),
    turma_id     BIGINT NOT NULL REFERENCES turmas(id),
    publicado_em DATETIME NOT NULL DEFAULT NOW(),
    UNIQUE (avaliacao_id, turma_id)
);

CREATE INDEX idx_turmas_avaliacoes_avaliacao ON turmas_avaliacoes(avaliacao_id);
CREATE INDEX idx_turmas_avaliacoes_turma     ON turmas_avaliacoes(turma_id);
