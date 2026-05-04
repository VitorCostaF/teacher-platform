package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.dto.request.AutosaveProvaRequest;
import br.com.inovadados.teacherplatform.dto.request.SalvarRascunhoAtividadeRequest;
import br.com.inovadados.teacherplatform.dto.response.AtividadeDetalheResponse;
import br.com.inovadados.teacherplatform.dto.response.EntregarAtividadeResponse;
import br.com.inovadados.teacherplatform.dto.response.ResultadoEntregaResponse;
import br.com.inovadados.teacherplatform.dto.response.SessaoProvaResponse;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.service.AtividadeService;
import br.com.inovadados.teacherplatform.service.SessaoProvaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class EntregaController {

    private final AtividadeService atividadeService;
    private final SessaoProvaService sessaoProvaService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/atividades/{id}")
    @PreAuthorize("hasRole('ALUNO')")
    public AtividadeDetalheResponse getAtividade(
            @PathVariable Long id,
            Authentication authentication) {
        return atividadeService.getAtividade(id, getAlunoId(authentication));
    }

    @PutMapping("/atividades/{id}/rascunho")
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<Void> salvarRascunho(
            @PathVariable Long id,
            @RequestBody SalvarRascunhoAtividadeRequest request,
            Authentication authentication) {
        atividadeService.salvarRascunho(id, request, getAlunoId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/atividades/{id}/entregar")
    @PreAuthorize("hasRole('ALUNO')")
    public EntregarAtividadeResponse entregar(
            @PathVariable Long id,
            @RequestBody SalvarRascunhoAtividadeRequest request,
            Authentication authentication) {
        return atividadeService.entregar(id, request, getAlunoId(authentication));
    }

    @PostMapping("/provas/{id}/iniciar")
    @PreAuthorize("hasRole('ALUNO')")
    public SessaoProvaResponse iniciarProva(
            @PathVariable Long id,
            Authentication authentication) {
        return sessaoProvaService.iniciar(id, getAlunoId(authentication));
    }

    @PutMapping("/provas/{id}/sessoes/{sessaoId}/autosave")
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<Void> autosave(
            @PathVariable Long id,
            @PathVariable Long sessaoId,
            @RequestBody AutosaveProvaRequest request,
            Authentication authentication) {
        sessaoProvaService.autosave(sessaoId, request, getAlunoId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/provas/{id}/sessoes/{sessaoId}/entregar")
    @PreAuthorize("hasRole('ALUNO')")
    public EntregarAtividadeResponse entregarProva(
            @PathVariable Long id,
            @PathVariable Long sessaoId,
            Authentication authentication) {
        return sessaoProvaService.entregar(id, sessaoId, getAlunoId(authentication));
    }

    @GetMapping("/aluno/avaliacoes/{entregaId}/resultado")
    @PreAuthorize("hasRole('ALUNO')")
    public ResultadoEntregaResponse getResultado(
            @PathVariable Long entregaId,
            Authentication authentication) {
        return atividadeService.getResultado(entregaId, getAlunoId(authentication));
    }

    private UUID getAlunoId(Authentication authentication) {
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"))
                .getId();
    }
}
