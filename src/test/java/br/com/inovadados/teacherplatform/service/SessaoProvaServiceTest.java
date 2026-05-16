package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.entity.SessaoProva;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.StatusAvaliacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.dto.request.AutosaveProvaRequest;
import br.com.inovadados.teacherplatform.exception.AvaliacaoNaoEncontradaException;
import br.com.inovadados.teacherplatform.exception.OperacaoNaoPermitidaException;
import br.com.inovadados.teacherplatform.repository.AvaliacaoRepository;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.QuestaoRepository;
import br.com.inovadados.teacherplatform.repository.RespostaRepository;
import br.com.inovadados.teacherplatform.repository.SessaoProvaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessaoProvaServiceTest {

    @Mock SessaoProvaRepository sessaoProvaRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock EntregaRepository entregaRepository;
    @Mock QuestaoRepository questaoRepository;
    @Mock RespostaRepository respostaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock AtividadeService atividadeService;

    @InjectMocks SessaoProvaService sessaoProvaService;

    private UUID alunoId;
    private Usuario aluno;
    private Avaliacao avaliacao;

    @BeforeEach
    void setUp() {
        alunoId = UUID.randomUUID();
        aluno = new Usuario();
        aluno.setId(alunoId);

        avaliacao = new Avaliacao();
        avaliacao.setId(1L);
        avaliacao.setStatus(StatusAvaliacaoEnum.PUBLICADA);
        avaliacao.setDuracaoMinutos(60);
    }

    private SessaoProva sessaoFake(UUID alunoId, Integer duracaoMin, OffsetDateTime encerrada) {
        Usuario a = new Usuario();
        a.setId(alunoId);
        Avaliacao av = new Avaliacao();
        av.setId(1L);
        av.setDuracaoMinutos(duracaoMin);
        SessaoProva s = new SessaoProva();
        s.setAluno(a);
        s.setAvaliacao(av);
        s.setIniciadaEm(OffsetDateTime.now().minusMinutes(10));
        s.setEncerradaEm(encerrada);
        return s;
    }

    // --- calcularTempoRestanteSegundos ---

    @Test
    void calcularTempoRestante_duracaoNull_retornaMaxLong() {
        SessaoProva s = sessaoFake(alunoId, null, null);
        assertEquals(Long.MAX_VALUE, sessaoProvaService.calcularTempoRestanteSegundos(s));
    }

    @Test
    void calcularTempoRestante_10MinDecorridos_60MinTotal_retornaAproximadamente3000s() {
        SessaoProva s = sessaoFake(alunoId, 60, null);
        long tempo = sessaoProvaService.calcularTempoRestanteSegundos(s);
        assertTrue(tempo >= 2990 && tempo <= 3010, "Esperado ~3000, obtido: " + tempo);
    }

    @Test
    void calcularTempoRestante_90MinDecorridos_60MinTotal_retornaZero() {
        SessaoProva s = sessaoFake(alunoId, 60, null);
        s.setIniciadaEm(OffsetDateTime.now().minusMinutes(90));
        assertEquals(0, sessaoProvaService.calcularTempoRestanteSegundos(s));
    }

    @Test
    void calcularTempoRestante_expiradoAgora_retornaZero() {
        SessaoProva s = sessaoFake(alunoId, 10, null);
        s.setIniciadaEm(OffsetDateTime.now().minusMinutes(10));
        long tempo = sessaoProvaService.calcularTempoRestanteSegundos(s);
        assertTrue(tempo >= 0 && tempo <= 5, "Esperado 0, obtido: " + tempo);
    }

    // --- iniciar ---

    @Test
    void iniciar_provaNotFound_lancaAvaliacaoNaoEncontradaException() {
        when(avaliacaoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AvaliacaoNaoEncontradaException.class, () -> sessaoProvaService.iniciar(99L, alunoId));
    }

    @Test
    void iniciar_provaNotPublicada_lancaOperacaoNaoPermitida() {
        avaliacao.setStatus(StatusAvaliacaoEnum.RASCUNHO);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        assertThrows(OperacaoNaoPermitidaException.class, () -> sessaoProvaService.iniciar(1L, alunoId));
    }

    @Test
    void iniciar_disponivelEmFuturo_lancaOperacaoNaoPermitida() {
        avaliacao.setDisponivelEm(OffsetDateTime.now().plusHours(1));
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        assertThrows(OperacaoNaoPermitidaException.class, () -> sessaoProvaService.iniciar(1L, alunoId));
    }

    @Test
    void iniciar_encerraEmPassado_lancaOperacaoNaoPermitida() {
        avaliacao.setEncerraEm(OffsetDateTime.now().minusHours(1));
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        assertThrows(OperacaoNaoPermitidaException.class, () -> sessaoProvaService.iniciar(1L, alunoId));
    }

    @Test
    void iniciar_sessaoJaEncerrada_lancaOperacaoNaoPermitida() {
        SessaoProva sessaoEncerrada = sessaoFake(alunoId, 60, OffsetDateTime.now().minusHours(1));
        sessaoEncerrada.setAvaliacao(avaliacao);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(sessaoProvaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.of(sessaoEncerrada));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.empty());
        assertThrows(OperacaoNaoPermitidaException.class, () -> sessaoProvaService.iniciar(1L, alunoId));
    }

    @Test
    void iniciar_primeiravez_criaSessaoComIniciadaEm() {
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(sessaoProvaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(sessaoProvaRepository.save(any(SessaoProva.class))).thenAnswer(inv -> {
            SessaoProva s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.empty());

        var resp = sessaoProvaService.iniciar(1L, alunoId);

        assertNotNull(resp.iniciadaEm());
    }

    @Test
    void iniciar_sessaoExistente_reutilizaSemCriar() {
        SessaoProva sessao = sessaoFake(alunoId, 60, null);
        sessao.setId(5L);
        sessao.setAvaliacao(avaliacao);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(sessaoProvaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.of(sessao));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.empty());

        sessaoProvaService.iniciar(1L, alunoId);

        verify(sessaoProvaRepository, never()).save(any());
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void iniciar_duracaoNull_retornaDuracaoZero() {
        avaliacao.setDuracaoMinutos(null);
        SessaoProva sessao = sessaoFake(alunoId, null, null);
        sessao.setId(1L);
        sessao.setAvaliacao(avaliacao);
        when(avaliacaoRepository.findById(1L)).thenReturn(Optional.of(avaliacao));
        when(sessaoProvaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.of(sessao));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.empty());

        var resp = sessaoProvaService.iniciar(1L, alunoId);

        assertEquals(0, resp.duracaoMinutos());
    }

    // --- autosave ---

    private SessaoProva sessaoAtiva() {
        SessaoProva s = new SessaoProva();
        s.setId(1L);
        s.setAluno(aluno);
        s.setAvaliacao(avaliacao);
        s.setIniciadaEm(OffsetDateTime.now().minusMinutes(5));
        s.setEncerradaEm(null);
        return s;
    }

    @Test
    void autosave_sessaoNaoEncontrada_lancaOperacaoNaoPermitida() {
        when(sessaoProvaRepository.findById(1L)).thenReturn(Optional.empty());
        AutosaveProvaRequest req = new AutosaveProvaRequest(Map.of(), null);
        assertThrows(OperacaoNaoPermitidaException.class, () -> sessaoProvaService.autosave(1L, req, alunoId));
    }

    @Test
    void autosave_alunoErrado_lancaOperacaoNaoPermitida() {
        SessaoProva s = sessaoAtiva();
        UUID outroAluno = UUID.randomUUID();
        when(sessaoProvaRepository.findById(1L)).thenReturn(Optional.of(s));
        AutosaveProvaRequest req = new AutosaveProvaRequest(Map.of(), null);
        assertThrows(OperacaoNaoPermitidaException.class, () -> sessaoProvaService.autosave(1L, req, outroAluno));
    }

    @Test
    void autosave_sessaoEncerrada_lancaOperacaoNaoPermitida() {
        SessaoProva s = sessaoAtiva();
        s.setEncerradaEm(OffsetDateTime.now());
        when(sessaoProvaRepository.findById(1L)).thenReturn(Optional.of(s));
        AutosaveProvaRequest req = new AutosaveProvaRequest(Map.of(), null);
        assertThrows(OperacaoNaoPermitidaException.class, () -> sessaoProvaService.autosave(1L, req, alunoId));
    }

    @Test
    void autosave_statusNaoIniciada_mudaParaRascunho() {
        SessaoProva s = sessaoAtiva();
        Entrega entrega = new Entrega();
        entrega.setId(1L);
        entrega.setStatus(StatusEntregaEnum.NAO_INICIADA);

        when(sessaoProvaRepository.findById(1L)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.of(entrega));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of());
        when(entregaRepository.save(any())).thenReturn(entrega);

        AutosaveProvaRequest req = new AutosaveProvaRequest(Map.of(), null);
        sessaoProvaService.autosave(1L, req, alunoId);

        assertEquals(StatusEntregaEnum.RASCUNHO, entrega.getStatus());
    }

    @Test
    void autosave_respostasNull_naoSalvaRespostas() {
        SessaoProva s = sessaoAtiva();
        Entrega entrega = new Entrega();
        entrega.setId(1L);
        entrega.setStatus(StatusEntregaEnum.RASCUNHO);

        when(sessaoProvaRepository.findById(1L)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(aluno));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.of(entrega));
        when(questaoRepository.findByAvaliacaoIdOrderByOrdem(1L)).thenReturn(List.of());

        AutosaveProvaRequest req = new AutosaveProvaRequest(null, null);
        sessaoProvaService.autosave(1L, req, alunoId);

        verify(respostaRepository, never()).save(any());
    }

    // --- entregar ---

    @Test
    void entregar_sessaoNaoEncontrada_lancaOperacaoNaoPermitida() {
        when(sessaoProvaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OperacaoNaoPermitidaException.class, () -> sessaoProvaService.entregar(1L, 1L, alunoId));
    }

    @Test
    void entregar_alunoErrado_lancaOperacaoNaoPermitida() {
        SessaoProva s = sessaoAtiva();
        when(sessaoProvaRepository.findById(1L)).thenReturn(Optional.of(s));
        assertThrows(OperacaoNaoPermitidaException.class, () -> sessaoProvaService.entregar(1L, 1L, UUID.randomUUID()));
    }

    @Test
    void entregar_sessaoEncerrada_lancaOperacaoNaoPermitida() {
        SessaoProva s = sessaoAtiva();
        s.setEncerradaEm(OffsetDateTime.now());
        when(sessaoProvaRepository.findById(1L)).thenReturn(Optional.of(s));
        assertThrows(OperacaoNaoPermitidaException.class, () -> sessaoProvaService.entregar(1L, 1L, alunoId));
    }

    @Test
    void entregar_encerraEPreencheEncerradaEm() {
        SessaoProva s = sessaoAtiva();
        when(sessaoProvaRepository.findById(1L)).thenReturn(Optional.of(s));
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.empty());
        when(atividadeService.entregar(anyLong(), any(), any())).thenReturn(null);
        when(sessaoProvaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sessaoProvaService.entregar(1L, 1L, alunoId);

        assertNotNull(s.getEncerradaEm());
        assertTrue(s.isEntregueManualmente());
    }

    // --- encerrarPorExpiracao ---

    @Test
    void encerrarPorExpiracao_sucesso_encerradaEmPreenchidaEManualmenteFalse() {
        SessaoProva s = sessaoFake(alunoId, 60, null);
        s.setAvaliacao(avaliacao);
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(1L, alunoId)).thenReturn(Optional.empty());
        when(atividadeService.entregar(anyLong(), any(), any())).thenReturn(null);
        when(sessaoProvaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sessaoProvaService.encerrarPorExpiracao(s);

        assertNotNull(s.getEncerradaEm());
        assertFalse(s.isEntregueManualmente());
    }

    @Test
    void encerrarPorExpiracao_operacaoNaoPermitida_naoPropagatException() {
        SessaoProva s = sessaoFake(alunoId, 60, null);
        s.setAvaliacao(avaliacao);
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(anyLong(), any())).thenReturn(Optional.empty());
        when(atividadeService.entregar(anyLong(), any(), any()))
            .thenThrow(new OperacaoNaoPermitidaException("já entregue"));
        when(sessaoProvaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> sessaoProvaService.encerrarPorExpiracao(s));
        assertNotNull(s.getEncerradaEm());
    }

    @Test
    void encerrarPorExpiracao_excecaoGenerica_naoPropagatException() {
        SessaoProva s = sessaoFake(alunoId, 60, null);
        s.setAvaliacao(avaliacao);
        when(entregaRepository.findByAvaliacaoIdAndAlunoId(anyLong(), any())).thenReturn(Optional.empty());
        when(atividadeService.entregar(anyLong(), any(), any()))
            .thenThrow(new RuntimeException("erro inesperado"));
        when(sessaoProvaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> sessaoProvaService.encerrarPorExpiracao(s));
        assertNotNull(s.getEncerradaEm());
    }
}
