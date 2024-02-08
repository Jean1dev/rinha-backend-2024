package com.backend.rinha.dto;

import java.util.List;

public record ExtratoOutputDto (
        SaldoDto saldo,
        List<TransacaoDto> ultimas_transacoes
){}
