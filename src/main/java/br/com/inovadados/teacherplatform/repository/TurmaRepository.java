package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TurmaRepository extends JpaRepository<Turma, Long> {

    List<Turma> findByProfessorIdAndDeletadoEmIsNull(UUID professorId);

    List<Turma> findByEscolaIdAndDeletadoEmIsNull(Long escolaId);
}
