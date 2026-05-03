package br.com.inovadados.teacherplatform.dto.request;

import java.util.UUID;

public record AdicionarAlunoRequest(UUID alunoId, String email) {}
