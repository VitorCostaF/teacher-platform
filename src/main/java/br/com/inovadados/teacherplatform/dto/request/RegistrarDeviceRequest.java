package br.com.inovadados.teacherplatform.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegistrarDeviceRequest(
    @NotBlank String endpoint,
    @NotBlank String p256dh,
    @NotBlank String auth
) {}
