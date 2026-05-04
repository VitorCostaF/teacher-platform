package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.ObservacaoProfessor;
import br.com.inovadados.teacherplatform.domain.enums.PerfilEnum;
import br.com.inovadados.teacherplatform.dto.request.ObservacaoRequest;
import br.com.inovadados.teacherplatform.dto.response.ObservacaoResponse;
import br.com.inovadados.teacherplatform.exception.TurmaNaoEncontradaException;
import br.com.inovadados.teacherplatform.repository.ObservacaoProfessorRepository;
import br.com.inovadados.teacherplatform.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ObservacaoService {

    private final ObservacaoProfessorRepository observacaoRepository;
    private final TurmaRepository turmaRepository;

    public ObservacaoResponse criar(Long turmaId, UUID alunoId, UUID professorId, ObservacaoRequest req) {
        var turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new TurmaNaoEncontradaException(turmaId));

        var obs = new ObservacaoProfessor();
        obs.setTurma(turma);
        obs.setAlunoId(alunoId);
        obs.setProfessorId(professorId);
        obs.setTexto(req.texto());
        obs.setCriadoEm(OffsetDateTime.now());
        obs = observacaoRepository.save(obs);

        return toResponse(obs);
    }

    @Transactional(readOnly = true)
    public List<ObservacaoResponse> listar(Long turmaId, UUID alunoId, PerfilEnum perfil) {
        if (perfil == PerfilEnum.RESPONSAVEL) return List.of();

        return observacaoRepository.findByTurmaIdAndAlunoIdOrderByCriadoEmDesc(turmaId, alunoId)
                .stream().map(this::toResponse).toList();
    }

    private ObservacaoResponse toResponse(ObservacaoProfessor obs) {
        return new ObservacaoResponse(obs.getId(), obs.getAlunoId(),
                obs.getProfessorId(), obs.getTexto(), obs.getCriadoEm());
    }
}
