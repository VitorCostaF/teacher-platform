package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Avaliacao;
import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.domain.entity.Matricula;
import br.com.inovadados.teacherplatform.domain.entity.Turma;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.PerfilEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusFrequenciaEnum;
import br.com.inovadados.teacherplatform.exception.AcessoNegadoException;
import br.com.inovadados.teacherplatform.exception.TurmaNaoEncontradaException;
import br.com.inovadados.teacherplatform.repository.AvaliacaoRepository;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.MatriculaRepository;
import br.com.inovadados.teacherplatform.repository.RegistroFrequenciaRepository;
import br.com.inovadados.teacherplatform.repository.RespostaRepository;
import br.com.inovadados.teacherplatform.repository.TurmaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock TurmaRepository turmaRepository;
    @Mock MatriculaRepository matriculaRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock EntregaRepository entregaRepository;
    @Mock RegistroFrequenciaRepository frequenciaRepository;
    @Mock RespostaRepository respostaRepository;
    @Mock UsuarioRepository usuarioRepository;

    @InjectMocks DashboardService dashboardService;

    private UUID professorId;
    private UUID alunoId;
    private final Long turmaId = 1L;

    @BeforeEach
    void setUp() {
        professorId = UUID.randomUUID();
        alunoId = UUID.randomUUID();
    }

    private Turma turmaComProfessor(UUID profId) {
        Turma t = new Turma();
        t.setId(turmaId);
        t.setNome("Turma A");
        t.setDisciplina("Matemática");
        Usuario prof = new Usuario();
        prof.setId(profId);
        t.setProfessor(prof);
        return t;
    }

    private Usuario usuario(UUID id, PerfilEnum perfil) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setPerfil(perfil);
        u.setNome("Usuário");
        return u;
    }

    private Matricula matricula(UUID alunoUUID) {
        Matricula m = new Matricula();
        Usuario aluno = new Usuario();
        aluno.setId(alunoUUID);
        aluno.setNome("Aluno");
        m.setAluno(aluno);
        return m;
    }

    private Entrega entregaComNota(UUID alunoUUID, String nota) {
        Entrega e = new Entrega();
        Usuario aluno = new Usuario();
        aluno.setId(alunoUUID);
        e.setAluno(aluno);
        e.setNotaFinal(nota != null ? new BigDecimal(nota) : null);
        return e;
    }

    private void stubGetDesempenhoTurmaVazio(UUID usuarioId) {
        when(matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turmaId)).thenReturn(List.of());
        when(avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turmaId)).thenReturn(List.of());
        when(entregaRepository.findByAvaliacaoIdIn(List.of())).thenReturn(List.of());
        when(frequenciaRepository.countAulasByTurmaId(turmaId)).thenReturn(0L);
    }

    // --- calcularTendencia ---

    @Test
    void calcularTendencia_listaVazia_retornaStable() {
        assertEquals("STABLE", dashboardService.calcularTendencia(List.of()));
    }

    @Test
    void calcularTendencia_umaNota_retornaStable() {
        assertEquals("STABLE", dashboardService.calcularTendencia(List.of(new BigDecimal("7.0"))));
    }

    @Test
    void calcularTendencia_recenteMaisAltaDiff3_retornaUp() {
        // [5.0, 8.0]: antiga=[5.0], recente=[8.0], diff=3.0 > 0.3
        assertEquals("UP", dashboardService.calcularTendencia(
                List.of(new BigDecimal("5.0"), new BigDecimal("8.0"))));
    }

    @Test
    void calcularTendencia_recenteMaisBaixaDiffMenos3_retornaDown() {
        // [8.0, 5.0]: diff=-3.0 < -0.3
        assertEquals("DOWN", dashboardService.calcularTendencia(
                List.of(new BigDecimal("8.0"), new BigDecimal("5.0"))));
    }

    @Test
    void calcularTendencia_diferencaPequena_retornaStable() {
        // [5.0, 5.1]: diff=0.1, dentro de ±0.3
        assertEquals("STABLE", dashboardService.calcularTendencia(
                List.of(new BigDecimal("5.0"), new BigDecimal("5.1"))));
    }

    @Test
    void calcularTendencia_diferencaExatamenteMais03_retornaStable() {
        // diff = +0.3, não é > 0.3
        assertEquals("STABLE", dashboardService.calcularTendencia(
                List.of(new BigDecimal("5.0"), new BigDecimal("5.3"))));
    }

    @Test
    void calcularTendencia_diferencaExatamenteMenos03_retornaStable() {
        // diff = -0.3, não é < -0.3
        assertEquals("STABLE", dashboardService.calcularTendencia(
                List.of(new BigDecimal("5.3"), new BigDecimal("5.0"))));
    }

    @Test
    void calcularTendencia_listaImpar_elementoCentralVaiParaRecente() {
        // [3.0, 8.0, 3.5]: metade=1, antiga=[3.0], recente=[8.0, 3.5]=5.75, diff=2.75 → UP
        // Se central fosse para antiga: antiga=[3.0,8.0]=5.5, recente=[3.5]=3.5, diff=-2.0 → DOWN
        assertEquals("UP", dashboardService.calcularTendencia(
                List.of(new BigDecimal("3.0"), new BigDecimal("8.0"), new BigDecimal("3.5"))));
    }

    // --- calcularPosicaoNaTurma ---

    @Test
    void calcularPosicaoNaTurma_unicoAlunoComNota_retorna1() {
        when(entregaRepository.findByAvaliacaoId(1L)).thenReturn(List.of(entregaComNota(alunoId, "7.0")));

        assertEquals(1, dashboardService.calcularPosicaoNaTurma(1L, alunoId));
    }

    @Test
    void calcularPosicaoNaTurma_alunoComMaiorNota_retorna1() {
        UUID outro = UUID.randomUUID();
        when(entregaRepository.findByAvaliacaoId(1L)).thenReturn(List.of(
                entregaComNota(alunoId, "9.0"),
                entregaComNota(outro, "7.0")
        ));

        assertEquals(1, dashboardService.calcularPosicaoNaTurma(1L, alunoId));
    }

    @Test
    void calcularPosicaoNaTurma_alunoComMenorNota_retornaUltimaPosicao() {
        UUID outro = UUID.randomUUID();
        when(entregaRepository.findByAvaliacaoId(1L)).thenReturn(List.of(
                entregaComNota(outro, "9.0"),
                entregaComNota(alunoId, "5.0")
        ));

        assertEquals(2, dashboardService.calcularPosicaoNaTurma(1L, alunoId));
    }

    @Test
    void calcularPosicaoNaTurma_alunoSemNota_filtradoRetornaSizeMaisUm() {
        UUID outro1 = UUID.randomUUID();
        UUID outro2 = UUID.randomUUID();
        // alunoId tem nota null → filtrado; lista filtrada tem 2 → retorna 3
        when(entregaRepository.findByAvaliacaoId(1L)).thenReturn(List.of(
                entregaComNota(alunoId, null),
                entregaComNota(outro1, "8.0"),
                entregaComNota(outro2, "6.0")
        ));

        assertEquals(3, dashboardService.calcularPosicaoNaTurma(1L, alunoId));
    }

    @Test
    void calcularPosicaoNaTurma_alunoNaoParticipou_retornaSizeMaisUm() {
        UUID outro1 = UUID.randomUUID();
        UUID outro2 = UUID.randomUUID();
        when(entregaRepository.findByAvaliacaoId(1L)).thenReturn(List.of(
                entregaComNota(outro1, "8.0"),
                entregaComNota(outro2, "6.0")
        ));

        assertEquals(3, dashboardService.calcularPosicaoNaTurma(1L, alunoId));
    }

    @Test
    void calcularPosicaoNaTurma_listaVazia_retorna1() {
        when(entregaRepository.findByAvaliacaoId(1L)).thenReturn(List.of());

        assertEquals(1, dashboardService.calcularPosicaoNaTurma(1L, alunoId));
    }

    // --- determinarSituacao via getDesempenhoAluno ---

    private void stubDesempenhoAluno(long totalAulas, long presencas) {
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turmaComProfessor(professorId)));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(usuario(alunoId, PerfilEnum.ALUNO)));
        when(avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turmaId)).thenReturn(List.of());
        when(entregaRepository.findByAlunoId(alunoId)).thenReturn(List.of());
        when(frequenciaRepository.countAulasByTurmaId(turmaId)).thenReturn(totalAulas);
        when(frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(turmaId, alunoId, StatusFrequenciaEnum.PRESENTE))
                .thenReturn(presencas);
        when(frequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(turmaId, alunoId))
                .thenReturn(List.of());
    }

    @Test
    void determinarSituacao_frequenciaBaixo75_retornaReprovadoPorFalta() {
        // 7/10 = 70% < 75 → REPROVADO_POR_FALTA independente da nota
        stubDesempenhoAluno(10L, 7L);

        var resp = dashboardService.getDesempenhoAluno(turmaId, alunoId, UUID.randomUUID(), PerfilEnum.PROFESSOR);
        assertEquals("REPROVADO_POR_FALTA", resp.situacao());
    }

    @Test
    void determinarSituacao_frequencia74PorCento_retornaReprovadoPorFalta() {
        // 74/100 = 74% < 75
        stubDesempenhoAluno(100L, 74L);

        var resp = dashboardService.getDesempenhoAluno(turmaId, alunoId, UUID.randomUUID(), PerfilEnum.PROFESSOR);
        assertEquals("REPROVADO_POR_FALTA", resp.situacao());
    }

    @Test
    void determinarSituacao_frequencia75_naoReprovadoPorFalta() {
        // 75/100 = 75% == 75 → não é REPROVADO_POR_FALTA
        stubDesempenhoAluno(100L, 75L);

        var resp = dashboardService.getDesempenhoAluno(turmaId, alunoId, UUID.randomUUID(), PerfilEnum.PROFESSOR);
        assertNotEquals("REPROVADO_POR_FALTA", resp.situacao());
    }

    @Test
    void determinarSituacao_frequenciaOkMediaZero_retornaEmRisco() {
        // 80% frequencia OK; sem entregas → media=0 < 5 → EM_RISCO
        stubDesempenhoAluno(10L, 8L);

        var resp = dashboardService.getDesempenhoAluno(turmaId, alunoId, UUID.randomUUID(), PerfilEnum.PROFESSOR);
        assertEquals("EM_RISCO", resp.situacao());
    }

    @Test
    void determinarSituacao_mediaExatamente5_retornaAprovadoEmAndamento() {
        Long avId = 10L;
        Avaliacao av = new Avaliacao();
        av.setId(avId);
        Entrega entrega = entregaComNota(alunoId, "5.0");
        entrega.setAvaliacao(av);

        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turmaComProfessor(professorId)));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(usuario(alunoId, PerfilEnum.ALUNO)));
        when(avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turmaId)).thenReturn(List.of(av));
        when(entregaRepository.findByAlunoId(alunoId)).thenReturn(List.of(entrega));
        when(frequenciaRepository.countAulasByTurmaId(turmaId)).thenReturn(10L);
        when(frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(turmaId, alunoId, StatusFrequenciaEnum.PRESENTE))
                .thenReturn(8L);
        when(frequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(turmaId, alunoId)).thenReturn(List.of());
        when(entregaRepository.findByAvaliacaoId(avId)).thenReturn(List.of(entrega));
        when(respostaRepository.findByEntregaId(any())).thenReturn(List.of());

        var resp = dashboardService.getDesempenhoAluno(turmaId, alunoId, UUID.randomUUID(), PerfilEnum.PROFESSOR);
        assertEquals("APROVADO_EM_ANDAMENTO", resp.situacao());
    }

    @Test
    void determinarSituacao_mediaAcima5_retornaAprovadoEmAndamento() {
        Long avId = 11L;
        Avaliacao av = new Avaliacao();
        av.setId(avId);
        Entrega entrega = entregaComNota(alunoId, "8.0");
        entrega.setAvaliacao(av);

        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turmaComProfessor(professorId)));
        when(usuarioRepository.findById(alunoId)).thenReturn(Optional.of(usuario(alunoId, PerfilEnum.ALUNO)));
        when(avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turmaId)).thenReturn(List.of(av));
        when(entregaRepository.findByAlunoId(alunoId)).thenReturn(List.of(entrega));
        when(frequenciaRepository.countAulasByTurmaId(turmaId)).thenReturn(10L);
        when(frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(turmaId, alunoId, StatusFrequenciaEnum.PRESENTE))
                .thenReturn(8L);
        when(frequenciaRepository.findByTurmaIdAndAlunoIdOrderByDataAulaAsc(turmaId, alunoId)).thenReturn(List.of());
        when(entregaRepository.findByAvaliacaoId(avId)).thenReturn(List.of(entrega));
        when(respostaRepository.findByEntregaId(any())).thenReturn(List.of());

        var resp = dashboardService.getDesempenhoAluno(turmaId, alunoId, UUID.randomUUID(), PerfilEnum.PROFESSOR);
        assertEquals("APROVADO_EM_ANDAMENTO", resp.situacao());
    }

    // --- getDesempenhoTurma: controle de acesso ---

    @Test
    void getDesempenhoTurma_turmaNaoEncontrada_lancaException() {
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.empty());

        assertThrows(TurmaNaoEncontradaException.class,
                () -> dashboardService.getDesempenhoTurma(turmaId, UUID.randomUUID()));
    }

    @Test
    void getDesempenhoTurma_professorDeTurmaAlheia_lancaAcessoNegado() {
        UUID outroProfessor = UUID.randomUUID();
        Turma turma = turmaComProfessor(professorId);
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(outroProfessor))
                .thenReturn(Optional.of(usuario(outroProfessor, PerfilEnum.PROFESSOR)));

        assertThrows(AcessoNegadoException.class,
                () -> dashboardService.getDesempenhoTurma(turmaId, outroProfessor));
    }

    @Test
    void getDesempenhoTurma_admin_permiteAcessoATurmaAlheia() {
        UUID adminId = UUID.randomUUID();
        Turma turma = turmaComProfessor(professorId);
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(adminId)).thenReturn(Optional.of(usuario(adminId, PerfilEnum.ADMIN)));
        stubGetDesempenhoTurmaVazio(adminId);

        assertDoesNotThrow(() -> dashboardService.getDesempenhoTurma(turmaId, adminId));
    }

    @Test
    void getDesempenhoTurma_coordenador_permiteAcessoATurmaAlheia() {
        UUID coordId = UUID.randomUUID();
        Turma turma = turmaComProfessor(professorId);
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(coordId)).thenReturn(Optional.of(usuario(coordId, PerfilEnum.COORDENADOR)));
        stubGetDesempenhoTurmaVazio(coordId);

        assertDoesNotThrow(() -> dashboardService.getDesempenhoTurma(turmaId, coordId));
    }

    @Test
    void getDesempenhoTurma_professorDaTurma_permiteAcesso() {
        Turma turma = turmaComProfessor(professorId);
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.of(usuario(professorId, PerfilEnum.PROFESSOR)));
        stubGetDesempenhoTurmaVazio(professorId);

        assertDoesNotThrow(() -> dashboardService.getDesempenhoTurma(turmaId, professorId));
    }

    // --- getDesempenhoTurma: cálculo de aprovação ---

    @Test
    void getDesempenhoTurma_semMatriculas_pctAprovacaoZero() {
        Turma turma = turmaComProfessor(professorId);
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.of(usuario(professorId, PerfilEnum.PROFESSOR)));
        when(matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turmaId)).thenReturn(List.of());
        when(avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turmaId)).thenReturn(List.of());
        when(entregaRepository.findByAvaliacaoIdIn(List.of())).thenReturn(List.of());
        when(frequenciaRepository.countAulasByTurmaId(turmaId)).thenReturn(5L);

        var resp = dashboardService.getDesempenhoTurma(turmaId, professorId);
        assertEquals(0.0, resp.percentualAprovacao());
    }

    @Test
    void getDesempenhoTurma_tresAprovadosDe4_pctAprovacao75() {
        UUID aluno1 = UUID.randomUUID(), aluno2 = UUID.randomUUID(),
             aluno3 = UUID.randomUUID(), aluno4 = UUID.randomUUID();
        Long avId = 99L;
        Avaliacao av = new Avaliacao();
        av.setId(avId);

        Turma turma = turmaComProfessor(professorId);
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.of(usuario(professorId, PerfilEnum.PROFESSOR)));

        List<Matricula> matriculas = List.of(matricula(aluno1), matricula(aluno2), matricula(aluno3), matricula(aluno4));
        when(matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turmaId)).thenReturn(matriculas);
        when(avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turmaId)).thenReturn(List.of(av));

        // aluno1→8.0, aluno2→7.0, aluno3→5.0 (aprovados), aluno4→3.0 (reprovado)
        Entrega e1 = entregaComNota(aluno1, "8.0");
        Entrega e2 = entregaComNota(aluno2, "7.0");
        Entrega e3 = entregaComNota(aluno3, "5.0");
        Entrega e4 = entregaComNota(aluno4, "3.0");
        when(entregaRepository.findByAvaliacaoIdIn(List.of(avId))).thenReturn(List.of(e1, e2, e3, e4));

        when(frequenciaRepository.countAulasByTurmaId(turmaId)).thenReturn(10L);
        when(frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(eq(turmaId), any(UUID.class), eq(StatusFrequenciaEnum.PRESENTE)))
                .thenReturn(8L);

        var resp = dashboardService.getDesempenhoTurma(turmaId, professorId);
        assertEquals(75.0, resp.percentualAprovacao());
    }

    // --- getDesempenhoTurma: cálculo de frequência ---

    @Test
    void getDesempenhoTurma_semMatriculas_pctFrequencia100() {
        Turma turma = turmaComProfessor(professorId);
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.of(usuario(professorId, PerfilEnum.PROFESSOR)));
        when(matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turmaId)).thenReturn(List.of());
        when(avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turmaId)).thenReturn(List.of());
        when(entregaRepository.findByAvaliacaoIdIn(List.of())).thenReturn(List.of());
        when(frequenciaRepository.countAulasByTurmaId(turmaId)).thenReturn(10L);

        var resp = dashboardService.getDesempenhoTurma(turmaId, professorId);
        assertEquals(100.0, resp.percentualFrequencia());
    }

    @Test
    void getDesempenhoTurma_totalAulasZero_pctFrequencia100() {
        Turma turma = turmaComProfessor(professorId);
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.of(usuario(professorId, PerfilEnum.PROFESSOR)));
        when(matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turmaId)).thenReturn(List.of(matricula(alunoId)));
        when(avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turmaId)).thenReturn(List.of());
        when(entregaRepository.findByAvaliacaoIdIn(List.of())).thenReturn(List.of());
        when(frequenciaRepository.countAulasByTurmaId(turmaId)).thenReturn(0L);

        var resp = dashboardService.getDesempenhoTurma(turmaId, professorId);
        assertEquals(100.0, resp.percentualFrequencia());
    }

    @Test
    void getDesempenhoTurma_pctFrequenciaFormula_calculadaCorretamente() {
        // 1 matricula, 4 aulas, 2 presencas → 2/(1*4)*100 = 50.0
        Turma turma = turmaComProfessor(professorId);
        when(turmaRepository.findById(turmaId)).thenReturn(Optional.of(turma));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.of(usuario(professorId, PerfilEnum.PROFESSOR)));
        when(matriculaRepository.findByTurmaIdAndRemovidoEmIsNull(turmaId)).thenReturn(List.of(matricula(alunoId)));
        when(avaliacaoRepository.findByTurmaIdOrderByDisponivelEmAsc(turmaId)).thenReturn(List.of());
        when(entregaRepository.findByAvaliacaoIdIn(List.of())).thenReturn(List.of());
        when(frequenciaRepository.countAulasByTurmaId(turmaId)).thenReturn(4L);
        when(frequenciaRepository.countByTurmaIdAndAlunoIdAndStatus(turmaId, alunoId, StatusFrequenciaEnum.PRESENTE))
                .thenReturn(2L);

        var resp = dashboardService.getDesempenhoTurma(turmaId, professorId);
        assertEquals(50.0, resp.percentualFrequencia());
    }
}
