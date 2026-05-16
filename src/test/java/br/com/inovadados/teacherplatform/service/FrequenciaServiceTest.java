package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.RegistroFrequencia;
import br.com.inovadados.teacherplatform.domain.entity.Turma;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.StatusFrequenciaEnum;
import br.com.inovadados.teacherplatform.dto.request.FrequenciaAlunoDto;
import br.com.inovadados.teacherplatform.dto.request.LancarFrequenciaRequest;
import br.com.inovadados.teacherplatform.dto.response.HistoricoFrequenciaResponse;
import br.com.inovadados.teacherplatform.exception.TurmaNaoEncontradaException;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.RegistroFrequenciaRepository;
import br.com.inovadados.teacherplatform.repository.TurmaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FrequenciaServiceTest {

    @Mock RegistroFrequenciaRepository registroFrequenciaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock TurmaRepository turmaRepository;
    @Mock AlertaFrequenciaService alertaFrequenciaService;

    @InjectMocks FrequenciaService frequenciaService;

    private static final Long TURMA_ID = 1L;
    private UUID alunoId;
    private UUID professorId;
    private Turma turma;
    private Usuario professor;
    private Usuario aluno;

    @BeforeEach
    void setUp() {
        alunoId = UUID.randomUUID();
        professorId = UUID.randomUUID();

        turma = new Turma();
        turma.setId(TURMA_ID);

        professor = new Usuario();
        professor.setId(professorId);

        aluno = new Usuario();
        aluno.setId(alunoId);
    }

    private RegistroFrequencia registro(StatusFrequenciaEnum status) {
        RegistroFrequencia r = new RegistroFrequencia();
        r.setStatus(status);
        r.setDataAula(LocalDate.now());
        r.setAluno(aluno);
        return r;
    }

    private List<RegistroFrequencia> historico(StatusFrequenciaEnum... statuses) {
        return List.of(statuses).stream().map(this::registro).toList();
    }

    private void configurarLancarFrequencia(List<RegistroFrequencia> historico, long totalAulas, long totalPresencas) {
        when(turmaRepository.findById(TURMA_ID)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.of(professor));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdAndDataAula(
                eq(TURMA_ID), eq(alunoId), any(LocalDate.class))).thenReturn(Optional.empty());
        when(registroFrequenciaRepository.save(any(RegistroFrequencia.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(registroFrequenciaRepository.countAulasByTurmaId(TURMA_ID)).thenReturn(totalAulas);
        when(registroFrequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                TURMA_ID, alunoId, StatusFrequenciaEnum.PRESENTE)).thenReturn(totalPresencas);
        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(TURMA_ID, alunoId))
            .thenReturn(historico);
    }

    // --- verificarTresFaltasConsecutivas (testado indiretamente) ---

    @Test
    void consecutivas_duasFaltas_naoAtiva() {
        List<RegistroFrequencia> hist = historico(
            StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.PRESENTE);
        configurarLancarFrequencia(hist, 5, 3);

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.PRESENTE, null)));
        frequenciaService.lancarFrequencia(TURMA_ID, req, professorId);

        verify(alertaFrequenciaService).verificarAlertas(eq(TURMA_ID), eq(alunoId), anyDouble(), eq(false));
    }

    @Test
    void consecutivas_exatamenteTres_ativa() {
        List<RegistroFrequencia> hist = historico(
            StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.AUSENTE);
        configurarLancarFrequencia(hist, 5, 2);

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.AUSENTE, null)));
        frequenciaService.lancarFrequencia(TURMA_ID, req, professorId);

        verify(alertaFrequenciaService).verificarAlertas(eq(TURMA_ID), eq(alunoId), anyDouble(), eq(true));
    }

    @Test
    void consecutivas_intercaladas_naoAtiva() {
        List<RegistroFrequencia> hist = historico(
            StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.PRESENTE,
            StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.AUSENTE);
        configurarLancarFrequencia(hist, 4, 1);

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.PRESENTE, null)));
        frequenciaService.lancarFrequencia(TURMA_ID, req, professorId);

        verify(alertaFrequenciaService).verificarAlertas(eq(TURMA_ID), eq(alunoId), anyDouble(), eq(false));
    }

    @Test
    void consecutivas_tresMeioDaLista_ativa() {
        List<RegistroFrequencia> hist = historico(
            StatusFrequenciaEnum.PRESENTE,
            StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.AUSENTE,
            StatusFrequenciaEnum.PRESENTE);
        configurarLancarFrequencia(hist, 5, 2);

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.PRESENTE, null)));
        frequenciaService.lancarFrequencia(TURMA_ID, req, professorId);

        verify(alertaFrequenciaService).verificarAlertas(eq(TURMA_ID), eq(alunoId), anyDouble(), eq(true));
    }

    @Test
    void consecutivas_quatroSeguidas_ativa() {
        List<RegistroFrequencia> hist = historico(
            StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.AUSENTE,
            StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.AUSENTE);
        configurarLancarFrequencia(hist, 4, 0);

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.AUSENTE, null)));
        frequenciaService.lancarFrequencia(TURMA_ID, req, professorId);

        verify(alertaFrequenciaService).verificarAlertas(eq(TURMA_ID), eq(alunoId), anyDouble(), eq(true));
    }

    @Test
    void consecutivas_listaVazia_naoAtiva() {
        configurarLancarFrequencia(List.of(), 0, 0);

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.PRESENTE, null)));
        frequenciaService.lancarFrequencia(TURMA_ID, req, professorId);

        verify(alertaFrequenciaService).verificarAlertas(eq(TURMA_ID), eq(alunoId), anyDouble(), eq(false));
    }

    @Test
    void consecutivas_duasSequenciasDe2_naoAtiva() {
        List<RegistroFrequencia> hist = historico(
            StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.AUSENTE,
            StatusFrequenciaEnum.PRESENTE,
            StatusFrequenciaEnum.AUSENTE, StatusFrequenciaEnum.AUSENTE);
        configurarLancarFrequencia(hist, 5, 1);

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.PRESENTE, null)));
        frequenciaService.lancarFrequencia(TURMA_ID, req, professorId);

        verify(alertaFrequenciaService).verificarAlertas(eq(TURMA_ID), eq(alunoId), anyDouble(), eq(false));
    }

    // --- buscarHistorico: percentual ---

    @Test
    void historico_10De10Aulas_percentualCem() {
        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(TURMA_ID, alunoId))
            .thenReturn(List.of());
        when(registroFrequenciaRepository.countAulasByTurmaId(TURMA_ID)).thenReturn(10L);
        when(registroFrequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                TURMA_ID, alunoId, StatusFrequenciaEnum.PRESENTE)).thenReturn(10L);

        HistoricoFrequenciaResponse resp = frequenciaService.buscarHistorico(TURMA_ID, alunoId);

        assertEquals(100.0, resp.percentualPresenca());
    }

    @Test
    void historico_3De4Aulas_percentual75() {
        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(TURMA_ID, alunoId))
            .thenReturn(List.of());
        when(registroFrequenciaRepository.countAulasByTurmaId(TURMA_ID)).thenReturn(4L);
        when(registroFrequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                TURMA_ID, alunoId, StatusFrequenciaEnum.PRESENTE)).thenReturn(3L);

        HistoricoFrequenciaResponse resp = frequenciaService.buscarHistorico(TURMA_ID, alunoId);

        assertEquals(75.0, resp.percentualPresenca());
    }

    @Test
    void historico_0De5Aulas_percentualZero() {
        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(TURMA_ID, alunoId))
            .thenReturn(List.of());
        when(registroFrequenciaRepository.countAulasByTurmaId(TURMA_ID)).thenReturn(5L);
        when(registroFrequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                TURMA_ID, alunoId, StatusFrequenciaEnum.PRESENTE)).thenReturn(0L);

        HistoricoFrequenciaResponse resp = frequenciaService.buscarHistorico(TURMA_ID, alunoId);

        assertEquals(0.0, resp.percentualPresenca());
    }

    @Test
    void historico_0Aulas_semDivisaoPorZero() {
        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(TURMA_ID, alunoId))
            .thenReturn(List.of());
        when(registroFrequenciaRepository.countAulasByTurmaId(TURMA_ID)).thenReturn(0L);
        when(registroFrequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                TURMA_ID, alunoId, StatusFrequenciaEnum.PRESENTE)).thenReturn(0L);

        assertDoesNotThrow(() -> frequenciaService.buscarHistorico(TURMA_ID, alunoId));
        HistoricoFrequenciaResponse resp = frequenciaService.buscarHistorico(TURMA_ID, alunoId);
        assertEquals(0.0, resp.percentualPresenca());
    }

    @Test
    void historico_2De3Aulas_percentual667() {
        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(TURMA_ID, alunoId))
            .thenReturn(List.of());
        when(registroFrequenciaRepository.countAulasByTurmaId(TURMA_ID)).thenReturn(3L);
        when(registroFrequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                TURMA_ID, alunoId, StatusFrequenciaEnum.PRESENTE)).thenReturn(2L);

        HistoricoFrequenciaResponse resp = frequenciaService.buscarHistorico(TURMA_ID, alunoId);

        assertEquals(66.7, resp.percentualPresenca());
    }

    @Test
    void historico_totalFaltasCalculadoCorretamente() {
        RegistroFrequencia r1 = registro(StatusFrequenciaEnum.PRESENTE);
        RegistroFrequencia r2 = registro(StatusFrequenciaEnum.AUSENTE);
        RegistroFrequencia r3 = registro(StatusFrequenciaEnum.AUSENTE);

        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(TURMA_ID, alunoId))
            .thenReturn(List.of(r1, r2, r3));
        when(registroFrequenciaRepository.countAulasByTurmaId(TURMA_ID)).thenReturn(3L);
        when(registroFrequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                TURMA_ID, alunoId, StatusFrequenciaEnum.PRESENTE)).thenReturn(1L);

        HistoricoFrequenciaResponse resp = frequenciaService.buscarHistorico(TURMA_ID, alunoId);

        assertEquals(2, resp.totalFaltas());
        assertEquals(1, resp.totalPresencas());
    }

    // --- lancarFrequencia: erros ---

    @Test
    void lancarFrequencia_turmaNaoEncontrada_deveLancarTurmaNaoEncontradaException() {
        when(turmaRepository.findById(TURMA_ID)).thenReturn(Optional.empty());

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.PRESENTE, null)));

        assertThrows(TurmaNaoEncontradaException.class,
            () -> frequenciaService.lancarFrequencia(TURMA_ID, req, professorId));
    }

    @Test
    void lancarFrequencia_usuarioNaoEncontrado_deveLancarUnauthorizedException() {
        when(turmaRepository.findById(TURMA_ID)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.empty());

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.PRESENTE, null)));

        assertThrows(UnauthorizedException.class,
            () -> frequenciaService.lancarFrequencia(TURMA_ID, req, professorId));
    }

    @Test
    void lancarFrequencia_alunoNaoEncontrado_deveLancarIllegalArgumentException() {
        when(turmaRepository.findById(TURMA_ID)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.of(professor));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.empty());

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.PRESENTE, null)));

        assertThrows(IllegalArgumentException.class,
            () -> frequenciaService.lancarFrequencia(TURMA_ID, req, professorId));
    }

    @Test
    void lancarFrequencia_novoRegistro_lancadoEmPreenchido() {
        configurarLancarFrequencia(List.of(), 1, 1);

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.PRESENTE, null)));
        frequenciaService.lancarFrequencia(TURMA_ID, req, professorId);

        ArgumentCaptor<RegistroFrequencia> captor = ArgumentCaptor.forClass(RegistroFrequencia.class);
        verify(registroFrequenciaRepository).save(captor.capture());
        assertNotNull(captor.getValue().getLancadoEm());
        assertNull(captor.getValue().getEditadoEm());
    }

    @Test
    void lancarFrequencia_registroExistente_editadoEmAtualizado() {
        RegistroFrequencia existente = new RegistroFrequencia();
        existente.setId(1L);
        existente.setStatus(StatusFrequenciaEnum.PRESENTE);

        when(turmaRepository.findById(TURMA_ID)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.of(professor));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdAndDataAula(
                eq(TURMA_ID), eq(alunoId), any(LocalDate.class))).thenReturn(Optional.of(existente));
        when(registroFrequenciaRepository.save(any(RegistroFrequencia.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(registroFrequenciaRepository.countAulasByTurmaId(TURMA_ID)).thenReturn(3L);
        when(registroFrequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                TURMA_ID, alunoId, StatusFrequenciaEnum.PRESENTE)).thenReturn(2L);
        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(TURMA_ID, alunoId))
            .thenReturn(List.of());

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(),
            List.of(new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.AUSENTE, null)));
        frequenciaService.lancarFrequencia(TURMA_ID, req, professorId);

        ArgumentCaptor<RegistroFrequencia> captor = ArgumentCaptor.forClass(RegistroFrequencia.class);
        verify(registroFrequenciaRepository).save(captor.capture());
        assertNotNull(captor.getValue().getEditadoEm());
    }

    @Test
    void lancarFrequencia_chamadoVerificarAlertasPorAluno() {
        configurarLancarFrequencia(List.of(), 5, 5);

        UUID aluno2Id = UUID.randomUUID();
        Usuario aluno2 = new Usuario();
        aluno2.setId(aluno2Id);

        when(usuarioRepository.findById(aluno2Id)).thenReturn(Optional.of(aluno2));
        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdAndDataAula(
                eq(TURMA_ID), eq(aluno2Id), any(LocalDate.class))).thenReturn(Optional.empty());
        when(registroFrequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(
                TURMA_ID, aluno2Id, StatusFrequenciaEnum.PRESENTE)).thenReturn(5L);
        when(registroFrequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(TURMA_ID, aluno2Id))
            .thenReturn(List.of());

        LancarFrequenciaRequest req = new LancarFrequenciaRequest(LocalDate.now(), List.of(
            new FrequenciaAlunoDto(alunoId, StatusFrequenciaEnum.PRESENTE, null),
            new FrequenciaAlunoDto(aluno2Id, StatusFrequenciaEnum.AUSENTE, null)
        ));

        frequenciaService.lancarFrequencia(TURMA_ID, req, professorId);

        verify(alertaFrequenciaService, times(2)).verificarAlertas(eq(TURMA_ID), any(UUID.class), anyDouble(), anyBoolean());
    }
}
