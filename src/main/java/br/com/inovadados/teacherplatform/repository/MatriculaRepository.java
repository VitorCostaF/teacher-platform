package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    List<Matricula> findByTurmaIdAndRemovidoEmIsNull(Long turmaId);

    Optional<Matricula> findByTurmaIdAndAlunoId(Long turmaId, UUID alunoId);
}
