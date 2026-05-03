package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.enums.TipoQuestaoEnum;
import br.com.inovadados.teacherplatform.dto.request.GerarFlashcardsRequest;
import br.com.inovadados.teacherplatform.dto.request.GerarGradeRequest;
import br.com.inovadados.teacherplatform.dto.request.GerarProvaRequest;
import br.com.inovadados.teacherplatform.dto.request.RegerarQuestaoRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PromptBuilderService {

    private static final Map<TipoQuestaoEnum, String> NOMES_TIPO = Map.of(
            TipoQuestaoEnum.MULTIPLA_ESCOLHA, "múltipla escolha (4 alternativas, apenas 1 correta)",
            TipoQuestaoEnum.VERDADEIRO_FALSO, "verdadeiro ou falso",
            TipoQuestaoEnum.DISSERTATIVA, "dissertativa (resposta aberta)",
            TipoQuestaoEnum.UPLOAD_ARQUIVO, "com envio de arquivo"
    );

    public String buildGerarProvaPrompt(GerarProvaRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Você é um especialista em educação brasileira. Gere questões para uma prova com as seguintes especificações:\n\n");
        sb.append("Disciplina: ").append(req.disciplina()).append('\n');
        sb.append("Série/Ano: ").append(req.serie()).append('\n');
        sb.append("Nível de dificuldade: ").append(req.dificuldade()).append('\n');

        if (req.quantidadesPorTipo() != null && !req.quantidadesPorTipo().isEmpty()) {
            sb.append("\nQuantidade de questões por tipo:\n");
            req.quantidadesPorTipo().forEach((tipo, qtd) ->
                    sb.append("- ").append(qtd).append(" questão(ões) de ").append(NOMES_TIPO.getOrDefault(tipo, tipo.name())).append('\n'));
        }

        if (req.topicos() != null && !req.topicos().isEmpty()) {
            sb.append("\nTópicos a abordar:\n");
            req.topicos().forEach(t -> sb.append("- ").append(t).append('\n'));
        }

        if (req.conteudoTexto() != null && !req.conteudoTexto().isBlank()) {
            sb.append("\nConteúdo de referência:\n");
            sb.append(sanitizarConteudo(req.conteudoTexto())).append('\n');
        }

        sb.append("""

Retorne APENAS um JSON válido, sem texto adicional antes ou depois, no formato:
{
  "questoes": [
    {
      "id": "<uuid-v4>",
      "tipo": "MULTIPLA_ESCOLHA",
      "enunciado": "Texto da questão...",
      "alternativas": ["A) Opção 1", "B) Opção 2", "C) Opção 3", "D) Opção 4"],
      "gabarito": 0,
      "dificuldade": "MEDIO",
      "topico": "Nome do tópico",
      "criteriosCorrecao": null
    }
  ]
}

Regras:
- Para MULTIPLA_ESCOLHA: alternativas deve ter 4 itens; gabarito é o índice (0-3) da alternativa correta
- Para VERDADEIRO_FALSO: alternativas deve ser ["Verdadeiro", "Falso"]; gabarito é 0 (verdadeiro) ou 1 (falso)
- Para DISSERTATIVA: alternativas deve ser null; gabarito deve ser null; preencha criteriosCorrecao
- Gere UUIDs válidos no campo id
- Todos os textos em português do Brasil
- Nível de dificuldade de cada questão: FACIL, MEDIO ou DIFICIL
""");

        return sb.toString();
    }

    public String buildRegerarQuestaoPrompt(RegerarQuestaoRequest req) {
        return String.format("""
Você é um especialista em educação brasileira. Gere UMA nova questão diferente da atual, mantendo o mesmo contexto de prova.

Contexto da prova: %s

Questão atual (a ser substituída):
- Tipo: %s
- Enunciado: %s
- Tópico: %s

Gere uma questão diferente do mesmo tipo e tópico. Retorne APENAS um JSON com a nova questão no formato:
{
  "questoes": [
    {
      "id": "<uuid-v4>",
      "tipo": "%s",
      "enunciado": "Novo enunciado...",
      "alternativas": [...],
      "gabarito": <índice ou null>,
      "dificuldade": "%s",
      "topico": "%s",
      "criteriosCorrecao": <null ou texto>
    }
  ]
}
""",
                req.contextoProva(),
                req.questaoAtual().tipo(),
                req.questaoAtual().enunciado(),
                req.questaoAtual().topico(),
                req.questaoAtual().tipo(),
                req.questaoAtual().dificuldade(),
                req.questaoAtual().topico()
        );
    }

    public String buildGerarGradePrompt(GerarGradeRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Você é um especialista em planejamento educacional. Crie uma grade de aulas para:\n\n");
        sb.append("Disciplina: ").append(req.disciplina()).append('\n');
        sb.append("Série: ").append(req.serie()).append('\n');
        sb.append("Total de aulas: ").append(req.totalAulas()).append('\n');
        if (req.topicos() != null && !req.topicos().isEmpty()) {
            sb.append("Tópicos: ").append(String.join(", ", req.topicos())).append('\n');
        }
        sb.append("""

Retorne APENAS um JSON válido no formato:
{
  "grade": [
    {
      "semana": 1,
      "aula": 1,
      "conteudo": "Título do conteúdo",
      "objetivos": "Objetivos de aprendizagem",
      "recursosSugeridos": "Livro didático, vídeo, etc."
    }
  ]
}
""");
        return sb.toString();
    }

    public String buildGerarFlashcardsPrompt(GerarFlashcardsRequest req) {
        return String.format("""
Você é um especialista em educação brasileira. Crie %d flashcards para estudo sobre:

Disciplina: %s
Série: %s
%s

Retorne APENAS um JSON válido no formato:
{
  "flashcards": [
    {
      "frente": "Pergunta ou conceito",
      "verso": "Resposta ou explicação"
    }
  ]
}
""",
                req.quantidade(),
                req.disciplina(),
                req.serie(),
                req.conteudoTexto() != null ? "Conteúdo: " + sanitizarConteudo(req.conteudoTexto()) : ""
        );
    }

    public String sanitizarConteudo(String texto) {
        if (texto == null) return "";
        return texto
                .replaceAll("<[^>]+>", "")
                .replaceAll("(?i)(SELECT|INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|EXEC|UNION)\\s", "[SQL] ")
                .replaceAll("\\b\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}\\b", "[CPF]")
                .replaceAll("\\b\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}\\b", "[CNPJ]")
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                .trim();
    }
}
