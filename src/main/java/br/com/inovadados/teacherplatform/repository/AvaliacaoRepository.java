package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.enums.StatusAvaliacaoEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByTurmaIdAndStatus(Long turmaId, StatusAvaliacaoEnum status);

    List<Avaliacao> findByTurmaIdInAndStatus(List<Long> turmaIds, StatusAvaliacaoEnum status);

    List<Avaliacao> findByTurmaIdOrderByDisponivelEmAsc(Long turmaId);

    List<Avaliacao> findByTurmaIdIn(List<Long> turmaIds);
}
