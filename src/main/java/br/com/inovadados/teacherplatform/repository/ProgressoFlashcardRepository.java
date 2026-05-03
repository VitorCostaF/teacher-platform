package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.ProgressoFlashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ProgressoFlashcardRepository extends JpaRepository<ProgressoFlashcard, Long> {

    List<ProgressoFlashcard> findByAlunoIdAndProximaRevisaoBefore(UUID alunoId, OffsetDateTime now);
}
