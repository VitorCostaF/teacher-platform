package br.com.inovadados.teacherplatform.dto.response;

public record LoginResponse(
    String accessToken,
    int expiresIn,
    String perfil,
    UsuarioResponse usuario
) {}
