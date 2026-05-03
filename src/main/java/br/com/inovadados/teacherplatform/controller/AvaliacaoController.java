package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.dto.request.PublicarAvaliacaoRequest;
import br.com.inovadados.teacherplatform.dto.request.SalvarRascunhoRequest;
import br.com.inovadados.teacherplatform.dto.response.AvaliacaoResponse;
import br.com.inovadados.teacherplatform.dto.response.PreviewAvaliacaoResponse;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.service.AvaliacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'COORDENADOR')")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioAutenticado(Authentication auth) {
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado"));
    }

    @PostMapping("/provas/rascunho")
    public ResponseEntity<AvaliacaoResponse> criarRascunhoProva(
            @RequestBody @Valid SalvarRascunhoRequest req,
            Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(avaliacaoService.salvarRascunho(req, usuario.getId()));
    }

    @PostMapping("/atividades/rascunho")
    public ResponseEntity<AvaliacaoResponse> criarRascunhoAtividade(
            @RequestBody @Valid SalvarRascunhoRequest req,
            Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(avaliacaoService.salvarRascunho(req, usuario.getId()));
    }

    @PutMapping("/provas/{id}/rascunho")
    public AvaliacaoResponse atualizarRascunho(
            @PathVariable Long id,
            @RequestBody @Valid SalvarRascunhoRequest req,
            Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        return avaliacaoService.atualizarRascunho(id, req, usuario.getId());
    }

    @GetMapping("/provas/{id}")
    public AvaliacaoResponse buscarAvaliacao(@PathVariable Long id) {
        return avaliacaoService.buscarAvaliacao(id);
    }

    @GetMapping("/provas/{id}/preview")
    public PreviewAvaliacaoResponse preview(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String seed) {
        UUID seedUuid = seed.isBlank() ? UUID.randomUUID() : UUID.nameUUIDFromBytes(seed.getBytes());
        return avaliacaoService.preview(id, seedUuid);
    }

    @PostMapping("/provas/{id}/publicar")
    public AvaliacaoResponse publicar(
            @PathVariable Long id,
            @RequestBody @Valid PublicarAvaliacaoRequest req) {
        return avaliacaoService.publicar(id, req);
    }
}
