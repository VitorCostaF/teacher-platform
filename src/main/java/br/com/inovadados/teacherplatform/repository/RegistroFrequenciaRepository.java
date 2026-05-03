package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.RegistroFrequencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroFrequenciaRepository extends JpaRepository<RegistroFrequencia, Long> {

    Optional<RegistroFrequencia> findByTurmaIdAndDataAula(Long turmaId, LocalDate data);

    List<RegistroFrequencia> findByTurmaIdAndAlunoId(Long turmaId, UUID alunoId);
}
