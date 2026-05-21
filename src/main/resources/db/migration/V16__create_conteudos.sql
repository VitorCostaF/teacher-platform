CREATE TABLE conteudos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  turma_id BIGINT NOT NULL REFERENCES turmas(id),
  professor_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
  titulo VARCHAR(255) NOT NULL,
  tipo ENUM('texto','video','arquivo','link') NOT NULL,
  corpo TEXT,
  arquivo_url VARCHAR(500),
  video_url VARCHAR(500),
  link_externo VARCHAR(500),
  publicado_em DATETIME,
  serie_sugerida VARCHAR(50),
  topicos JSON,
  criado_em DATETIME NOT NULL DEFAULT NOW()
);
