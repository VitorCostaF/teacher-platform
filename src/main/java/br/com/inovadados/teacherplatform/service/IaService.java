package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.dto.request.GerarFlashcardsRequest;
import br.com.inovadados.teacherplatform.dto.request.GerarGradeRequest;
import br.com.inovadados.teacherplatform.dto.request.GerarProvaRequest;
import br.com.inovadados.teacherplatform.dto.request.RegerarQuestaoRequest;
import br.com.inovadados.teacherplatform.dto.response.GeracaoResponse;
import br.com.inovadados.teacherplatform.dto.response.GradeAulaDto;
import br.com.inovadados.teacherplatform.dto.response.QuestaoGeradaDto;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class IaService {

    private static final Logger log = LoggerFactory.getLogger(IaService.class);
    private static final Pattern QUESTOES_PATTERN =
            Pattern.compile("\"questoes\"\\s*:\\s*(\\[.+?\\])", Pattern.DOTALL);
    private static final Pattern GRADE_PATTERN =
            Pattern.compile("\"grade\"\\s*:\\s*(\\[.+?\\])", Pattern.DOTALL);

    private final ClaudeApiService claudeApiService;
    private final PromptBuilderService promptBuilderService;
    private final IaRateLimitService iaRateLimitService;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public GeracaoResponse gerarProva(GerarProvaRequest req, UUID professorId) {
        iaRateLimitService.verificarLimite(professorId);
        String prompt = promptBuilderService.buildGerarProvaPrompt(req);
        ClaudeApiService.ClaudeResult result = claudeApiService.gerarConteudo(prompt);
        List<QuestaoGeradaDto> questoes = parseQuestoes(result.text());
        logUso(professorId, "/ia/gerar-prova", result.tokensUsados());
        return new GeracaoResponse(questoes, result.tokensUsados());
    }

    public GeracaoResponse regenerarQuestao(RegerarQuestaoRequest req, UUID professorId) {
        iaRateLimitService.verificarLimite(professorId);
        String prompt = promptBuilderService.buildRegerarQuestaoPrompt(req);
        ClaudeApiService.ClaudeResult result = claudeApiService.gerarConteudo(prompt);
        List<QuestaoGeradaDto> questoes = parseQuestoes(result.text());
        logUso(professorId, "/ia/regenerar-questao", result.tokensUsados());
        return new GeracaoResponse(questoes, result.tokensUsados());
    }

    public List<GradeAulaDto> gerarGrade(GerarGradeRequest req, UUID professorId) {
        iaRateLimitService.verificarLimite(professorId);
        String prompt = promptBuilderService.buildGerarGradePrompt(req);
        ClaudeApiService.ClaudeResult result = claudeApiService.gerarConteudo(prompt);
        List<GradeAulaDto> grade = parseGrade(result.text());
        logUso(professorId, "/ia/gerar-grade", result.tokensUsados());
        return grade;
    }

    public GeracaoResponse gerarFlashcards(GerarFlashcardsRequest req, UUID professorId) {
        iaRateLimitService.verificarLimite(professorId);
        String prompt = promptBuilderService.buildGerarFlashcardsPrompt(req);
        ClaudeApiService.ClaudeResult result = claudeApiService.gerarConteudo(prompt);
        logUso(professorId, "/ia/gerar-flashcards", result.tokensUsados());
        return new GeracaoResponse(Collections.emptyList(), result.tokensUsados());
    }

    private List<QuestaoGeradaDto> parseQuestoes(String jsonText) {
        try {
            String cleaned = extrairJson(jsonText);
            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode questoesNode = root.get("questoes");
            if (questoesNode != null && questoesNode.isArray()) {
                return objectMapper.convertValue(questoesNode, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.warn("Falha ao parsear JSON principal, tentando extração via regex: {}", e.getMessage());
            Matcher m = QUESTOES_PATTERN.matcher(jsonText);
            if (m.find()) {
                try {
                    return objectMapper.readValue(m.group(1), new TypeReference<>() {});
                } catch (Exception ex) {
                    log.error("Falha na extração via regex: {}", ex.getMessage());
                }
            }
        }
        return Collections.emptyList();
    }

    private List<GradeAulaDto> parseGrade(String jsonText) {
        try {
            String cleaned = extrairJson(jsonText);
            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode gradeNode = root.get("grade");
            if (gradeNode != null && gradeNode.isArray()) {
                return objectMapper.convertValue(gradeNode, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.warn("Falha ao parsear grade: {}", e.getMessage());
            Matcher m = GRADE_PATTERN.matcher(jsonText);
            if (m.find()) {
                try {
                    return objectMapper.readValue(m.group(1), new TypeReference<>() {});
                } catch (Exception ex) {
                    log.error("Falha na extração via regex (grade): {}", ex.getMessage());
                }
            }
        }
        return Collections.emptyList();
    }

    private String extrairJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private void logUso(UUID professorId, String endpoint, int tokens) {
        try {
            iaRateLimitService.incrementar(professorId);
            Usuario professor = usuarioRepository.findById(professorId)
                    .orElseThrow(() -> new UnauthorizedException("Professor não encontrado"));
            iaRateLimitService.registrarUso(professorId, professor.getEscola().getId(), endpoint, tokens);
        } catch (UnauthorizedException e) {
            log.warn("Não foi possível registrar uso de IA: {}", e.getMessage());
        }
    }
}
