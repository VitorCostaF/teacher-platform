package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.exception.IaIndisponibilException;
import br.com.inovadados.teacherplatform.exception.IaRateLimitException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaudeApiService {

    @Value("${app.ia.api-key}")
    private String apiKey;

    @Value("${app.ia.api-url}")
    private String apiUrl;

    @Value("${app.ia.model}")
    private String model;

    @Value("${app.ia.max-tokens}")
    private int maxTokens;

    private final WebClient.Builder webClientBuilder;

    public ClaudeResult gerarConteudo(String prompt) {
        var requestBody = new MessageRequest(
                model,
                maxTokens,
                List.of(new MessageRequest.Message("user", prompt))
        );

        MessageResponse response = webClientBuilder.build()
                .post()
                .uri(apiUrl)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.value() == 429,
                        resp -> Mono.error(new IaRateLimitException()))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new IaIndisponibilException()))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> resp.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Claude API error: " + body)))
                .bodyToMono(MessageResponse.class)
                .block();

        if (response == null || response.content() == null || response.content().isEmpty()) {
            throw new IaIndisponibilException();
        }

        String text = response.content().stream()
                .filter(c -> "text".equals(c.type()))
                .map(MessageResponse.ContentBlock::text)
                .findFirst()
                .orElseThrow(IaIndisponibilException::new);

        int tokensUsados = response.usage() != null ? response.usage().outputTokens() : 0;
        return new ClaudeResult(text, tokensUsados);
    }

    public record ClaudeResult(String text, int tokensUsados) {}

    private record MessageRequest(
            String model,
            @JsonProperty("max_tokens") int maxTokens,
            List<Message> messages
    ) {
        record Message(String role, String content) {}
    }

    private record MessageResponse(
            List<ContentBlock> content,
            UsageBlock usage
    ) {
        record ContentBlock(String type, String text) {}

        record UsageBlock(
                @JsonProperty("input_tokens") int inputTokens,
                @JsonProperty("output_tokens") int outputTokens
        ) {}
    }
}
