CREATE TABLE registros_frequencia (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  aluno_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  data_aula DATE NOT NULL,
  status ENUM('presente','ausente','justificado','meio_periodo') NOT NULL,
  observacao TEXT,
  lancado_por VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  lancado_em DATETIME NOT NULL DEFAULT NOW(),
  editado_em DATETIME,
  CONSTRAINT uq_frequencia_turma_aluno_data UNIQUE (turma_id, aluno_id, data_aula)
);

CREATE INDEX idx_frequencia_turma_data ON registros_frequencia(turma_id, data_aula);
CREATE INDEX idx_frequencia_aluno ON registros_frequencia(aluno_id);
