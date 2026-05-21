CREATE TABLE avaliacoes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  professor_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  titulo VARCHAR(255) NOT NULL,
  tipo ENUM('prova','atividade','trabalho') NOT NULL,
  status ENUM('rascunho','agendada','publicada','encerrada') NOT NULL DEFAULT 'rascunho',
  peso_nota NUMERIC(4,2),
  duracao_minutos INTEGER,
  disponivel_em DATETIME,
  encerra_em DATETIME,
  embaralhar_questoes BOOLEAN NOT NULL DEFAULT FALSE,
  embaralhar_alternativas BOOLEAN NOT NULL DEFAULT FALSE,
  gabarito_liberacao ENUM('imediata','apos_encerramento','manual') NOT NULL DEFAULT 'apos_encerramento',
  permite_entrega_atrasada BOOLEAN NOT NULL DEFAULT FALSE,
  gerado_por_ia BOOLEAN NOT NULL DEFAULT FALSE,
  criado_em DATETIME NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_avaliacoes_turma_id ON avaliacoes(turma_id);
CREATE INDEX idx_avaliacoes_professor_id ON avaliacoes(professor_id);
