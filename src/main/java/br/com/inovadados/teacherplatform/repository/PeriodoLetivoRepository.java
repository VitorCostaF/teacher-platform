package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.PeriodoLetivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PeriodoLetivoRepository extends JpaRepository<PeriodoLetivo, Long> {

    Optional<PeriodoLetivo> findByEscolaIdAndAtivoTrue(Long escolaId);
}
