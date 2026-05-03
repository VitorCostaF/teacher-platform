package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessaoRepository extends JpaRepository<Sessao, Long> {

    Optional<Sessao> findByRefreshTokenHashAndRevogadoEmIsNull(String hash);

    List<Sessao> findByUsuarioIdAndRevogadoEmIsNull(UUID usuarioId);
}
