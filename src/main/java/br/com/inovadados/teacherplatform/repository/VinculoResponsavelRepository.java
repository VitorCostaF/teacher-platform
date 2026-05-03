package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.VinculoResponsavel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VinculoResponsavelRepository extends JpaRepository<VinculoResponsavel, Long> {

    List<VinculoResponsavel> findByResponsavelId(UUID responsavelId);
}
