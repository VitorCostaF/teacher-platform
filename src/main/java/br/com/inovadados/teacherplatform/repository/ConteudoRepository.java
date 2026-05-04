package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.Conteudo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ConteudoRepository extends JpaRepository<Conteudo, Long> {

    List<Conteudo> findByTurmaIdAndPublicadoEmIsNotNull(Long turmaId);

    List<Conteudo> findByTurmaIdInAndPublicadoEmAfterOrderByPublicadoEmDesc(
            List<Long> turmaIds, OffsetDateTime desde);
}
