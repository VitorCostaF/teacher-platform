package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.PreferenciasNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PreferenciasNotificacaoRepository extends JpaRepository<PreferenciasNotificacao, UUID> {

    Optional<PreferenciasNotificacao> findByUsuarioId(UUID usuarioId);
}
