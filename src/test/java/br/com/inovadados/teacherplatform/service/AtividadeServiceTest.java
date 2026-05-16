package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.domain.entity.Questao;
import br.com.inovadados.teacherplatform.domain.entity.Resposta;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.dto.request.SalvarRascunhoAtividadeRequest;
import br.com.inovadados.teacherplatform.dto.response.EntregarAtividadeResponse;
import br.com.inovadados.teacherplatform.exception.AvaliacaoNaoEncontradaException;
import br.com.inovadados.teacherplatform.exception.OperacaoNaoPermitidaException;
import br.com.inovadados.teacherplatform.domain.entity.Turma;
import br.com.inovadados.teacherplatform.domain.enums.GabaritoLiberacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusAvaliacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import br.com.inovadados.teacherplatform.domain.enums.TipoQuestaoEnum;
import br.com.inovadados.teacherplatform.repository.AvaliacaoRepository;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.QuestaoRepository;
import br.com.inovadados.teacherplatform.repository.RespostaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtividadeServiceTest {

    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock EntregaRepository entregaRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock RespostaRepository respostaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ObjectMapper objectMapper;

    @InjectMocks AtividadeService atividadeService;

    private UUID alunoId;
    private Avaliacao avaliacao;

    @BeforeEach
    void setUp() {
        alunoId = UUID.randomUUID();

        Turma turma = new Turma();
        turma.setId(1L);
        turma.setDisciplina("Matemática");

        avaliacao = new Avaliacao();
        avaliacao.setId(1L);
        avaliacao.setTitulo("Prova 1");
        avaliacao.setTurma(turma);
        avaliacao.setStatus(StatusAvaliacaoEnum.PUBLICADA);
        avaliacao.setGabaritoLiberacao(GabaritoLiberacaoEnum.IMEDIATA);
    }

    private Questao questaoMC(Long id, int gabaritoIndice, String pontos) {
        Questao q = new Questao();
        q.setId(id);
        q.setTipo(TipoQuestaoEnum.MULTIPLA_ESCOLHA);
        q.setGabaritoIndice(gabaritoIndice);
        q.setPontos(new BigDecimal(pontos));
        return q;
    }

    private Questao questaoDissertativa(Long id, String pontos) {
        Questao q = new Questao();
        q.setId(id);
        q.setTipo(TipoQuestaoEnum.DISSERTATIVA);
        q.setPontos(new BigDecimal(pontos));
        return q;
    }

    private Questao questaoUpload(Long id, String pontos) {
        Questao q = new Questao();
        q.setId(id);
        q.setTipo(TipoQuestaoEnum.UPLOAD_ARQUIVO);
        q.setPontos(new BigDecimal(pontos));
        return q;
    }

    // --- calcularNotaAutomatica ---

    @Test
    void calcularNota_todasCorretas_retorna10() {
        Questao q1 = questaoMC(1L, 0, "5");
        Questao q2 = questaoMC(2L, 1, "5");
        Map<Long, Object> respostas = Map.of(1L, 0, 2L, 1);

        BigDecimal nota = atividadeService.calcularNotaAutomatica(1L, List.of(q1, q2), respostas);

        assertEquals(0, new BigDecimal("10.00").compareTo(nota));
    }

    @Test
    void calcularNota_nenhumaCorreta_retorna0() {
        Questao q1 = questaoMC(1L, 0, "5");
        Questao q2 = questaoMC(2L, 1, "5");
        Map<Long, Object> respostas = Map.of(1L, 1, 2L, 0);

        BigDecimal nota = atividadeService.calcularNotaAutomatica(1L, List.of(q1, q2), respostas);

        assertEquals(0, new BigDecimal("0.00").compareTo(nota));
    }

    @Test
    void calcularNota_metadeCorretaPesosIguais_retorna5() {
        Questao q1 = questaoMC(1L, 0, "5");
        Questao q2 = questaoMC(2L, 1, "5");
        Map<Long, Object> respostas = Map.of(1L, 0, 2L, 0);

        BigDecimal nota = atividadeService.calcularNotaAutomatica(1L, List.of(q1, q2), respostas);

        assertEquals(0, new BigDecimal("5.00").compareTo(nota));
    }

    @Test
    void calcularNota_dissertativaIgnoradaNoCalculo() {
        Questao mc = questaoMC(1L, 0, "10");
        Questao diss = questaoDissertativa(2L, "5");
        Map<Long, Object> respostas = Map.of(1L, 0);

        BigDecimal nota = atividadeService.calcularNotaAutomatica(1L, List.of(mc, diss), respostas);

        assertEquals(0, new BigDecimal("10.00").compareTo(nota));
    }

    @Test
    void calcularNota_apenasDissertatitvas_retorna0SemDivisaoPorZero() {
        Questao diss1 = questaoDissertativa(1L, "5");
        Questao diss2 = questaoDissertativa(2L, "5");

        assertDoesNotThrow(() -> atividadeService.calcularNotaAutomatica(1L, List.of(diss1, diss2), Map.of()));
        BigDecimal nota = atividadeService.calcularNotaAutomatica(1L, List.of(diss1, diss2), Map.of());
        assertEquals(0, BigDecimal.ZERO.compareTo(nota));
    }

    @Test
    void calcularNota_questaoSemGabaritoIndice_naoPontua() {
        Questao q = new Questao();
        q.setId(1L);
        q.setTipo(TipoQuestaoEnum.MULTIPLA_ESCOLHA);
        q.setGabaritoIndice(null);
        q.setPontos(new BigDecimal("10"));
        Map<Long, Object> respostas = Map.of(1L, 0);

        BigDecimal nota = atividadeService.calcularNotaAutomatica(1L, List.of(q), respostas);

        assertEquals(0, new BigDecimal("0.00").compareTo(nota));
    }

    @Test
    void calcularNota_respostaAusente_naoPontua() {
        Questao q = questaoMC(1L, 0, "10");
        Map<Long, Object> respostas = new HashMap<>();

        BigDecimal nota = atividadeService.calcularNotaAutomatica(1L, List.of(q), respostas);

        assertEquals(0, new BigDecimal("0.00").compareTo(nota));
    }

    @Test
    void calcularNota_1De3Questoes_retorna333() {
        Questao q1 = questaoMC(1L, 0, "1");
        Questao q2 = questaoMC(2L, 1, "1");
        Questao q3 = questaoMC(3L, 2, "1");
        Map<Long, Object> respostas = Map.of(1L, 0, 2L, 0, 3L, 0);

        BigDecimal nota = atividadeService.calcularNotaAutomatica(1L, List.of(q1, q2, q3), respostas);

        assertEquals(0, new BigDecimal("3.33").compareTo(nota));
    }

    @Test
    void calcularNota_pesosdiferentes_calculaCorretamente() {
        Questao q1 = questaoMC(1L, 0, "3");
        Questao q2 = questaoMC(2L, 1, "7");
        Map<Long, Object> respostas = Map.of(1L, 0, 2L, 0);

        BigDecimal nota = atividadeService.calcularNotaAutomatica(1L, List.of(q1, q2), respostas);

        assertEquals(0, new BigDecimal("3.00").compareTo(nota));
    }

    @Test
    void calcularNota_uploadArquivoIgnorado() {
        Questao mc = questaoMC(1L, 0, "10");
        Questao upload = questaoUpload(2L, "5");
        Map<Long, Object> respostas = Map.of(1L, 0);

        BigDecimal nota = atividadeService.calcularNotaAutomatica(1L, List.of(mc, upload), respostas);

        assertEquals(0, new BigDecimal("10.00").compareTo(nota));
    }

    // --- resolverStatus (via getAtividade) ---

    private void configurarGetAtividade(Entrega entrega) {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of());
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.ofNullable(entrega));
    }

    @Test
    void resolverStatus_semEntrega_retornaNaoIniciado() {
        configurarGetAtividade(null);

        var resp = atividadeService.getAtividade(1L, alunoId);
        assertEquals("NAO_INICIADO", resp.statusAluno());
    }

    @Test
    void resolverStatus_statusNaoIniciada_retornaNaoIniciado() {
        Entrega e = new Entrega();
        e.setStatus(StatusEntregaEnum.NAO_INICIADA);
        configurarGetAtividade(e);

        var resp = atividadeService.getAtividade(1L, alunoId);
        assertEquals("NAO_INICIADO", resp.statusAluno());
    }

    @Test
    void resolverStatus_statusRascunho_retornaEmAndamento() {
        Entrega e = new Entrega();
        e.setId(1L);
        e.setStatus(StatusEntregaEnum.RASCUNHO);
        when(respostaRepository.findByEntregaId(1L)).thenReturn(List.of());
        configurarGetAtividade(e);

        var resp = atividadeService.getAtividade(1L, alunoId);
        assertEquals("EM_ANDAMENTO", resp.statusAluno());
    }

    @Test
    void resolverStatus_statusEntregue_retornaEntregue() {
        Entrega e = new Entrega();
        e.setStatus(StatusEntregaEnum.ENTREGUE);
        configurarGetAtividade(e);

        var resp = atividadeService.getAtividade(1L, alunoId);
        assertEquals("ENTREGUE", resp.statusAluno());
    }

    @Test
    void resolverStatus_statusCorrigida_retornaEntregue() {
        Entrega e = new Entrega();
        e.setStatus(StatusEntregaEnum.CORRIGIDA);
        configurarGetAtividade(e);

        var resp = atividadeService.getAtividade(1L, alunoId);
        assertEquals("ENTREGUE", resp.statusAluno());
    }

    // --- isGabaritoDisponivel (via getAtividade) ---

    @Test
    void gabarito_liberacaoImediata_sempredisponivel() {
        avaliacao.setGabaritoLiberacao(GabaritoLiberacaoEnum.IMEDIATA);
        configurarGetAtividade(null);

        var resp = atividadeService.getAtividade(1L, alunoId);
        assertTrue(resp.gabaritoDisponivel());
    }

    @Test
    void gabarito_aposEncerramento_encerraEmPassado_disponivel() {
        avaliacao.setGabaritoLiberacao(GabaritoLiberacaoEnum.APOS_ENCERRAMENTO);
        avaliacao.setEncerraEm(OffsetDateTime.now().minusHours(1));
        configurarGetAtividade(null);

        var resp = atividadeService.getAtividade(1L, alunoId);
        assertTrue(resp.gabaritoDisponivel());
    }

    @Test
    void gabarito_aposEncerramento_encerraEmFuturo_naoDisponivel() {
        avaliacao.setGabaritoLiberacao(GabaritoLiberacaoEnum.APOS_ENCERRAMENTO);
        avaliacao.setEncerraEm(OffsetDateTime.now().plusHours(1));
        configurarGetAtividade(null);

        var resp = atividadeService.getAtividade(1L, alunoId);
        assertFalse(resp.gabaritoDisponivel());
    }

    @Test
    void gabarito_aposEncerramento_encerraEmNull_naoDisponivel() {
        avaliacao.setGabaritoLiberacao(GabaritoLiberacaoEnum.APOS_ENCERRAMENTO);
        avaliacao.setEncerraEm(null);
        configurarGetAtividade(null);

        var resp = atividadeService.getAtividade(1L, alunoId);
        assertFalse(resp.gabaritoDisponivel());
    }

    // --- entregar ---

    private void configurarEntregar(Entrega entregaExistente) {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(new Usuario()));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of());
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId))
            .thenReturn(Optional.ofNullable(entregaExistente));
        when(respostaRepository.findByEntregaIdAndQuestaoId(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(inv -> {
            Entrega e = inv.getArgument(0);
            if (e.getId() == null) e.setId(100L);
            return e;
        });
    }

    @Test
    void entregar_avaliacaoNaoEncontrada_deveLancarAvaliacaoNaoEncontradaException() {
        when(avaliacaoRepository.findById(99L)).thenReturn(Optional.empty());
        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        assertThrows(AvaliacaoNaoEncontradaException.class, () -> atividadeService.entregar(99L, req, alunoId));
    }

    @Test
    void entregar_dentroDoPrazo_entregaNaoAtrasada() {
        avaliacao.setEncerraEm(OffsetDateTime.now().plusHours(1));
        configurarEntregar(null);

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        EntregarAtividadeResponse resp = atividadeService.entregar(1L, req, alunoId);

        assertNotNull(resp);
    }

    @Test
    void entregar_aposEEncerrado_permiteAtrasada_aceita() {
        avaliacao.setEncerraEm(OffsetDateTime.now().minusHours(1));
        avaliacao.setPermiteEntregaAtrasada(true);
        configurarEntregar(null);

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        assertDoesNotThrow(() -> atividadeService.entregar(1L, req, alunoId));
    }

    @Test
    void entregar_aposEEncerrado_naoPermiteAtrasada_lancaOperacaoNaoPermitida() {
        avaliacao.setEncerraEm(OffsetDateTime.now().minusHours(1));
        avaliacao.setPermiteEntregaAtrasada(false);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        assertThrows(OperacaoNaoPermitidaException.class, () -> atividadeService.entregar(1L, req, alunoId));
    }

    @Test
    void entregar_statusEntregue_lancaOperacaoNaoPermitida() {
        Entrega e = new Entrega();
        e.setId(1L);
        e.setStatus(StatusEntregaEnum.ENTREGUE);
        configurarEntregar(e);

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        assertThrows(OperacaoNaoPermitidaException.class, () -> atividadeService.entregar(1L, req, alunoId));
    }

    @Test
    void entregar_statusCorrigida_lancaOperacaoNaoPermitida() {
        Entrega e = new Entrega();
        e.setId(1L);
        e.setStatus(StatusEntregaEnum.CORRIGIDA);
        configurarEntregar(e);

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        assertThrows(OperacaoNaoPermitidaException.class, () -> atividadeService.entregar(1L, req, alunoId));
    }

    @Test
    void entregar_iniciadoEmNullAntes_devePreencherIniciadoEm() {
        Entrega e = new Entrega();
        e.setId(1L);
        e.setStatus(StatusEntregaEnum.RASCUNHO);
        e.setIniciadoEm(null);
        configurarEntregar(e);

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        atividadeService.entregar(1L, req, alunoId);

        assertNotNull(e.getIniciadoEm());
    }

    @Test
    void entregar_iniciadoEmJaPreenchido_preservaValorAnterior() {
        OffsetDateTime inicioOriginal = OffsetDateTime.now().minusHours(2);
        Entrega e = new Entrega();
        e.setId(1L);
        e.setStatus(StatusEntregaEnum.RASCUNHO);
        e.setIniciadoEm(inicioOriginal);
        configurarEntregar(e);

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        atividadeService.entregar(1L, req, alunoId);

        assertEquals(inicioOriginal, e.getIniciadoEm());
    }

    @Test
    void entregar_statusMudaParaEntregue() {
        Entrega e = new Entrega();
        e.setId(1L);
        e.setStatus(StatusEntregaEnum.RASCUNHO);
        configurarEntregar(e);

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        atividadeService.entregar(1L, req, alunoId);

        assertEquals(StatusEntregaEnum.ENTREGUE, e.getStatus());
        assertNotNull(e.getEntregueEm());
    }

    @Test
    void entregar_gabaritoImediato_retornaGabarito() {
        avaliacao.setGabaritoLiberacao(GabaritoLiberacaoEnum.IMEDIATA);
        Questao q = questaoMC(1L, 0, "10");
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(new Usuario()));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of(q));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.empty());
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(inv -> {
            Entrega entry = inv.getArgument(0);
            if (entry.getId() == null) entry.setId(100L);
            return entry;
        });
        when(respostaRepository.findByEntregaIdAndQuestaoId(anyLong(), anyLong())).thenReturn(Optional.empty());

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of(1L, 0));
        EntregarAtividadeResponse resp = atividadeService.entregar(1L, req, alunoId);

        assertTrue(resp.gabaritoDisponivel());
        assertFalse(resp.gabarito().isEmpty());
    }

    // --- salvarRascunho ---

    @Test
    void salvarRascunho_statusEntregue_lancaOperacaoNaoPermitida() {
        Entrega e = new Entrega();
        e.setId(1L);
        e.setStatus(StatusEntregaEnum.ENTREGUE);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(new Usuario()));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.of(e));

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        assertThrows(OperacaoNaoPermitidaException.class, () -> atividadeService.salvarRascunho(1L, req, alunoId));
    }

    @Test
    void salvarRascunho_statusCorrigida_lancaOperacaoNaoPermitida() {
        Entrega e = new Entrega();
        e.setId(1L);
        e.setStatus(StatusEntregaEnum.CORRIGIDA);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(new Usuario()));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.of(e));

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        assertThrows(OperacaoNaoPermitidaException.class, () -> atividadeService.salvarRascunho(1L, req, alunoId));
    }

    @Test
    void salvarRascunho_primeiraVez_statusMudaParaRascunho() {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(new Usuario()));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.empty());
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of());
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(inv -> {
            Entrega e = inv.getArgument(0);
            if (e.getId() == null) e.setId(100L);
            return e;
        });

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        atividadeService.salvarRascunho(1L, req, alunoId);

        verify(entregaRepository, atLeastOnce()).save(argThat(e -> e.getStatus() == StatusEntregaEnum.RASCUNHO));
    }

    @Test
    void salvarRascunho_rascunhoExistente_statusPreservado() {
        Entrega e = new Entrega();
        e.setId(1L);
        e.setStatus(StatusEntregaEnum.RASCUNHO);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(new Usuario()));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.of(e));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of());
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(inv -> inv.getArgument(0));

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        atividadeService.salvarRascunho(1L, req, alunoId);

        assertEquals(StatusEntregaEnum.RASCUNHO, e.getStatus());
    }

    @Test
    void salvarRascunho_chamaSave() {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(new Usuario()));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.empty());
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of());
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(inv -> {
            Entrega entry = inv.getArgument(0);
            if (entry.getId() == null) entry.setId(100L);
            return entry;
        });

        SalvarRascunhoAtividadeRequest req = new SalvarRascunhoAtividadeRequest(Map.of());
        atividadeService.salvarRascunho(1L, req, alunoId);

        verify(entregaRepository, atLeastOnce()).save(any(Entrega.class));
    }

    // --- getResultado ---

    @Test
    void getResultado_entregaNaoEncontrada_lancaOperacaoNaoPermitida() {
        when(entregaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(OperacaoNaoPermitidaException.class, () -> atividadeService.getResultado(99L, alunoId));
    }

    @Test
    void getResultado_alunoErrado_lancaOperacaoNaoPermitidaAcessoNegado() {
        Usuario outroAluno = new Usuario();
        outroAluno.setId(UUID.randomUUID());

        Entrega e = new Entrega();
        e.setId(1L);
        e.setAluno(outroAluno);
        e.setAvaliacao(avaliacao);

        when(entregaRepository.findById(1L)).thenReturn(Optional.of(e));

        assertThrows(OperacaoNaoPermitidaException.class, () -> atividadeService.getResultado(1L, alunoId));
    }

    @Test
    void getResultado_gabaritoDisponivelComImediata_retornaGabarito() {
        avaliacao.setGabaritoLiberacao(GabaritoLiberacaoEnum.IMEDIATA);

        Usuario proprietario = new Usuario();
        proprietario.setId(alunoId);

        Entrega e = new Entrega();
        e.setId(1L);
        e.setAluno(proprietario);
        e.setAvaliacao(avaliacao);
        e.setNotaFinal(new BigDecimal("8.00"));

        Questao q = questaoMC(1L, 0, "10");

        when(entregaRepository.findById(1L)).thenReturn(Optional.of(e));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of(q));
        when(respostaRepository.findByEntregaId(1L)).thenReturn(List.of());
        when(entregaRepository.findAll()).thenReturn(List.of(e));

        var resp = atividadeService.getResultado(1L, alunoId);

        assertTrue(resp.gabaritoDisponivel());
    }
}
