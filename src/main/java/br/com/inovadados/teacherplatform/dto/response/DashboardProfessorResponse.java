package br.com.inovadados.teacherplatform.dto.response;

import java.util.List;

public record DashboardProfessorResponse(
        List<AlertaDashboardDto> alertas,
        List<TurmaDashboardDto> turmas,
        List<MediaHistoricaDto> mediasHistoricas,
        List<MapaCalorDto> mapaCalor
) {}
