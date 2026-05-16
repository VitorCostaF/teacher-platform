package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.dto.request.AdicionarAlunoRequest;
import br.com.inovadados.teacherplatform.dto.request.CriarTurmaRequest;
import br.com.inovadados.teacherplatform.dto.response.AlunoTurmaResponse;
import br.com.inovadados.teacherplatform.dto.response.ImportacaoResponse;
import br.com.inovadados.teacherplatform.dto.response.TurmaDetalheResponse;
import br.com.inovadados.teacherplatform.dto.response.TurmaResumoResponse;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.service.TurmaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TurmaController {

    private final TurmaService turmaService;
    private final UsuarioRepository usuarioRepository;

    // TODO extrair serviço
    private Usuario getUsuarioAutenticado(Authentication auth) {
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado"));
    }

    @GetMapping("/professor/turmas")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'COORDENADOR')")
    public List<TurmaResumoResponse> listarTurmasProfessor(
            @RequestParam(required = false) Long periodo,
            Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        return turmaService.listarTurmasProfessor(usuario.getId(), periodo);
    }

    @GetMapping("/admin/turmas")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDENADOR')")
    public List<TurmaResumoResponse> listarTurmasAdmin(
            @RequestParam(required = false) Long periodo,
            Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        return turmaService.listarTurmasAdmin(usuario.getEscola().getId(), periodo);
    }

    @GetMapping("/turmas/{id}")
    public TurmaDetalheResponse buscarTurma(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_COORDENADOR"));
        return turmaService.buscarTurma(id, usuario.getId(), isAdmin);
    }

    @PostMapping("/admin/turmas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TurmaDetalheResponse> criarTurma(@RequestBody @Valid CriarTurmaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turmaService.criarTurma(req));
    }

    @PutMapping("/admin/turmas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TurmaDetalheResponse editarTurma(
            @PathVariable Long id,
            @RequestBody @Valid CriarTurmaRequest req) {
        return turmaService.editarTurma(id, req);
    }

    @PatchMapping("/admin/turmas/{id}/encerrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> encerrarTurma(@PathVariable Long id) {
        turmaService.encerrarTurma(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/turmas/{id}/alunos")
    public List<AlunoTurmaResponse> listarAlunos(@PathVariable Long id) {
        return turmaService.listarAlunos(id);
    }

    @PostMapping("/turmas/{id}/alunos")
    public ResponseEntity<AlunoTurmaResponse> adicionarAluno(
            @PathVariable Long id,
            @RequestBody AdicionarAlunoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turmaService.adicionarAluno(id, req));
    }

    @DeleteMapping("/turmas/{id}/alunos/{alunoId}")
    public ResponseEntity<Void> removerAluno(
            @PathVariable Long id,
            @PathVariable UUID alunoId) {
        turmaService.removerAluno(id, alunoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/turmas/{id}/alunos/importar")
    public ImportacaoResponse importarAlunos(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return turmaService.importarAlunos(id, file);
    }
}
