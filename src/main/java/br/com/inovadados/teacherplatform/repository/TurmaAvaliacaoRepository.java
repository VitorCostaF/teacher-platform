package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.TurmaAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurmaAvaliacaoRepository extends JpaRepository<TurmaAvaliacao, Long> {

    List<TurmaAvaliacao> findByAvaliacaoId(Long avaliacaoId);

    List<TurmaAvaliacao> findByTurmaId(Long turmaId);
}
