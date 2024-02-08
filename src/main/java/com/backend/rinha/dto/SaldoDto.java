package com.backend.rinha.dto;

import java.time.LocalDateTime;

public record SaldoDto(
        int total,
        LocalDateTime data_extrato,
        int limite
) {
}
