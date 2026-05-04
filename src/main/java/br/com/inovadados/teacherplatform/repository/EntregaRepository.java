package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    Optional<Entrega> findByAvaliacaoIdAndAlunoId(Long avaliacaoId, UUID alunoId);

    @Query("SELECT COUNT(e) FROM Entrega e WHERE e.avaliacao.turma.id = :turmaId AND e.status = :status")
    long countByTurmaIdAndStatus(@Param("turmaId") Long turmaId, @Param("status") StatusEntregaEnum status);

    List<Entrega> findByAlunoId(UUID alunoId);

    List<Entrega> findByAvaliacaoId(Long avaliacaoId);

    List<Entrega> findByAvaliacaoIdIn(List<Long> avaliacaoIds);
}
