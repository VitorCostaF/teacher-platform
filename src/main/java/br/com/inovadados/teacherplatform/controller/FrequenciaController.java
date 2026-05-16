package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.dto.request.LancarFrequenciaRequest;
import br.com.inovadados.teacherplatform.dto.response.FrequenciaResponse;
import br.com.inovadados.teacherplatform.dto.response.HistoricoFrequenciaResponse;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.service.FrequenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/turmas/{turmaId}/frequencia")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'COORDENADOR')")
public class FrequenciaController {

    private final FrequenciaService frequenciaService;
    private final UsuarioRepository usuarioRepository;

    // TODO extrair para um service
    private Usuario getUsuarioAutenticado(Authentication auth) {
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado"));
    }

    @GetMapping
    public ResponseEntity<FrequenciaResponse> buscarPorData(
            @PathVariable Long turmaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return frequenciaService.buscarPorData(turmaId, data)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<FrequenciaResponse> lancarFrequencia(
            @PathVariable Long turmaId,
            @RequestBody @Valid LancarFrequenciaRequest req,
            Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(frequenciaService.lancarFrequencia(turmaId, req, usuario.getId()));
    }

    @PutMapping("/{frequenciaId}")
    public FrequenciaResponse editarFrequencia(
            @PathVariable Long turmaId,
            @PathVariable Long frequenciaId,
            @RequestBody @Valid LancarFrequenciaRequest req,
            Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        return frequenciaService.editarFrequencia(turmaId, frequenciaId, req, usuario.getId());
    }

    @GetMapping("/alunos/{alunoId}")
    public HistoricoFrequenciaResponse buscarHistorico(
            @PathVariable Long turmaId,
            @PathVariable UUID alunoId) {
        return frequenciaService.buscarHistorico(turmaId, alunoId);
    }
}
