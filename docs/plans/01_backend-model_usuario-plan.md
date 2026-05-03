# Plano de Implementação — backend-model_usuario

> **Task origem:** `docs/Tasks/backend-model_usuario.md`
> **Escopo:** Backend — Modelos de Dados
> **Complexidade:** M
> **Sprint:** 0 — Fundação
> **Depende de:** Nenhuma

---

## Contexto do Codebase

Projeto Spring Boot 4.0.5 / Java 21 completamente novo. Pacote raiz: `br.com.inovadados.teacherplatform`. Apenas a `TeacherPlatformApplication.java` e `application.properties` existem. JPA já está no pom.xml. PostgreSQL, Flyway, Spring Security e Lombok ainda precisam ser adicionados.

---

## Dependências a Adicionar no pom.xml

```xml
<!-- Banco de dados -->
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>

<!-- Migrations -->
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-database-postgresql</artifactId>
</dependency>

<!-- Validação -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## Arquivos a Criar

### Migrations (Flyway)
`src/main/resources/db/migration/V1__create_escolas.sql`
```sql
CREATE TABLE escolas (
  id BIGSERIAL PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  cnpj VARCHAR(14) UNIQUE NOT NULL,
  logo_url VARCHAR(500),
  nota_minima_aprovacao NUMERIC(4,2) DEFAULT 5.0 NOT NULL,
  frequencia_minima_aprovacao NUMERIC(5,2) DEFAULT 75.0 NOT NULL,
  sistema_avaliacao VARCHAR(30) NOT NULL,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

`src/main/resources/db/migration/V2__create_usuarios.sql`
```sql
CREATE TYPE perfil_enum AS ENUM ('professor','aluno','responsavel','coordenador','admin');

CREATE TABLE usuarios (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  escola_id BIGINT NOT NULL REFERENCES escolas(id),
  nome VARCHAR(150) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  senha_hash VARCHAR(255),
  perfil perfil_enum NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  avatar_url VARCHAR(500),
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  ultimo_acesso TIMESTAMPTZ
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_escola_perfil ON usuarios(escola_id, perfil);
```

`src/main/resources/db/migration/V3__create_sessoes.sql`
```sql
CREATE TABLE sessoes (
  id BIGSERIAL PRIMARY KEY,
  usuario_id UUID NOT NULL REFERENCES usuarios(id),
  refresh_token_hash VARCHAR(512) NOT NULL,
  user_agent VARCHAR(500),
  ip VARCHAR(45),
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expira_em TIMESTAMPTZ NOT NULL,
  revogado_em TIMESTAMPTZ
);

CREATE INDEX idx_sessoes_usuario_id ON sessoes(usuario_id);
```

`src/main/resources/db/migration/V4__create_tokens_temporarios.sql`
```sql
CREATE TYPE tipo_token_enum AS ENUM ('convite','recuperacao_senha');

CREATE TABLE tokens_temporarios (
  id BIGSERIAL PRIMARY KEY,
  usuario_id UUID NOT NULL REFERENCES usuarios(id),
  tipo tipo_token_enum NOT NULL,
  token_hash VARCHAR(512) UNIQUE NOT NULL,
  expira_em TIMESTAMPTZ NOT NULL,
  usado_em TIMESTAMPTZ,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### Enums Java
`src/main/java/br/com/inovadados/teacherplatform/domain/enums/PerfilEnum.java`
```java
public enum PerfilEnum { PROFESSOR, ALUNO, RESPONSAVEL, COORDENADOR, ADMIN }
```

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/SistemaAvaliacaoEnum.java`
```java
public enum SistemaAvaliacaoEnum { NUMERICA, CONCEITUAL, MISTO }
```

`src/main/java/br/com/inovadados/teacherplatform/domain/enums/TipoTokenEnum.java`
```java
public enum TipoTokenEnum { CONVITE, RECUPERACAO_SENHA }
```

### Entities JPA
`src/main/java/br/com/inovadados/teacherplatform/domain/entity/Escola.java`
- `@Entity @Table(name="escolas")`
- Campos: id (Long), nome, cnpj, logoUrl, notaMinimaAprovacao (BigDecimal), frequenciaMinimaAprovacao (BigDecimal), sistemaAvaliacao (SistemaAvaliacaoEnum), criadoEm, atualizadoEm
- `@OneToMany(mappedBy="escola") List<Usuario> usuarios`

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/Usuario.java`
- `@Entity @Table(name="usuarios")`
- Campos: id (UUID), escola (ManyToOne), nome, email, senhaHash, perfil (PerfilEnum), ativo, avatarUrl, criadoEm, ultimoAcesso
- `@OneToMany(mappedBy="usuario") List<Sessao> sessoes`

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/Sessao.java`
- `@Entity @Table(name="sessoes")`
- Campos: id (Long), usuario (ManyToOne UUID), refreshTokenHash, userAgent, ip, criadoEm, expiraEm, revogadoEm (nullable)

`src/main/java/br/com/inovadados/teacherplatform/domain/entity/TokenTemporario.java`
- `@Entity @Table(name="tokens_temporarios")`
- Campos: id (Long), usuario (ManyToOne UUID), tipo (TipoTokenEnum), tokenHash, expiraEm, usadoEm (nullable), criadoEm

### Repositories
`src/main/java/br/com/inovadados/teacherplatform/repository/EscolaRepository.java`
- Extends `JpaRepository<Escola, Long>`

`src/main/java/br/com/inovadados/teacherplatform/repository/UsuarioRepository.java`
- Extends `JpaRepository<Usuario, UUID>`
- `Optional<Usuario> findByEmail(String email)`
- `List<Usuario> findByEscolaIdAndPerfil(Long escolaId, PerfilEnum perfil)`

`src/main/java/br/com/inovadados/teacherplatform/repository/SessaoRepository.java`
- Extends `JpaRepository<Sessao, Long>`
- `Optional<Sessao> findByRefreshTokenHashAndRevogadoEmIsNull(String hash)`
- `List<Sessao> findByUsuarioIdAndRevogadoEmIsNull(UUID usuarioId)`

`src/main/java/br/com/inovadados/teacherplatform/repository/TokenTemporarioRepository.java`
- Extends `JpaRepository<TokenTemporario, Long>`
- `Optional<TokenTemporario> findByTokenHashAndUsadoEmIsNull(String hash)`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `pom.xml` | Adicionar: postgresql, flyway-core, flyway-database-postgresql, spring-boot-starter-validation |
| `src/main/resources/application.properties` | Adicionar: spring.datasource.*, spring.jpa.*, spring.flyway.* |

### Configuração application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/teacher_platform
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

---

## Ordem de Implementação

1. Modificar `pom.xml` — adicionar dependências
2. Criar migrations V1 → V4 (nessa ordem, FK dependencies)
3. Criar enums Java (PerfilEnum, SistemaAvaliacaoEnum, TipoTokenEnum)
4. Criar entities (Escola → Usuario → Sessao → TokenTemporario)
5. Criar repositories
6. Atualizar `application.properties`
7. Subir PostgreSQL local e validar `mvn flyway:migrate`
8. Teste: `mvn test` (JPA schema validation)

---

## Resumo

- **6 arquivos SQL** a criar (4 migrations)
- **7 arquivos Java** a criar (3 enums + 4 entities + 4 repositories = 11 total)
- **2 arquivos** a modificar (pom.xml, application.properties)
- **Bibliotecas a adicionar:** postgresql, flyway-core, flyway-database-postgresql, spring-boot-starter-validation
- **Complexidade mantida:** M
