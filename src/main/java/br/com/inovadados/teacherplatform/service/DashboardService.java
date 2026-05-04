package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.domain.entity.Matricula;
import br.com.inovadados.teacherplatform.domain.entity.Turma;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.PerfilEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusFrequenciaEnum;
import br.com.inovadados.teacherplatform.dto.response.AlertaDashboardDto;
import br.com.inovadados.teacherplatform.dto.response.AlunoRankingDto;
import br.com.inovadados.teacherplatform.dto.response.DashboardProfessorResponse;
import br.com.inovadados.teacherplatform.dto.response.DesempenhoAlunoResponse;
import br.com.inovadados.teacherplatform.dto.response.DesempenhoTurmaResponse;
import br.com.inovadados.teacherplatform.dto.response.EvolucaoNotaDto;
import br.com.inovadados.teacherplatform.dto.response.FaixaNotaDto;
import br.com.inovadados.teacherplatform.dto.response.FrequenciaMensalDto;
import br.com.inovadados.teacherplatform.dto.response.MapaCalorDto;
import br.com.inovadados.teacherplatform.dto.response.MediaHistoricaDto;
import br.com.inovadados.teacherplatform.dto.response.TopicoAcertoDto;
import br.com.inovadados.teacherplatform.dto.response.TurmaDashboardDto;
import br.com.inovadados.teacherplatform.exception.AcessoNegadoException;
import br.com.inovadados.teacherplatform.exception.TurmaNaoEncontradaException;
import br.com.inovadados.teacherplatform.repository.AvaliacaoRepository;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.MatriculaRepository;
import br.com.inovadados.teacherplatform.repository.RegistroFrequenciaRepository;
import br.com.inovadados.teacherplatform.repository.RespostaRepository;
import br.com.inovadados.teacherplatform.repository.TurmaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final TurmaRepository turmaRepository;
    private final MatriculaRepository matriculaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final EntregaRepository entregaRepository;
    private final RegistroFrequenciaRepository frequenciaRepository;
    private final RespostaRepository respostaRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardProfessorResponse getDashboardProfessor(UUID professorId) {
        List<Turma> turmas = turmaRepository.findByProfessorIdAndDeletadoEmIsNull(professorId);

        List<TurmaDashboardDto> turmasDtos = turmas.stream()
                .map(this::calcularMetricasTurma)
                .toList();

        List<AlertaDashboardDto> alertas = gerarAlertas(turmas);

        List<MediaHistoricaDto> mediasHistoricas = turmas.stream()
                .flatMap(t -> calcularMediasHistoricas(t).stream())
                .sorted(Comparator.comparing(MediaHistoricaDto::data))
                .toList();

        List<MapaCalorDto> mapaCalor = turmas.stream()
                .flatMap(t -> calcularMapaCalor(t).stream())
                .toList();

        return new DashboardProfessorResponse(alertas, turmasDtos, mediasHistoricas, mapaCalor);
    }

    public DesempenhoTurmaResponse getDesempenhoTurma(Long turmaId, UUID usuarioId) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new TurmaNaoEncontradaException(turmaId));

        boolean isAdmin = usuarioRepository.findById(usuarioId)
                .map(u -> u.getPerfil() == PerfilEnum.ADMIN || u.getPerfil() == PerfilEnum.COORDENADOR)
                .orElse(false);

        if (!isAdmin && !turma.getProfessor().getId().equals(usuarioId)) {
            throw new AcessoNegadoException("Acesso negado");
        }

        List<Matricula> matriculas = matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turmaId);
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turmaId);
        List<Long> avaliacaoIds = avaliacoes.stream().map(Avaliacao::getId).toList();
        List<Entrega> entregas = entregaRepository.findByAvaliacaoIdIn(avaliacaoIds).stream()
                .filter(e -> e.getNotaFinal() != null)
                .toList();

        Map<UUID, List<BigDecimal>> notasPorAluno = entregas.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getAluno().getId(),
                        Collectors.mapping(Entrega::getNotaFinal, Collectors.toList())
                ));

        Map<UUID, BigDecimal> mediaPorAluno = new HashMap<>();
        for (Matricula m : matriculas) {
            List<BigDecimal> notas = notasPorAluno.getOrDefault(m.getAluno().getId(), List.of());
            mediaPorAluno.put(m.getAluno().getId(), calcularMedia(notas));
        }

        List<BigDecimal> todasMedias = new ArrayList<>(mediaPorAluno.values());
        BigDecimal media = calcularMedia(todasMedias);
        BigDecimal maior = todasMedias.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        BigDecimal menor = todasMedias.stream().min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);

        long aprovados = mediaPorAluno.values().stream()
                .filter(m -> m.compareTo(new BigDecimal("5")) >= 0).count();
        double pctAprovacao = matriculas.isEmpty() ? 0 : (double) aprovados / matriculas.size() * 100;

        long totalAulas = frequenciaRepository.countAulasByTurmaId(turmaId);
        long totalPresencas = matriculas.stream()
                .mapToLong(m -> frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                        turmaId, m.getAluno().getId(), StatusFrequenciaEnum.PRESENTE))
                .sum();
        double pctFrequencia = (matriculas.isEmpty() || totalAulas == 0) ? 100 :
                (double) totalPresencas / (matriculas.size() * totalAulas) * 100;

        List<FaixaNotaDto> histograma = calcularHistograma(mediaPorAluno, matriculas);

        List<AlunoRankingDto> ranking = criarRanking(matriculas, mediaPorAluno, notasPorAluno);

        List<MediaHistoricaDto> linhaDoTempo = calcularMediasHistoricas(turma);

        return new DesempenhoTurmaResponse(
                turmaId, turma.getNome(), turma.getDisciplina(),
                media, maior, menor,
                round2(pctAprovacao), round2(pctFrequencia),
                histograma, ranking, linhaDoTempo
        );
    }

    public DesempenhoAlunoResponse getDesempenhoAluno(Long turmaId, UUID alunoId,
                                                       UUID usuarioId, PerfilEnum perfil) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new TurmaNaoEncontradaException(turmaId));

        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new AcessoNegadoException("Aluno não encontrado"));

        List<Avaliacao> avaliacoes = avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turmaId);
        List<Long> avaliacaoIds = avaliacoes.stream().map(Avaliacao::getId).toList();

        List<Entrega> entregas = entregaRepository.findByAlunoId(alunoId).stream()
                .filter(e -> avaliacaoIds.contains(e.getAvaliacao().getId()))
                .filter(e -> e.getNotaFinal() != null)
                .toList();

        BigDecimal mediaGeral = calcularMedia(entregas.stream().map(Entrega::getNotaFinal).toList());

        long totalAulas = frequenciaRepository.countAulasByTurmaId(turmaId);
        long presencas = frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                turmaId, alunoId, StatusFrequenciaEnum.PRESENTE);
        double pctFrequencia = totalAulas == 0 ? 100 : (double) presencas / totalAulas * 100;

        String situacao = determinarSituacao(mediaGeral, pctFrequencia);

        List<EvolucaoNotaDto> evolucao = calcularEvolucaoNotas(entregas, avaliacoes, avaliacaoIds);
        List<FrequenciaMensalDto> frequenciaMensal = calcularFrequenciaMensal(turmaId, alunoId);
        List<TopicoAcertoDto> topicos = calcularTopicos(entregas);

        return new DesempenhoAlunoResponse(
                alunoId, aluno.getNome(), situacao, mediaGeral,
                round2(pctFrequencia), evolucao, frequenciaMensal, topicos
        );
    }

    String calcularTendencia(List<BigDecimal> notas) {
        if (notas.size() < 2) return "STABLE";
        int metade = notas.size() / 2;
        BigDecimal mediaRecente = calcularMedia(notas.subList(metade, notas.size()));
        BigDecimal mediaAnterior = calcularMedia(notas.subList(0, metade));
        BigDecimal diff = mediaRecente.subtract(mediaAnterior);
        if (diff.compareTo(new BigDecimal("0.3")) > 0) return "UP";
        if (diff.compareTo(new BigDecimal("-0.3")) < 0) return "DOWN";
        return "STABLE";
    }

    int calcularPosicaoNaTurma(Long avaliacaoId, UUID alunoId) {
        List<Entrega> entregas = entregaRepository.findByAvaliacaoId(avaliacaoId).stream()
                .filter(e -> e.getNotaFinal() != null)
                .sorted(Comparator.comparing(Entrega::getNotaFinal).reversed())
                .toList();

        for (int i = 0; i < entregas.size(); i++) {
            if (entregas.get(i).getAluno().getId().equals(alunoId)) return i + 1;
        }
        return entregas.size() + 1;
    }

    private TurmaDashboardDto calcularMetricasTurma(Turma turma) {
        List<Matricula> matriculas = matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turma.getId());
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turma.getId());
        List<Long> avaliacaoIds = avaliacoes.stream().map(Avaliacao::getId).toList();

        List<Entrega> todasEntregas = entregaRepository.findByAvaliacaoIdIn(avaliacaoIds);
        List<BigDecimal> notas = todasEntregas.stream()
                .filter(e -> e.getNotaFinal() != null).map(Entrega::getNotaFinal).toList();

        long pendentesCorrecao = todasEntregas.stream()
                .filter(e -> e.getStatus() == StatusEntregaEnum.ENTREGUE && e.getNotaFinal() == null)
                .count();

        long totalAulas = frequenciaRepository.countAulasByTurmaId(turma.getId());
        long totalPresencas = matriculas.stream()
                .mapToLong(m -> frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                        turma.getId(), m.getAluno().getId(), StatusFrequenciaEnum.PRESENTE))
                .sum();
        double pctPresenca = (matriculas.isEmpty() || totalAulas == 0) ? 100 :
                (double) totalPresencas / (matriculas.size() * totalAulas) * 100;

        return new TurmaDashboardDto(
                turma.getId(), turma.getNome(), turma.getDisciplina(),
                matriculas.size(), calcularMedia(notas),
                BigDecimal.valueOf(pctPresenca).setScale(1, RoundingMode.HALF_UP),
                (int) pendentesCorrecao
        );
    }

    private List<AlertaDashboardDto> gerarAlertas(List<Turma> turmas) {
        List<AlertaDashboardDto> alertas = new ArrayList<>();

        for (Turma turma : turmas) {
            List<Matricula> matriculas = matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turma.getId());
            long totalAulas = frequenciaRepository.countAulasByTurmaId(turma.getId());

            for (Matricula m : matriculas) {
                if (totalAulas == 0) continue;
                long ausencias = frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                        turma.getId(), m.getAluno().getId(), StatusFrequenciaEnum.AUSENTE);
                double pctAusencia = (double) ausencias / totalAulas * 100;
                if (pctAusencia >= 25) {
                    alertas.add(new AlertaDashboardDto(
                            "RISCO_REPROVACAO",
                            m.getAluno().getNome() + " com " + String.format("%.0f", pctAusencia) + "% de faltas em " + turma.getNome(),
                            turma.getId(),
                            "/turmas/" + turma.getId() + "/alunos/" + m.getAluno().getId() + "/desempenho"
                    ));
                }
            }

            List<Avaliacao> avaliacoes = avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turma.getId());
            for (Avaliacao av : avaliacoes) {
                long pendentes = entregaRepository.findByAvaliacaoId(av.getId()).stream()
                        .filter(e -> e.getStatus() == StatusEntregaEnum.ENTREGUE && e.getNotaFinal() == null)
                        .count();
                if (pendentes > 0) {
                    alertas.add(new AlertaDashboardDto(
                            "ATIVIDADE_CORRIGIR",
                            av.getTitulo() + " — " + pendentes + " entrega(s) aguardando correção",
                            av.getId(),
                            "/avaliacoes/" + av.getId()
                    ));
                }
            }
        }

        return alertas;
    }

    private List<MediaHistoricaDto> calcularMediasHistoricas(Turma turma) {
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turma.getId());
        List<MediaHistoricaDto> resultado = new ArrayList<>();

        for (Avaliacao av : avaliacoes) {
            if (av.getDisponivelEm() == null) continue;
            List<BigDecimal> notas = entregaRepository.findByAvaliacaoId(av.getId()).stream()
                    .filter(e -> e.getNotaFinal() != null)
                    .map(Entrega::getNotaFinal).toList();
            if (!notas.isEmpty()) {
                resultado.add(new MediaHistoricaDto(
                        av.getId(), av.getTitulo(), av.getDisponivelEm(), calcularMedia(notas)));
            }
        }

        return resultado;
    }

    private List<MapaCalorDto> calcularMapaCalor(Turma turma) {
        List<Matricula> matriculas = matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turma.getId());
        Map<LocalDate, long[]> porDia = new HashMap<>();

        for (Matricula m : matriculas) {
            frequenciaRepository.findByTurmaIdAndAlunoId(turma.getId(), m.getAluno().getId())
                    .forEach(r -> {
                        long[] stats = porDia.computeIfAbsent(r.getDataAula(), k -> new long[2]);
                        stats[1]++;
                        if (r.getStatus() == StatusFrequenciaEnum.PRESENTE) stats[0]++;
                    });
        }

        return porDia.entrySet().stream()
                .map(e -> new MapaCalorDto(
                        e.getKey(), turma.getId(),
                        (int) e.getValue()[1], (int) e.getValue()[0],
                        e.getValue()[1] == 0 ? 0 : (double) e.getValue()[0] / e.getValue()[1] * 100
                ))
                .sorted(Comparator.comparing(MapaCalorDto::data))
                .toList();
    }

    private List<FaixaNotaDto> calcularHistograma(Map<UUID, BigDecimal> mediaPorAluno, List<Matricula> matriculas) {
        Map<UUID, String> nomeAluno = matriculas.stream()
                .collect(Collectors.toMap(m -> m.getAluno().getId(), m -> m.getAluno().getNome()));

        String[] faixas = {"0-2", "2-4", "4-6", "6-8", "8-10"};
        double[][] limites = {{0, 2}, {2, 4}, {4, 6}, {6, 8}, {8, 10}};
        List<FaixaNotaDto> resultado = new ArrayList<>();

        for (int i = 0; i < faixas.length; i++) {
            final double min = limites[i][0];
            final double max = limites[i][1];
            boolean isUltima = i == faixas.length - 1;
            List<String> nomes = mediaPorAluno.entrySet().stream()
                    .filter(e -> {
                        double v = e.getValue().doubleValue();
                        return isUltima ? v >= min && v <= max : v >= min && v < max;
                    })
                    .map(e -> nomeAluno.getOrDefault(e.getKey(), ""))
                    .filter(n -> !n.isEmpty())
                    .toList();
            resultado.add(new FaixaNotaDto(faixas[i], nomes.size(), nomes));
        }

        return resultado;
    }

    private List<AlunoRankingDto> criarRanking(List<Matricula> matriculas,
                                                Map<UUID, BigDecimal> mediaPorAluno,
                                                Map<UUID, List<BigDecimal>> notasPorAluno) {
        List<AlunoRankingDto> ranking = new ArrayList<>();
        List<Map.Entry<UUID, BigDecimal>> sorted = mediaPorAluno.entrySet().stream()
                .sorted(Map.Entry.<UUID, BigDecimal>comparingByValue().reversed())
                .toList();

        Map<UUID, String> nomeAluno = matriculas.stream()
                .collect(Collectors.toMap(m -> m.getAluno().getId(), m -> m.getAluno().getNome()));

        for (int i = 0; i < sorted.size(); i++) {
            UUID alunoId = sorted.get(i).getKey();
            List<BigDecimal> notas = notasPorAluno.getOrDefault(alunoId, List.of());
            ranking.add(new AlunoRankingDto(
                    i + 1, alunoId,
                    nomeAluno.getOrDefault(alunoId, ""),
                    sorted.get(i).getValue(),
                    calcularTendencia(notas)
            ));
        }

        return ranking;
    }

    private List<EvolucaoNotaDto> calcularEvolucaoNotas(List<Entrega> entregas,
                                                         List<Avaliacao> avaliacoes,
                                                         List<Long> avaliacaoIds) {
        Map<Long, Avaliacao> avMap = avaliacoes.stream()
                .collect(Collectors.toMap(Avaliacao::getId, a -> a));

        return entregas.stream()
                .filter(e -> avMap.containsKey(e.getAvaliacao().getId()))
                .map(e -> {
                    Avaliacao av = avMap.get(e.getAvaliacao().getId());
                    int total = (int) entregaRepository.findByAvaliacaoId(av.getId()).stream()
                            .filter(x -> x.getNotaFinal() != null).count();
                    int posicao = calcularPosicaoNaTurma(av.getId(), e.getAluno().getId());
                    return new EvolucaoNotaDto(
                            av.getId(), av.getTitulo(), av.getDisponivelEm(),
                            e.getNotaFinal(), posicao, total
                    );
                })
                .sorted(Comparator.comparing(EvolucaoNotaDto::data,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<FrequenciaMensalDto> calcularFrequenciaMensal(Long turmaId, UUID alunoId) {
        List<RegistroFrequencia> registros = frequenciaRepository
                .findByTurmaIdAndAlunoIdOrderByDataAulaAsc(turmaId, alunoId);

        Map<String, long[]> porMes = new HashMap<>();
        for (RegistroFrequencia r : registros) {
            String chave = r.getDataAula().getYear() + "-" + r.getDataAula().getMonthValue();
            long[] stats = porMes.computeIfAbsent(chave, k -> new long[2]);
            stats[1]++;
            if (r.getStatus() == StatusFrequenciaEnum.PRESENTE) stats[0]++;
        }

        return porMes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    String[] parts = e.getKey().split("-");
                    double pct = e.getValue()[1] == 0 ? 100 :
                            (double) e.getValue()[0] / e.getValue()[1] * 100;
                    return new FrequenciaMensalDto(
                            Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                            (int) e.getValue()[1], (int) e.getValue()[0], round2(pct));
                })
                .toList();
    }

    private List<TopicoAcertoDto> calcularTopicos(List<Entrega> entregas) {
        Map<String, int[]> topicos = new HashMap<>();
        for (Entrega e : entregas) {
            respostaRepository.findByEntregaId(e.getId()).forEach(r -> {
                String topico = r.getQuestao().getTopico();
                if (topico == null) return;
                int[] stats = topicos.computeIfAbsent(topico, k -> new int[2]);
                stats[1]++;
                if (Boolean.TRUE.equals(r.getCorreta())) stats[0]++;
            });
        }

        return topicos.entrySet().stream()
                .map(e -> {
                    double pct = e.getValue()[1] == 0 ? 0 :
                            (double) e.getValue()[0] / e.getValue()[1] * 100;
                    return new TopicoAcertoDto(e.getKey(), e.getValue()[0], e.getValue()[1], round2(pct));
                })
                .sorted(Comparator.comparing(TopicoAcertoDto::percentual))
                .toList();
    }

    private String determinarSituacao(BigDecimal media, double pctFrequencia) {
        if (pctFrequencia < 75) return "REPROVADO_POR_FALTA";
        if (media.compareTo(new BigDecimal("5")) < 0) return "EM_RISCO";
        return "APROVADO_EM_ANDAMENTO";
    }

    private BigDecimal calcularMedia(List<BigDecimal> notas) {
        if (notas.isEmpty()) return BigDecimal.ZERO;
        BigDecimal soma = notas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
