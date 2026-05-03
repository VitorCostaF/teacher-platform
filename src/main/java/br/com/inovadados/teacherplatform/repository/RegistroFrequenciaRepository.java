package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.RegistroFrequencia;
import br.com.inovadados.teacherplatform.domain.enums.StatusFrequenciaEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroFrequenciaRepository extends JpaRepository<RegistroFrequencia, Long> {

    Optional<RegistroFrequencia> findByTurmaIdAndDataAula(Long turmaId, LocalDate data);

    List<RegistroFrequencia> findAllByTurmaIdAndDataAulaOrderByAlunoId(Long turmaId, LocalDate dataAula);

    Optional<RegistroFrequencia> findByTurmaIdAndAlunoIdAndDataAula(Long turmaId, UUID alunoId, LocalDate dataAula);

    List<RegistroFrequencia> findByTurmaIdAndAlunoId(Long turmaId, UUID alunoId);

    List<RegistroFrequencia> findByTurmaIdAndAlunoIdOrderByDataAulaAsc(Long turmaId, UUID alunoId);

    long countByTurmaIdAndAlunoIdAndStatus(Long turmaId, UUID alunoId, StatusFrequenciaEnum status);

    @Query("SELECT COUNT(DISTINCT r.dataAula) FROM RegistroFrequencia r WHERE r.turma.id = :turmaId")
    long countAulasByTurmaId(@Param("turmaId") Long turmaId);
}
