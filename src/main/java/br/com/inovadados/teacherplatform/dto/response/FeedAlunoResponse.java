package br.com.inovadados.teacherplatform.dto.response;

import java.util.List;

public record FeedAlunoResponse(
        List<ItemFeedDto> urgentes,
        List<ItemFeedDto> paraFazer,
        List<ConteudoFeedDto> novosConteudos,
        List<RecomendacaoDto> recomendados
) {}
