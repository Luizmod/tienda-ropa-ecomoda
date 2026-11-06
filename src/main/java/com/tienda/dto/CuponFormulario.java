package com.tienda.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record CuponFormulario(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,40}") String codigo,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2)
        BigDecimal porcentaje,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimiento,
        @NotNull Boolean activo) {
}
