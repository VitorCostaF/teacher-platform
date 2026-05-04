package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.dto.response.BoletimResponse;
import br.com.inovadados.teacherplatform.dto.response.CalendarioResponsavelResponse;
import br.com.inovadados.teacherplatform.dto.response.FrequenciaResponsavelResponse;
import br.com.inovadados.teacherplatform.dto.response.PainelResponsavelResponse;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import br.com.inovadados.teacherplatform.service.ResponsavelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/responsavel")
@RequiredArgsConstructor
public class ResponsavelController {

    private final ResponsavelService responsavelService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/alunos")
    public List<PainelResponsavelResponse.AlunoResumoDto> listarAlunos(Authentication authentication) {
        return responsavelService.listarAlunos(getUsuarioId(authentication));
    }

    @GetMapping("/alunos/{alunoId}/painel")
    public PainelResponsavelResponse getPainel(
            @PathVariable UUID alunoId,
            Authentication authentication) {
        return responsavelService.getPainel(getUsuarioId(authentication), alunoId);
    }

    @GetMapping("/alunos/{alunoId}/boletim")
    public BoletimResponse getBoletim(
            @PathVariable UUID alunoId,
            @RequestParam(defaultValue = "atual") String periodo,
            Authentication authentication) {
        return responsavelService.getBoletim(getUsuarioId(authentication), alunoId, periodo);
    }

    @GetMapping("/alunos/{alunoId}/frequencia")
    public FrequenciaResponsavelResponse getFrequencia(
            @PathVariable UUID alunoId,
            Authentication authentication) {
        return responsavelService.getFrequencia(getUsuarioId(authentication), alunoId);
    }

    @GetMapping("/alunos/{alunoId}/calendario")
    public CalendarioResponsavelResponse getCalendario(
            @PathVariable UUID alunoId,
            Authentication authentication) {
        return responsavelService.getCalendario(getUsuarioId(authentication), alunoId);
    }

    private UUID getUsuarioId(Authentication authentication) {
        return usuarioRepository.findByEmail(authentication.getName())
                .map(Usuario::getId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
    }
}
