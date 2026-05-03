package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.RegistroFrequencia;
import br.com.inovadados.teacherplatform.domain.entity.Turma;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.StatusFrequenciaEnum;
import br.com.inovadados.teacherplatform.dto.request.FrequenciaAlunoDto;
import br.com.inovadados.teacherplatform.dto.request.LancarFrequenciaRequest;
import br.com.inovadados.teacherplatform.dto.response.FrequenciaResponse;
import br.com.inovadados.teacherplatform.dto.response.HistoricoFrequenciaResponse;
import br.com.inovadados.teacherplatform.exception.TurmaNaoEncontradaException;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.RegistroFrequenciaRepository;
import br.com.inovadados.teacherplatform.repository.TurmaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FrequenciaService {

    private final RegistroFrequenciaRepository registroFrequenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final AlertaFrequenciaService alertaFrequenciaService;

    @Transactional(readOnly = true)
    public Optional<FrequenciaResponse> buscarPorData(Long turmaId, LocalDate data) {
        List<RegistroFrequencia> registros = registroFrequenciaRepository
                .findAllByTurmaIdAndDataAulaOrderByAlunoId(turmaId, data);
        if (registros.isEmpty()) return Optional.empty();
        return Optional.of(toFrequenciaResponse(registros, data));
    }

    public FrequenciaResponse lancarFrequencia(Long turmaId, LancarFrequenciaRequest req, UUID lancadoPorId) {
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new TurmaNaoEncontradaException(turmaId));
        Usuario lancadoPor = usuarioRepository.findById(lancadoPorId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

        List<RegistroFrequencia> registros = new ArrayList<>();
        for (FrequenciaAlunoDto dto : req.alunos()) {
            Usuario aluno = usuarioRepository.findById(dto.alunoId())
                    .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado: " + dto.alunoId()));

            RegistroFrequencia registro = registroFrequenciaRepository
                    .findByTurmaIdAndAlunoIdAndDataAula(turmaId, dto.alunoId(), req.data())
                    .orElseGet(RegistroFrequencia::new);

            registro.setTurma(turma);
            registro.setAluno(aluno);
            registro.setDataAula(req.data());
            registro.setStatus(dto.status());
            registro.setObservacao(dto.observacao());
            registro.setLancadoPor(lancadoPor);

            if (registro.getId() == null) {
                registro.setLancadoEm(OffsetDateTime.now());
            } else {
                registro.setEditadoEm(OffsetDateTime.now());
            }

            registros.add(registroFrequenciaRepository.save(registro));
        }

        for (FrequenciaAlunoDto dto : req.alunos()) {
            dispararAlerta(turmaId, dto.alunoId());
        }

        return toFrequenciaResponse(registros, req.data());
    }

    public FrequenciaResponse editarFrequencia(Long turmaId, Long frequenciaId, LancarFrequenciaRequest req, UUID lancadoPorId) {
        RegistroFrequencia referencia = registroFrequenciaRepository.findById(frequenciaId)
                .orElseThrow(() -> new IllegalArgumentException("Frequência não encontrada: " + frequenciaId));
        LocalDate dataAula = referencia.getDataAula();

        Usuario lancadoPor = usuarioRepository.findById(lancadoPorId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

        List<RegistroFrequencia> registros = new ArrayList<>();
        for (FrequenciaAlunoDto dto : req.alunos()) {
            RegistroFrequencia registro = registroFrequenciaRepository
                    .findByTurmaIdAndAlunoIdAndDataAula(turmaId, dto.alunoId(), dataAula)
                    .orElseThrow(() -> new IllegalArgumentException("Registro não encontrado para aluno: " + dto.alunoId()));

            registro.setStatus(dto.status());
            registro.setObservacao(dto.observacao());
            registro.setLancadoPor(lancadoPor);
            registro.setEditadoEm(OffsetDateTime.now());
            registros.add(registroFrequenciaRepository.save(registro));
        }

        return toFrequenciaResponse(registros, dataAula);
    }

    @Transactional(readOnly = true)
    public HistoricoFrequenciaResponse buscarHistorico(Long turmaId, UUID alunoId) {
        List<RegistroFrequencia> registros = registroFrequenciaRepository
                .findByTurmaIdAndAlunoIdOrderByDataAulaAsc(turmaId, alunoId);
        long totalAulas = registroFrequenciaRepository.countAulasByTurmaId(turmaId);
        long totalPresencas = registroFrequenciaRepository
                .countByTurmaIdAndAlunoIdAndStatus(turmaId, alunoId, StatusFrequenciaEnum.PRESENTE);
        long totalFaltas = registros.size() - totalPresencas;
        double percentual = totalAulas > 0 ? (double) totalPresencas / totalAulas * 100 : 0.0;

        List<HistoricoFrequenciaResponse.DiaFrequenciaDto> calendario = registros.stream()
                .map(r -> new HistoricoFrequenciaResponse.DiaFrequenciaDto(
                        r.getDataAula(), r.getStatus(), r.getObservacao()))
                .toList();

        return new HistoricoFrequenciaResponse(
                Math.round(percentual * 10.0) / 10.0,
                (int) totalAulas,
                (int) totalPresencas,
                (int) totalFaltas,
                calendario
        );
    }

    private void dispararAlerta(Long turmaId, UUID alunoId) {
        long totalAulas = registroFrequenciaRepository.countAulasByTurmaId(turmaId);
        long totalPresencas = registroFrequenciaRepository
                .countByTurmaIdAndAlunoIdAndStatus(turmaId, alunoId, StatusFrequenciaEnum.PRESENTE);
        double percentual = totalAulas > 0 ? (double) totalPresencas / totalAulas * 100 : 100.0;

        List<RegistroFrequencia> historico = registroFrequenciaRepository
                .findByTurmaIdAndAlunoIdOrderByDataAulaAsc(turmaId, alunoId);
        boolean tresFaltasConsecutivas = verificarTresFaltasConsecutivas(historico);

        alertaFrequenciaService.verificarAlertas(turmaId, alunoId, percentual, tresFaltasConsecutivas);
    }

    private boolean verificarTresFaltasConsecutivas(List<RegistroFrequencia> historico) {
        int consecutivas = 0;
        for (RegistroFrequencia r : historico) {
            if (r.getStatus() == StatusFrequenciaEnum.AUSENTE) {
                consecutivas++;
                if (consecutivas >= 3) return true;
            } else {
                consecutivas = 0;
            }
        }
        return false;
    }

    private FrequenciaResponse toFrequenciaResponse(List<RegistroFrequencia> registros, LocalDate data) {
        Long id = registros.isEmpty() ? null : registros.get(0).getId();
        List<FrequenciaAlunoDto> alunos = registros.stream()
                .map(r -> new FrequenciaAlunoDto(r.getAluno().getId(), r.getStatus(), r.getObservacao()))
                .toList();
        return new FrequenciaResponse(id, data, alunos);
    }
}
