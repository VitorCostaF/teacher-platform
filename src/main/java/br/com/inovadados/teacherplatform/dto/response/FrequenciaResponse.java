package br.com.inovadados.teacherplatform.dto.response;

import br.com.inovadados.teacherplatform.dto.request.FrequenciaAlunoDto;

import java.time.LocalDate;
import java.util.List;

public record FrequenciaResponse(
        Long id,
        LocalDate data,
        List<FrequenciaAlunoDto> alunos
) {}
