package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Flashcard;
import br.com.inovadados.teacherplatform.domain.entity.FlashcardEstadoSm2;
import br.com.inovadados.teacherplatform.domain.entity.ProgressoFlashcard;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.ResultadoFlashcardEnum;
import br.com.inovadados.teacherplatform.dto.response.FlashcardResponse;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.FlashcardEstadoSm2Repository;
import br.com.inovadados.teacherplatform.repository.FlashcardRepository;
import br.com.inovadados.teacherplatform.repository.MatriculaRepository;
import br.com.inovadados.teacherplatform.repository.ProgressoFlashcardRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardEstadoSm2Repository estadoSm2Repository;
    private final ProgressoFlashcardRepository progressoRepository;
    private final MatriculaRepository matriculaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<FlashcardResponse> getFlashcardsPriorizados(UUID alunoId, Long turmaId) {
        List<FlashcardEstadoSm2> estados = estadoSm2Repository
                .findByAlunoIdAndProximaRevisaoLessThanEqualOrderByProximaRevisaoAsc(alunoId, LocalDate.now());

        List<Long> turmaIds = turmaId != null
                ? List.of(turmaId)
                : getTurmaIds(alunoId);

        return estados.stream()
                .filter(e -> turmaIds.contains(e.getFlashcard().getTurma().getId()))
                .map(e -> toResponse(e.getFlashcard()))
                .toList();
    }

    public void registrarAvaliacao(UUID alunoId, Long flashcardId, boolean sabia) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new UnauthorizedException("Flashcard não encontrado"));

        FlashcardEstadoSm2 estado = estadoSm2Repository
                .findByAlunoIdAndFlashcardId(alunoId, flashcardId)
                .orElseGet(() -> criarEstadoInicial(alunoId, flashcard));

        aplicarSm2(estado, sabia);
        estadoSm2Repository.save(estado);

        registrarHistorico(alunoId, flashcard, sabia);
    }

    private FlashcardEstadoSm2 criarEstadoInicial(UUID alunoId, Flashcard flashcard) {
        var estado = new FlashcardEstadoSm2();
        estado.setAlunoId(alunoId);
        estado.setFlashcard(flashcard);
        return estado;
    }

    private void aplicarSm2(FlashcardEstadoSm2 estado, boolean sabia) {
        BigDecimal fator = estado.getFatorFacilidade();
        int intervalo = estado.getIntervaloDias();

        if (sabia) {
            fator = fator.add(new BigDecimal("0.1"));
            intervalo = (int) Math.max(1, Math.round(intervalo * fator.doubleValue()));
        } else {
            fator = fator.subtract(new BigDecimal("0.2"));
            if (fator.compareTo(new BigDecimal("1.3")) < 0) {
                fator = new BigDecimal("1.3");
            }
            intervalo = 1;
        }

        estado.setFatorFacilidade(fator);
        estado.setIntervaloDias(intervalo);
        estado.setProximaRevisao(LocalDate.now().plusDays(intervalo));
        estado.setTotalRevisoes(estado.getTotalRevisoes() + 1);
    }

    private void registrarHistorico(UUID alunoId, Flashcard flashcard, boolean sabia) {
        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

        var progresso = new ProgressoFlashcard();
        progresso.setAluno(aluno);
        progresso.setFlashcard(flashcard);
        progresso.setResultado(sabia ? ResultadoFlashcardEnum.FACIL : ResultadoFlashcardEnum.DIFICIL);
        progresso.setRevisadoEm(OffsetDateTime.now());
        progresso.setProximaRevisao(OffsetDateTime.now().plusDays(1));
        progressoRepository.save(progresso);
    }

    private List<Long> getTurmaIds(UUID alunoId) {
        return matriculaRepository.findByAlunoIdAndRemovidoEmIsNull(alunoId)
                .stream()
                .map(m -> m.getTurma().getId())
                .toList();
    }

    private FlashcardResponse toResponse(Flashcard f) {
        return new FlashcardResponse(
                f.getId(),
                f.getPergunta(),
                f.getResposta(),
                f.getTurma().getDisciplina()
        );
    }
}
