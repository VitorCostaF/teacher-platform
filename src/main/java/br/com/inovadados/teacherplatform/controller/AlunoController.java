package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.dto.request.AvaliacaoFlashcardRequest;
import br.com.inovadados.teacherplatform.dto.response.DesempenhoAlunoSimplesResponse;
import br.com.inovadados.teacherplatform.dto.response.FeedAlunoResponse;
import br.com.inovadados.teacherplatform.dto.response.FlashcardResponse;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.service.FeedAlunoService;
import br.com.inovadados.teacherplatform.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/aluno")
@RequiredArgsConstructor
public class AlunoController {

    private final FeedAlunoService feedAlunoService;
    private final FlashcardService flashcardService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/feed")
    @PreAuthorize("hasRole('ALUNO')")
    public FeedAlunoResponse getFeed(Authentication authentication) {
        UUID alunoId = getAlunoId(authentication);
        return feedAlunoService.getFeed(alunoId);
    }

    @GetMapping("/desempenho")
    @PreAuthorize("hasRole('ALUNO')")
    public DesempenhoAlunoSimplesResponse getDesempenho(Authentication authentication) {
        UUID alunoId = getAlunoId(authentication);
        return feedAlunoService.getDesempenho(alunoId);
    }

    @GetMapping("/flashcards")
    @PreAuthorize("hasRole('ALUNO')")
    public List<FlashcardResponse> getFlashcards(
            @RequestParam(required = false) Long turmaId,
            Authentication authentication) {
        UUID alunoId = getAlunoId(authentication);
        return flashcardService.getFlashcardsPriorizados(alunoId, turmaId);
    }

    @PostMapping("/flashcards/{cardId}/avaliacao")
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<Void> avaliarFlashcard(
            @PathVariable Long cardId,
            @RequestBody AvaliacaoFlashcardRequest request,
            Authentication authentication) {
        UUID alunoId = getAlunoId(authentication);
        flashcardService.registrarAvaliacao(alunoId, cardId, request.sabia());
        return ResponseEntity.noContent().build();
    }

    private UUID getAlunoId(Authentication authentication) {
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"))
                .getId();
    }
}
