package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.entity.Conteudo;
import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.domain.entity.Flashcard;
import br.com.inovadados.teacherplatform.domain.entity.Matricula;
import br.com.inovadados.teacherplatform.domain.enums.StatusAvaliacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import br.com.inovadados.teacherplatform.dto.response.ConteudoFeedDto;
import br.com.inovadados.teacherplatform.dto.response.DesempenhoDisciplinaDto;
import br.com.inovadados.teacherplatform.dto.response.DesempenhoAlunoSimplesResponse;
import br.com.inovadados.teacherplatform.dto.response.FeedAlunoResponse;
import br.com.inovadados.teacherplatform.dto.response.ItemFeedDto;
import br.com.inovadados.teacherplatform.dto.response.RecomendacaoDto;
import br.com.inovadados.teacherplatform.repository.AvaliacaoRepository;
import br.com.inovadados.teacherplatform.repository.ConteudoRepository;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.FlashcardRepository;
import br.com.inovadados.teacherplatform.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedAlunoService {

    private final MatriculaRepository matriculaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final EntregaRepository entregaRepository;
    private final ConteudoRepository conteudoRepository;
    private final FlashcardRepository flashcardRepository;
    private final GamificacaoService gamificacaoService;

    public FeedAlunoResponse getFeed(UUID alunoId) {
        List<Long> turmaIds = getTurmaIds(alunoId);
        if (turmaIds.isEmpty()) {
            return new FeedAlunoResponse(List.of(), List.of(), List.of(), List.of());
        }

        List<Avaliacao> disponiveis = avaliacaoRepository.findByTurmaIdInAndStatus(turmaIds, StatusAvaliacaoEnum.PUBLICADA);
        List<Entrega> entregasAluno = entregaRepository.findByAlunoId(alunoId);

        OffsetDateTime agora = OffsetDateTime.now();
        OffsetDateTime limite24h = agora.plusHours(24);

        List<ItemFeedDto> urgentes = disponiveis.stream()
                .filter(a -> a.getEncerraEm() != null && a.getEncerraEm().isBefore(limite24h))
                .filter(a -> semEntregaRealizada(a.getId(), entregasAluno))
                .map(a -> toItemFeed(a, entregasAluno, agora))
                .toList();

        List<ItemFeedDto> paraFazer = disponiveis.stream()
                .filter(a -> semEntregaRealizada(a.getId(), entregasAluno))
                .sorted((a, b) -> {
                    if (a.getEncerraEm() == null) return 1;
                    if (b.getEncerraEm() == null) return -1;
                    return a.getEncerraEm().compareTo(b.getEncerraEm());
                })
                .map(a -> toItemFeed(a, entregasAluno, agora))
                .toList();

        List<ConteudoFeedDto> novosConteudos = conteudoRepository
                .findByTurmaIdInAndPublicadoEmAfterOrderByPublicadoEmDesc(turmaIds, agora.minusHours(48))
                .stream()
                .map(this::toConteudoFeed)
                .toList();

        List<RecomendacaoDto> recomendados = gerarRecomendacoes(alunoId, turmaIds, entregasAluno);

        return new FeedAlunoResponse(urgentes, paraFazer, novosConteudos, recomendados);
    }

    public DesempenhoAlunoSimplesResponse getDesempenho(UUID alunoId) {
        List<Matricula> matriculas = matriculaRepository.findByAlunoIdAndRemovidoEmIsNull(alunoId);
        List<Entrega> entregas = entregaRepository.findByAlunoId(alunoId);

        List<Entrega> entregasFinalizadas = entregas.stream()
                .filter(e -> e.getStatus() == StatusEntregaEnum.ENTREGUE || e.getStatus() == StatusEntregaEnum.CORRIGIDA)
                .toList();

        long entregasNoPrazo = entregasFinalizadas.stream()
                .filter(e -> !e.isEntregaAtrasada())
                .count();

        BigDecimal mediaGeral = entregasFinalizadas.stream()
                .filter(e -> e.getNotaFinal() != null)
                .map(Entrega::getNotaFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long comNota = entregasFinalizadas.stream().filter(e -> e.getNotaFinal() != null).count();
        if (comNota > 0) {
            mediaGeral = mediaGeral.divide(BigDecimal.valueOf(comNota), 2, RoundingMode.HALF_UP);
        }

        Map<String, List<Entrega>> porDisciplina = entregasFinalizadas.stream()
                .filter(e -> e.getNotaFinal() != null)
                .collect(Collectors.groupingBy(e -> e.getAvaliacao().getTurma().getDisciplina()));

        List<DesempenhoDisciplinaDto> porDisciplinaDto = porDisciplina.entrySet().stream()
                .map(entry -> {
                    List<Entrega> lista = entry.getValue();
                    BigDecimal media = lista.stream()
                            .map(Entrega::getNotaFinal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(lista.size()), 2, RoundingMode.HALF_UP);
                    String tendencia = media.compareTo(new BigDecimal("7")) >= 0 ? "POSITIVA" : "NEGATIVA";
                    return new DesempenhoDisciplinaDto(entry.getKey(), media, tendencia);
                })
                .toList();

        return new DesempenhoAlunoSimplesResponse(
                mediaGeral,
                entregasFinalizadas.size(),
                (int) entregasNoPrazo,
                porDisciplinaDto,
                gamificacaoService.getConquistas(alunoId)
        );
    }

    private List<RecomendacaoDto> gerarRecomendacoes(UUID alunoId, List<Long> turmaIds, List<Entrega> entregas) {
        List<Flashcard> flashcardsDisponiveis = flashcardRepository.findByTurmaIdIn(turmaIds);
        if (flashcardsDisponiveis.isEmpty()) return List.of();

        return flashcardsDisponiveis.stream()
                .limit(5)
                .map(f -> new RecomendacaoDto(
                        f.getId(),
                        "FLASHCARD",
                        f.getPergunta(),
                        f.getTurma().getDisciplina(),
                        "Recomendado para revisão"
                ))
                .toList();
    }

    private boolean semEntregaRealizada(Long avaliacaoId, List<Entrega> entregas) {
        return entregas.stream()
                .filter(e -> e.getAvaliacao().getId().equals(avaliacaoId))
                .noneMatch(e -> e.getStatus() == StatusEntregaEnum.ENTREGUE
                        || e.getStatus() == StatusEntregaEnum.CORRIGIDA);
    }

    private ItemFeedDto toItemFeed(Avaliacao a, List<Entrega> entregas, OffsetDateTime agora) {
        Optional<Entrega> entrega = entregas.stream()
                .filter(e -> e.getAvaliacao().getId().equals(a.getId()))
                .findFirst();

        String status = entrega.map(e -> switch (e.getStatus()) {
            case NAO_INICIADA -> "NAO_INICIADO";
            case RASCUNHO -> "EM_ANDAMENTO";
            default -> "ENTREGUE";
        }).orElse("NAO_INICIADO");

        boolean atrasado = a.getEncerraEm() != null && a.getEncerraEm().isBefore(agora);

        return new ItemFeedDto(
                a.getId(),
                a.getTipo().name(),
                a.getTitulo(),
                a.getTurma().getDisciplina(),
                a.getEncerraEm(),
                status,
                atrasado
        );
    }

    private ConteudoFeedDto toConteudoFeed(Conteudo c) {
        return new ConteudoFeedDto(
                c.getId(),
                c.getTitulo(),
                c.getTipo().name(),
                c.getTurma().getDisciplina(),
                c.getPublicadoEm()
        );
    }

    private List<Long> getTurmaIds(UUID alunoId) {
        return matriculaRepository.findByAlunoIdAndRemovidoEmIsNull(alunoId)
                .stream()
                .map(m -> m.getTurma().getId())
                .toList();
    }
}
