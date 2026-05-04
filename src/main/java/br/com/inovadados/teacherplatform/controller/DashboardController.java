package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.PerfilEnum;
import br.com.inovadados.teacherplatform.dto.request.ObservacaoRequest;
import br.com.inovadados.teacherplatform.dto.response.DashboardProfessorResponse;
import br.com.inovadados.teacherplatform.dto.response.DesempenhoAlunoResponse;
import br.com.inovadados.teacherplatform.dto.response.DesempenhoTurmaResponse;
import br.com.inovadados.teacherplatform.dto.response.ObservacaoResponse;
import br.com.inovadados.teacherplatform.dto.response.RelatorioStatusResponse;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.service.DashboardService;
import br.com.inovadados.teacherplatform.service.ObservacaoService;
import br.com.inovadados.teacherplatform.service.RelatorioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final ObservacaoService observacaoService;
    private final RelatorioService relatorioService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/professor/dashboard")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'COORDENADOR')")
    public DashboardProfessorResponse getDashboard(Authentication authentication) {
        return dashboardService.getDashboardProfessor(getUsuarioId(authentication));
    }

    @GetMapping("/turmas/{id}/desempenho")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'COORDENADOR')")
    public DesempenhoTurmaResponse getDesempenhoTurma(
            @PathVariable Long id,
            Authentication authentication) {
        return dashboardService.getDesempenhoTurma(id, getUsuarioId(authentication));
    }

    @GetMapping("/turmas/{id}/alunos/{alunoId}/desempenho")
    public DesempenhoAlunoResponse getDesempenhoAluno(
            @PathVariable Long id,
            @PathVariable UUID alunoId,
            Authentication authentication) {
        var usuario = getUsuario(authentication);
        return dashboardService.getDesempenhoAluno(id, alunoId, usuario.getId(), usuario.getPerfil());
    }

    @PostMapping("/turmas/{id}/alunos/{alunoId}/observacoes")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'COORDENADOR')")
    public ResponseEntity<ObservacaoResponse> criarObservacao(
            @PathVariable Long id,
            @PathVariable UUID alunoId,
            @RequestBody @Valid ObservacaoRequest request,
            Authentication authentication) {
        ObservacaoResponse obs = observacaoService.criar(id, alunoId, getUsuarioId(authentication), request);
        return ResponseEntity.status(201).body(obs);
    }

    @GetMapping("/turmas/{id}/alunos/{alunoId}/observacoes")
    public List<ObservacaoResponse> listarObservacoes(
            @PathVariable Long id,
            @PathVariable UUID alunoId,
            Authentication authentication) {
        var usuario = getUsuario(authentication);
        return observacaoService.listar(id, alunoId, usuario.getPerfil());
    }

    @PostMapping("/relatorios/turma/{id}/pdf")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'COORDENADOR')")
    public RelatorioStatusResponse gerarRelatorioPDFTurma(
            @PathVariable Long id,
            Authentication authentication) {
        return relatorioService.solicitarRelatorioPDF("TURMA", id, getUsuarioId(authentication));
    }

    @PostMapping("/relatorios/aluno/{turmaId}/{alunoId}/pdf")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'COORDENADOR')")
    public RelatorioStatusResponse gerarRelatorioPDFAluno(
            @PathVariable Long turmaId,
            @PathVariable UUID alunoId,
            Authentication authentication) {
        return relatorioService.solicitarRelatorioAluno(turmaId, alunoId, getUsuarioId(authentication));
    }

    @GetMapping("/relatorios/status/{jobId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'COORDENADOR')")
    public RelatorioStatusResponse getStatusRelatorio(@PathVariable String jobId) {
        return relatorioService.getStatus(jobId);
    }

    private UUID getUsuarioId(Authentication authentication) {
        return getUsuario(authentication).getId();
    }

    private Usuario getUsuario(Authentication authentication) {
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
    }
}
