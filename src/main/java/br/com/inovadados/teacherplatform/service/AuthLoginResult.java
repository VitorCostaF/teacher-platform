package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.dto.response.LoginResponse;

public record AuthLoginResult(LoginResponse loginResponse, String refreshToken) {}
