CREATE TABLE observacoes_professor (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  aluno_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  professor_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  texto TEXT NOT NULL,
  criado_em DATETIME NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_obs_turma_aluno ON observacoes_professor(turma_id, aluno_id);
