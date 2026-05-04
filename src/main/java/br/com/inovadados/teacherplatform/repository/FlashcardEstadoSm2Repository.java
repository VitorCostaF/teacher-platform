package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.FlashcardEstadoSm2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlashcardEstadoSm2Repository extends JpaRepository<FlashcardEstadoSm2, Long> {

    Optional<FlashcardEstadoSm2> findByAlunoIdAndFlashcardId(UUID alunoId, Long flashcardId);

    List<FlashcardEstadoSm2> findByAlunoIdAndProximaRevisaoLessThanEqualOrderByProximaRevisaoAsc(
            UUID alunoId, LocalDate hoje);
}
