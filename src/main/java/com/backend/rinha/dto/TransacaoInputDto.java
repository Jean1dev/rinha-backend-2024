package com.backend.rinha.dto;

public record TransacaoInputDto(
        int valor,
        char tipo,
        String descricao
) {
}
