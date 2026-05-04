package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.ObservacaoProfessor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ObservacaoProfessorRepository extends JpaRepository<ObservacaoProfessor, Long> {

    List<ObservacaoProfessor> findByTurmaIdAndAlunoIdOrderByCriadoEmDesc(Long turmaId, UUID alunoId);
}
