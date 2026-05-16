package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Flashcard;
import br.com.inovadados.teacherplatform.domain.entity.FlashcardEstadoSm2;
import br.com.inovadados.teacherplatform.domain.entity.Matricula;
import br.com.inovadados.teacherplatform.domain.entity.ProgressoFlashcard;
import br.com.inovadados.teacherplatform.domain.entity.Turma;
import br.com.inovadados.teacherplatform.dto.response.FlashcardResponse;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.ResultadoFlashcardEnum;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.FlashcardEstadoSm2Repository;
import br.com.inovadados.teacherplatform.repository.FlashcardRepository;
import br.com.inovadados.teacherplatform.repository.MatriculaRepository;
import br.com.inovadados.teacherplatform.repository.ProgressoFlashcardRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock FlashcardRepository flashcardRepository;
    @Mock FlashcardEstadoSm2Repository estadoSm2Repository;
    @Mock ProgressoFlashcardRepository progressoRepository;
    @Mock MatriculaRepository matriculaRepository;
    @Mock UsuarioRepository usuarioRepository;

    @InjectMocks FlashcardService flashcardService;

    private UUID alunoId;
    private Flashcard flashcard;
    private Usuario aluno;

    @BeforeEach
    void setUp() {
        alunoId = UUID.randomUUID();
        flashcard = new Flashcard();
        flashcard.setId(1L);

        aluno = new Usuario();
        aluno.setId(alunoId);
    }

    private FlashcardEstadoSm2 estadoComFator(double fator, int intervalo) {
        FlashcardEstadoSm2 e = new FlashcardEstadoSm2();
        e.setFatorFacilidade(new BigDecimal(String.valueOf(fator)));
        e.setIntervaloDias(intervalo);
        e.setTotalRevisoes(0);
        e.setFlashcard(flashcard);
        e.setAlunoId(alunoId);
        return e;
    }

    private FlashcardEstadoSm2 capturarEstadoSalvo() {
        ArgumentCaptor<FlashcardEstadoSm2> captor = ArgumentCaptor.forClass(FlashcardEstadoSm2.class);
        verify(estadoSm2Repository).save(captor.capture());
        return captor.getValue();
    }

    private void configurarMocksParaRegistrar(FlashcardEstadoSm2 estado) {
        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));
        when(estadoSm2Repository.findByAlunoIdAndFlashcardId(alunoId, 1L)).thenReturn(Optional.of(estado));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
    }

    // --- SM-2: sabia=true ---

    @Test
    void sm2_sabiaTrue_fator25_intervalo1_deveResultarFator26_intervalo3() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 1);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, true);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(0, new BigDecimal("2.6").compareTo(salvo.getFatorFacilidade()));
        assertEquals(3, salvo.getIntervaloDias());
    }

    @Test
    void sm2_sabiaTrue_fator25_intervalo3_deveResultarFator26_intervalo8() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 3);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, true);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(0, new BigDecimal("2.6").compareTo(salvo.getFatorFacilidade()));
        assertEquals(8, salvo.getIntervaloDias());
    }

    @Test
    void sm2_sabiaTrue_fator25_intervalo8_deveResultarFator26_intervalo21() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 8);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, true);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(0, new BigDecimal("2.6").compareTo(salvo.getFatorFacilidade()));
        assertEquals(21, salvo.getIntervaloDias());
    }

    @Test
    void sm2_sabiaTrue_fator30_intervalo10_deveResultarFator31_intervalo31() {
        FlashcardEstadoSm2 estado = estadoComFator(3.0, 10);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, true);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(0, new BigDecimal("3.1").compareTo(salvo.getFatorFacilidade()));
        assertEquals(31, salvo.getIntervaloDias());
    }

    // --- SM-2: sabia=false ---

    @Test
    void sm2_sabiaFalse_fator25_intervalo10_deveResultarFator23_intervalo1() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 10);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, false);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(0, new BigDecimal("2.3").compareTo(salvo.getFatorFacilidade()));
        assertEquals(1, salvo.getIntervaloDias());
    }

    @Test
    void sm2_sabiaFalse_fator15_deveFicarFixoEm13() {
        FlashcardEstadoSm2 estado = estadoComFator(1.5, 5);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, false);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(0, new BigDecimal("1.3").compareTo(salvo.getFatorFacilidade()));
        assertEquals(1, salvo.getIntervaloDias());
    }

    @Test
    void sm2_sabiaFalse_fator13_devePermanecerEm13() {
        FlashcardEstadoSm2 estado = estadoComFator(1.3, 3);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, false);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(0, new BigDecimal("1.3").compareTo(salvo.getFatorFacilidade()));
    }

    @Test
    void sm2_sabiaFalse_fator14_deveFicarFixoEm13() {
        FlashcardEstadoSm2 estado = estadoComFator(1.4, 3);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, false);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(0, new BigDecimal("1.3").compareTo(salvo.getFatorFacilidade()));
    }

    // --- totalRevisoes ---

    @Test
    void sm2_totalRevisoes_deveIncrementarDe0Para1() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 1);
        estado.setTotalRevisoes(0);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, true);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(1, salvo.getTotalRevisoes());
    }

    @Test
    void sm2_totalRevisoes_deveIncrementarDe5Para6() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 1);
        estado.setTotalRevisoes(5);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, false);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(6, salvo.getTotalRevisoes());
    }

    // --- proximaRevisao ---

    @Test
    void sm2_sabiaTrue_proximaRevisaoDeveSerHojeMaisIntervalo() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 1);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, true);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(LocalDate.now().plusDays(3), salvo.getProximaRevisao());
    }

    @Test
    void sm2_sabiaFalse_proximaRevisaoDeveSerHojeMais1() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 10);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, false);

        FlashcardEstadoSm2 salvo = capturarEstadoSalvo();
        assertEquals(LocalDate.now().plusDays(1), salvo.getProximaRevisao());
    }

    // --- Erros ---

    @Test
    void registrarAvaliacao_flashcardInexistente_deveLancarUnauthorizedException() {
        when(flashcardRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> flashcardService.registrarAvaliacao(alunoId, 999L, true));
    }

    @Test
    void registrarAvaliacao_alunoNaoEncontradoNoHistorico_deveLancarUnauthorizedException() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 1);
        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));
        when(estadoSm2Repository.findByAlunoIdAndFlashcardId(alunoId, 1L)).thenReturn(Optional.of(estado));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> flashcardService.registrarAvaliacao(alunoId, 1L, true));
    }

    // --- Estado novo vs existente ---

    @Test
    void registrarAvaliacao_estadoInexistente_deveCriarNovoComAlunoIdEFlashcard() {
        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));
        when(estadoSm2Repository.findByAlunoIdAndFlashcardId(alunoId, 1L)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(aluno));

        flashcardService.registrarAvaliacao(alunoId, 1L, true);

        ArgumentCaptor<FlashcardEstadoSm2> captor = ArgumentCaptor.forClass(FlashcardEstadoSm2.class);
        verify(estadoSm2Repository).save(captor.capture());
        assertEquals(alunoId, captor.getValue().getAlunoId());
        assertEquals(flashcard, captor.getValue().getFlashcard());
    }

    @Test
    void registrarAvaliacao_estadoExistente_deveAtualizarMesmoObjeto() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 1);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, true);

        verify(estadoSm2Repository, times(1)).save(estado);
    }

    // --- Progresso ---

    @Test
    void registrarAvaliacao_sabiaTrue_deveSalvarProgressoComResultadoFACIL() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 1);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, true);

        ArgumentCaptor<ProgressoFlashcard> captor = ArgumentCaptor.forClass(ProgressoFlashcard.class);
        verify(progressoRepository).save(captor.capture());
        assertEquals(ResultadoFlashcardEnum.FACIL, captor.getValue().getResultado());
    }

    @Test
    void registrarAvaliacao_sabiaFalse_deveSalvarProgressoComResultadoDIFICIL() {
        FlashcardEstadoSm2 estado = estadoComFator(2.5, 1);
        configurarMocksParaRegistrar(estado);

        flashcardService.registrarAvaliacao(alunoId, 1L, false);

        ArgumentCaptor<ProgressoFlashcard> captor = ArgumentCaptor.forClass(ProgressoFlashcard.class);
        verify(progressoRepository).save(captor.capture());
        assertEquals(ResultadoFlashcardEnum.DIFICIL, captor.getValue().getResultado());
    }

    // --- getFlashcardsPriorizados ---

    private FlashcardEstadoSm2 estadoParaTurma(Long turmaId, String pergunta, String resposta) {
        Turma turma = new Turma();
        turma.setId(turmaId);
        turma.setDisciplina("Matemática");

        Flashcard fc = new Flashcard();
        fc.setId(turmaId * 10);
        fc.setPergunta(pergunta);
        fc.setResposta(resposta);
        fc.setTurma(turma);

        FlashcardEstadoSm2 estado = new FlashcardEstadoSm2();
        estado.setFlashcard(fc);
        estado.setAlunoId(alunoId);
        return estado;
    }

    @Test
    void getFlashcardsPriorizados_comTurmaId_retornaApenasFlashcardsDaTurma() {
        FlashcardEstadoSm2 estadoTurma1 = estadoParaTurma(1L, "P1", "R1");
        FlashcardEstadoSm2 estadoTurma2 = estadoParaTurma(2L, "P2", "R2");

        when(estadoSm2Repository.findByAlunoIdAndProximaRevisaoLessThanEqualOrderByProximaRevisaoAsc(
                eq(alunoId), any(LocalDate.class)))
            .thenReturn(List.of(estadoTurma1, estadoTurma2));

        List<FlashcardResponse> resultado = flashcardService.getFlashcardsPriorizados(alunoId, 1L);

        assertEquals(1, resultado.size());
        assertEquals("P1", resultado.get(0).pergunta());
    }

    @Test
    void getFlashcardsPriorizados_comTurmaIdNull_buscaTurmasViaMatricula() {
        Turma turma = new Turma();
        turma.setId(3L);
        turma.setDisciplina("Português");

        Matricula matricula = new Matricula();
        matricula.setTurma(turma);

        FlashcardEstadoSm2 estado = estadoParaTurma(3L, "PQ", "RQ");

        when(estadoSm2Repository.findByAlunoIdAndProximaRevisaoLessThanEqualOrderByProximaRevisaoAsc(
                eq(alunoId), any(LocalDate.class)))
            .thenReturn(List.of(estado));
        when(matriculaRepository.findByAlunoIdAndRemovidoEmIsNull(alunoId))
            .thenReturn(List.of(matricula));

        List<FlashcardResponse> resultado = flashcardService.getFlashcardsPriorizados(alunoId, null);

        assertEquals(1, resultado.size());
        verify(matriculaRepository).findByAlunoIdAndRemovidoEmIsNull(alunoId);
    }

    @Test
    void getFlashcardsPriorizados_comTurmaIdNull_retornaFlashcardsDasMatriculas() {
        Turma turma = new Turma();
        turma.setId(5L);
        turma.setDisciplina("Ciências");

        Matricula matricula = new Matricula();
        matricula.setTurma(turma);

        FlashcardEstadoSm2 estadoCerto = estadoParaTurma(5L, "Pergunta5", "Resp5");
        FlashcardEstadoSm2 estadoErrado = estadoParaTurma(6L, "Pergunta6", "Resp6");

        when(estadoSm2Repository.findByAlunoIdAndProximaRevisaoLessThanEqualOrderByProximaRevisaoAsc(
                eq(alunoId), any(LocalDate.class)))
            .thenReturn(List.of(estadoCerto, estadoErrado));
        when(matriculaRepository.findByAlunoIdAndRemovidoEmIsNull(alunoId))
            .thenReturn(List.of(matricula));

        List<FlashcardResponse> resultado = flashcardService.getFlashcardsPriorizados(alunoId, null);

        assertEquals(1, resultado.size());
        assertEquals("Pergunta5", resultado.get(0).pergunta());
    }

    @Test
    void getFlashcardsPriorizados_semEstadosPendentes_retornaListaVazia() {
        when(estadoSm2Repository.findByAlunoIdAndProximaRevisaoLessThanEqualOrderByProximaRevisaoAsc(
                eq(alunoId), any(LocalDate.class)))
            .thenReturn(List.of());

        List<FlashcardResponse> resultado = flashcardService.getFlashcardsPriorizados(alunoId, 1L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void getFlashcardsPriorizados_mapeamentoCorreto_retornaFlashcardResponseComCamposCorretos() {
        FlashcardEstadoSm2 estado = estadoParaTurma(1L, "Pergunta X", "Resposta Y");
        estado.getFlashcard().getTurma().setDisciplina("Física");

        when(estadoSm2Repository.findByAlunoIdAndProximaRevisaoLessThanEqualOrderByProximaRevisaoAsc(
                eq(alunoId), any(LocalDate.class)))
            .thenReturn(List.of(estado));

        List<FlashcardResponse> resultado = flashcardService.getFlashcardsPriorizados(alunoId, 1L);

        assertEquals(1, resultado.size());
        FlashcardResponse response = resultado.get(0);
        assertEquals("Pergunta X", response.pergunta());
        assertEquals("Resposta Y", response.resposta());
        assertEquals("Física", response.topico());
    }
}
