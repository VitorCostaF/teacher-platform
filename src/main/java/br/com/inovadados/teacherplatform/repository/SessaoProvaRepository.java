package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.SessaoProva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessaoProvaRepository extends JpaRepository<SessaoProva, Long> {

    Optional<SessaoProva> findByAvaliacaoIdAndAlunoId(Long avaliacaoId, UUID alunoId);

    List<SessaoProva> findByEncerradaEmIsNullAndAvaliacaoEncerraEmBefore(OffsetDateTime now);
}
