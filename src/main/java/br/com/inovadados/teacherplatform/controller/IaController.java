package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.dto.request.GerarFlashcardsRequest;
import br.com.inovadados.teacherplatform.dto.request.GerarGradeRequest;
import br.com.inovadados.teacherplatform.dto.request.GerarProvaRequest;
import br.com.inovadados.teacherplatform.dto.request.RegerarQuestaoRequest;
import br.com.inovadados.teacherplatform.dto.response.GeracaoResponse;
import br.com.inovadados.teacherplatform.dto.response.GradeAulaDto;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.service.IaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ia")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'COORDENADOR')")
public class IaController {

    private final IaService iaService;
    private final UsuarioRepository usuarioRepository;

    private Usuario getUsuarioAutenticado(Authentication auth) {
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado"));
    }

    @PostMapping("/gerar-prova")
    public GeracaoResponse gerarProva(
            @RequestBody @Valid GerarProvaRequest req,
            Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        return iaService.gerarProva(req, usuario.getId());
    }

    @PostMapping("/regenerar-questao")
    public GeracaoResponse regenerarQuestao(
            @RequestBody @Valid RegerarQuestaoRequest req,
            Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        return iaService.regenerarQuestao(req, usuario.getId());
    }

    @PostMapping("/gerar-grade")
    public List<GradeAulaDto> gerarGrade(
            @RequestBody @Valid GerarGradeRequest req,
            Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        return iaService.gerarGrade(req, usuario.getId());
    }

    @PostMapping("/gerar-flashcards")
    public GeracaoResponse gerarFlashcards(
            @RequestBody @Valid GerarFlashcardsRequest req,
            Authentication authentication) {
        Usuario usuario = getUsuarioAutenticado(authentication);
        return iaService.gerarFlashcards(req, usuario.getId());
    }
}
