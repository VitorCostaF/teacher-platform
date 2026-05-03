package br.com.inovadados.teacherplatform.dto.response;

import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email, String avatarUrl) {}
