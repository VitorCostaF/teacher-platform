package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    Optional<Entrega> findByAvaliacaoIdAndAlunoId(Long avaliacaoId, UUID alunoId);
}
