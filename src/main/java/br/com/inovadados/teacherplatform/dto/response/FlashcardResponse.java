package br.com.inovadados.teacherplatform.dto.response;

public record FlashcardResponse(
        Long id,
        String pergunta,
        String resposta,
        String topico
) {}
