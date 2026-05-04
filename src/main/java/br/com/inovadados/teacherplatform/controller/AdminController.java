package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.domain.entity.Escola;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.dto.request.AlterarStatusProfessorRequest;
import br.com.inovadados.teacherplatform.dto.request.ConfiguracoesEscolaRequest;
import br.com.inovadados.teacherplatform.dto.request.ConvidarProfessorRequest;
import br.com.inovadados.teacherplatform.dto.request.CriarAlunoRequest;
import br.com.inovadados.teacherplatform.dto.request.TransferirAlunoRequest;
import br.com.inovadados.teacherplatform.dto.response.AdminDashboardResponse;
import br.com.inovadados.teacherplatform.dto.response.ImportacaoResponse;
import br.com.inovadados.teacherplatform.dto.response.UsuarioResponse;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboard(Authentication authentication) {
        return adminService.getDashboard(getEscolaId(authentication));
    }

    @GetMapping("/professores")
    public Page<UsuarioResponse> listarProfessores(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable,
            Authentication authentication) {
        return adminService.listarProfessores(getEscolaId(authentication), nome, ativo, pageable);
    }

    @PostMapping("/professores/convidar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> convidarProfessor(
            @Valid @RequestBody ConvidarProfessorRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UsuarioResponse resp = adminService.convidarProfessor(request, getEscolaId(authentication), httpRequest);
        return ResponseEntity.status(201).body(resp);
    }

    @PatchMapping("/professores/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse alterarStatusProfessor(
            @PathVariable UUID id,
            @Valid @RequestBody AlterarStatusProfessorRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return adminService.alterarStatusProfessor(id, request, getEscolaId(authentication),
                getUsuarioId(authentication), httpRequest);
    }

    @PostMapping("/professores/importar")
    @PreAuthorize("hasRole('ADMIN')")
    public ImportacaoResponse importarProfessores(
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            HttpServletRequest httpRequest) throws IOException {
        return adminService.importarProfessores(file, getEscolaId(authentication), httpRequest);
    }

    @GetMapping("/alunos")
    public Page<UsuarioResponse> listarAlunos(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable,
            Authentication authentication) {
        return adminService.listarAlunos(getEscolaId(authentication), nome, ativo, pageable);
    }

    @PostMapping("/alunos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> criarAluno(
            @Valid @RequestBody CriarAlunoRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UsuarioResponse resp = adminService.criarAluno(request, getEscolaId(authentication),
                getUsuarioId(authentication), httpRequest);
        return ResponseEntity.status(201).body(resp);
    }

    @PatchMapping("/alunos/{id}/turma")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse transferirAluno(
            @PathVariable UUID id,
            @Valid @RequestBody TransferirAlunoRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return adminService.transferirAluno(id, request, getEscolaId(authentication),
                getUsuarioId(authentication), httpRequest);
    }

    @PostMapping("/alunos/importar")
    @PreAuthorize("hasRole('ADMIN')")
    public ImportacaoResponse importarAlunos(
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            HttpServletRequest httpRequest) throws IOException {
        return adminService.importarAlunos(file, getEscolaId(authentication),
                getUsuarioId(authentication), httpRequest);
    }

    @GetMapping("/escola/configuracoes")
    public Escola getConfiguracoes(Authentication authentication) {
        return adminService.getConfiguracoes(getEscolaId(authentication));
    }

    @PutMapping("/escola/configuracoes")
    @PreAuthorize("hasRole('ADMIN')")
    public Escola atualizarConfiguracoes(
            @Valid @RequestBody ConfiguracoesEscolaRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return adminService.atualizarConfiguracoes(getEscolaId(authentication), request,
                getUsuarioId(authentication), httpRequest);
    }

    private Long getEscolaId(Authentication authentication) {
        return getUsuario(authentication).getEscola().getId();
    }

    private UUID getUsuarioId(Authentication authentication) {
        return getUsuario(authentication).getId();
    }

    private Usuario getUsuario(Authentication authentication) {
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
    }
}
