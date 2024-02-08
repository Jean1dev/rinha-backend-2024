package com.backend.rinha.dto;

import java.time.LocalDateTime;

public record TransacaoDto(
        int valor,
        char tipo,
        String descricao,
        LocalDateTime realizada_em
) {
}
