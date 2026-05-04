package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.domain.entity.Matricula;
import br.com.inovadados.teacherplatform.domain.entity.RegistroFrequencia;
import br.com.inovadados.teacherplatform.domain.entity.Turma;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.entity.VinculoResponsavel;
import br.com.inovadados.teacherplatform.domain.enums.StatusAvaliacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusFrequenciaEnum;
import br.com.inovadados.teacherplatform.dto.response.BoletimResponse;
import br.com.inovadados.teacherplatform.dto.response.CalendarioResponsavelResponse;
import br.com.inovadados.teacherplatform.dto.response.FrequenciaResponsavelResponse;
import br.com.inovadados.teacherplatform.dto.response.PainelResponsavelResponse;
import br.com.inovadados.teacherplatform.exception.AcessoNegadoException;
import br.com.inovadados.teacherplatform.repository.AvaliacaoRepository;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.MatriculaRepository;
import br.com.inovadados.teacherplatform.repository.RegistroFrequenciaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.repository.VinculoResponsavelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResponsavelService {

    private final VinculoResponsavelRepository vinculoResponsavelRepository;
    private final MatriculaRepository matriculaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final EntregaRepository entregaRepository;
    private final RegistroFrequenciaRepository frequenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public void verificarVinculo(UUID responsavelId, UUID alunoId) {
        boolean vinculado = vinculoResponsavelRepository.findByResponsavelId(responsavelId)
                .stream()
                .anyMatch(v -> v.getAluno().getId().equals(alunoId));
        if (!vinculado) {
            throw new AcessoNegadoException("Responsável não vinculado a este aluno");
        }
    }

    public List<PainelResponsavelResponse.AlunoResumoDto> listarAlunos(UUID responsavelId) {
        return vinculoResponsavelRepository.findByResponsavelId(responsavelId).stream()
                .map(v -> {
                    Usuario aluno = v.getAluno();
                    return new PainelResponsavelResponse.AlunoResumoDto(
                            aluno.getId(), aluno.getNome(), aluno.getAvatarUrl());
                })
                .toList();
    }

    public PainelResponsavelResponse getPainel(UUID responsavelId, UUID alunoId) {
        verificarVinculo(responsavelId, alunoId);

        Usuario aluno = usuarioRepository.findById(alunoId)
                .orElseThrow(() -> new AcessoNegadoException("Aluno não encontrado"));

        List<Matricula> matriculas = matriculaRepository.findByAlunoIdAndRemovidoEmIsNull(alunoId);
        List<Long> turmaIds = matriculas.stream().map(m -> m.getTurma().getId()).toList();

        List<Long> avaliacaoIds = avaliacaoRepository.findByTurmaIdIn(turmaIds).stream()
                .map(Avaliacao::getId).toList();

        List<BigDecimal> notas = entregaRepository.findByAlunoId(alunoId).stream()
                .filter(e -> avaliacaoIds.contains(e.getAvaliacao().getId()) && e.getNotaFinal() != null)
                .map(Entrega::getNotaFinal).toList();

        BigDecimal mediaGeral = calcularMedia(notas);

        long totalAulas = 0;
        long totalPresencas = 0;
        for (Matricula m : matriculas) {
            totalAulas += frequenciaRepository.countAulasByTurmaId(m.getTurma().getId());
            totalPresencas += frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                    m.getTurma().getId(), alunoId, StatusFrequenciaEnum.PRESENTE);
        }
        double percentualFrequencia = totalAulas == 0 ? 100.0 :
                Math.round((double) totalPresencas / totalAulas * 1000.0) / 10.0;

        PainelResponsavelResponse.AvaliacaoProximaDto proximaProva = avaliacaoRepository
                .findByTurmaIdIn(turmaIds).stream()
                .filter(av -> av.getStatus() == StatusAvaliacaoEnum.PUBLICADA
                        && av.getDisponivelEm() != null
                        && av.getDisponivelEm().isAfter(OffsetDateTime.now()))
                .min(Comparator.comparing(Avaliacao::getDisponivelEm))
                .map(av -> new PainelResponsavelResponse.AvaliacaoProximaDto(
                        av.getId(), av.getTitulo(),
                        av.getTurma().getDisciplina(), av.getDisponivelEm()))
                .orElse(null);

        List<PainelResponsavelResponse.AlertaDto> alertas = new ArrayList<>();
        if (percentualFrequencia < 75) {
            alertas.add(new PainelResponsavelResponse.AlertaDto(
                    "FREQUENCIA_BAIXA",
                    String.format("Frequência atual: %.1f%% (mínimo: 75%%)", percentualFrequencia)));
        }
        if (mediaGeral.compareTo(new BigDecimal("5")) < 0 && !notas.isEmpty()) {
            alertas.add(new PainelResponsavelResponse.AlertaDto(
                    "MEDIA_BAIXA",
                    String.format("Média atual: %.1f (abaixo da média de aprovação)", mediaGeral)));
        }

        return new PainelResponsavelResponse(
                new PainelResponsavelResponse.AlunoResumoDto(
                        aluno.getId(), aluno.getNome(), aluno.getAvatarUrl()),
                mediaGeral, percentualFrequencia, proximaProva, alertas);
    }

    public BoletimResponse getBoletim(UUID responsavelId, UUID alunoId, String periodo) {
        verificarVinculo(responsavelId, alunoId);

        List<Matricula> matriculas = matriculaRepository.findByAlunoIdAndRemovidoEmIsNull(alunoId);
        List<BoletimResponse.BoletimDisciplinaDto> disciplinas = new ArrayList<>();

        for (Matricula m : matriculas) {
            Turma turma = m.getTurma();
            List<Avaliacao> avaliacoes = avaliacaoRepository
                    .findByTurmaIdOrderByDisponivelEmAsc(turma.getId()).stream()
                    .filter(av -> av.getDisponivelEm() != null)
                    .toList();

            List<Long> avaliacaoIds = avaliacoes.stream().map(Avaliacao::getId).toList();
            List<Entrega> entregas = entregaRepository.findByAlunoId(alunoId).stream()
                    .filter(e -> avaliacaoIds.contains(e.getAvaliacao().getId())
                            && e.getNotaFinal() != null)
                    .toList();

            BigDecimal[] bimestres = new BigDecimal[4];
            for (Entrega e : entregas) {
                int bimestre = bimestreDoMes(e.getAvaliacao().getDisponivelEm().getMonthValue());
                if (bimestre >= 0 && bimestres[bimestre] == null) {
                    bimestres[bimestre] = e.getNotaFinal();
                }
            }

            List<BigDecimal> notasPresentes = new ArrayList<>();
            for (BigDecimal b : bimestres) {
                if (b != null) notasPresentes.add(b);
            }
            BigDecimal mediaFinal = calcularMedia(notasPresentes);

            String situacao;
            if (notasPresentes.isEmpty()) {
                situacao = "EM_ANDAMENTO";
            } else if (mediaFinal.compareTo(new BigDecimal("7")) >= 0) {
                situacao = "APROVADO";
            } else if (mediaFinal.compareTo(new BigDecimal("5")) >= 0) {
                situacao = "RECUPERACAO";
            } else {
                situacao = "REPROVADO";
            }

            disciplinas.add(new BoletimResponse.BoletimDisciplinaDto(
                    turma.getDisciplina(),
                    bimestres[0], bimestres[1], bimestres[2], bimestres[3],
                    mediaFinal, situacao));
        }

        return new BoletimResponse(periodo, disciplinas);
    }

    public FrequenciaResponsavelResponse getFrequencia(UUID responsavelId, UUID alunoId) {
        verificarVinculo(responsavelId, alunoId);

        List<Matricula> matriculas = matriculaRepository.findByAlunoIdAndRemovidoEmIsNull(alunoId);

        int totalAulas = 0;
        int totalPresencas = 0;
        List<FrequenciaResponsavelResponse.DiaFrequenciaDto> calendario = new ArrayList<>();
        List<FrequenciaResponsavelResponse.FaltaDto> faltas = new ArrayList<>();

        for (Matricula m : matriculas) {
            Turma turma = m.getTurma();
            long aulas = frequenciaRepository.countAulasByTurmaId(turma.getId());
            long presencas = frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                    turma.getId(), alunoId, StatusFrequenciaEnum.PRESENTE);
            totalAulas += (int) aulas;
            totalPresencas += (int) presencas;

            List<RegistroFrequencia> registros = frequenciaRepository
                    .findByTurmaIdAndAlunoIdOrderByDataAulaAsc(turma.getId(), alunoId);

            for (RegistroFrequencia r : registros) {
                calendario.add(new FrequenciaResponsavelResponse.DiaFrequenciaDto(
                        r.getDataAula(), r.getStatus(), turma.getDisciplina()));
                if (r.getStatus() == StatusFrequenciaEnum.AUSENTE) {
                    faltas.add(new FrequenciaResponsavelResponse.FaltaDto(
                            r.getDataAula(), turma.getDisciplina()));
                }
            }
        }

        calendario.sort(Comparator.comparing(FrequenciaResponsavelResponse.DiaFrequenciaDto::data));
        faltas.sort(Comparator.comparing(FrequenciaResponsavelResponse.FaltaDto::data));

        int totalFaltas = totalAulas - totalPresencas;
        double percentual = totalAulas == 0 ? 100.0 :
                Math.round((double) totalPresencas / totalAulas * 1000.0) / 10.0;

        return new FrequenciaResponsavelResponse(
                percentual, totalAulas, totalPresencas, totalFaltas, calendario, faltas);
    }

    public CalendarioResponsavelResponse getCalendario(UUID responsavelId, UUID alunoId) {
        verificarVinculo(responsavelId, alunoId);

        List<Matricula> matriculas = matriculaRepository.findByAlunoIdAndRemovidoEmIsNull(alunoId);
        List<Long> turmaIds = matriculas.stream().map(m -> m.getTurma().getId()).toList();

        OffsetDateTime agora = OffsetDateTime.now();

        List<Avaliacao> todasAvaliacoes = avaliacaoRepository.findByTurmaIdIn(turmaIds);

        List<CalendarioResponsavelResponse.ProvaFuturaDto> proximas = todasAvaliacoes.stream()
                .filter(av -> (av.getStatus() == StatusAvaliacaoEnum.PUBLICADA
                        || av.getStatus() == StatusAvaliacaoEnum.AGENDADA)
                        && av.getDisponivelEm() != null
                        && av.getDisponivelEm().isAfter(agora))
                .sorted(Comparator.comparing(Avaliacao::getDisponivelEm))
                .map(av -> new CalendarioResponsavelResponse.ProvaFuturaDto(
                        av.getId(), av.getTitulo(),
                        av.getTurma().getDisciplina(),
                        av.getTipo().name(),
                        av.getDisponivelEm(), av.getEncerraEm()))
                .toList();

        List<Long> avaliacaoIds = todasAvaliacoes.stream().map(Avaliacao::getId).toList();
        List<Entrega> entregas = entregaRepository.findByAlunoId(alunoId).stream()
                .filter(e -> avaliacaoIds.contains(e.getAvaliacao().getId()))
                .toList();

        List<CalendarioResponsavelResponse.ProvaHistoricoDto> historico = entregas.stream()
                .filter(e -> e.getAvaliacao().getStatus() == StatusAvaliacaoEnum.ENCERRADA)
                .sorted(Comparator.comparing(
                        e -> e.getAvaliacao().getDisponivelEm(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(e -> new CalendarioResponsavelResponse.ProvaHistoricoDto(
                        e.getAvaliacao().getId(),
                        e.getAvaliacao().getTitulo(),
                        e.getAvaliacao().getTurma().getDisciplina(),
                        e.getAvaliacao().getTipo().name(),
                        e.getAvaliacao().getDisponivelEm(),
                        e.getNotaFinal()))
                .toList();

        return new CalendarioResponsavelResponse(proximas, historico);
    }

    private int bimestreDoMes(int mes) {
        if (mes >= 2 && mes <= 4) return 0;
        if (mes >= 5 && mes <= 7) return 1;
        if (mes >= 8 && mes <= 10) return 2;
        return 3;
    }

    private BigDecimal calcularMedia(List<BigDecimal> notas) {
        if (notas.isEmpty()) return BigDecimal.ZERO;
        return notas.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);
    }
}
