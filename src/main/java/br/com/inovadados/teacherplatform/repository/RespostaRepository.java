package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RespostaRepository extends JpaRepository<Resposta, Long> {

    List<Resposta> findByEntregaId(Long entregaId);

    Optional<Resposta> findByEntregaIdAndQuestaoId(Long entregaId, Long questaoId);
}
