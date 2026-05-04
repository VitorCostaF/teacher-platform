package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.Conquista;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConquistaRepository extends JpaRepository<Conquista, Long> {

    List<Conquista> findByAlunoIdOrderByObtidaEmDesc(UUID alunoId);

    boolean existsByAlunoIdAndTipo(UUID alunoId, String tipo);
}
