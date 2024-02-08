package com.backend.rinha.model;


import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;

public class Transacao {
    private final int id;
    private final Long cliente_id;
    private final int valor;
    private final char tipo;
    private final String descricao;
    private final LocalDateTime realizada_em;

    public Transacao(int id, Long clienteId, int valor, char tipo, String descricao, LocalDateTime realizadaEm) {
        this.id = id;
        cliente_id = clienteId;
        this.valor = valor;
        this.tipo = tipo;
        this.descricao = descricao;
        realizada_em = realizadaEm;
    }

    public static RowMapper<Transacao> mapper = (
            (rs, rowNum) -> {
                char tipo1 = rs.getObject("tipo").toString().charAt(0);
                return Transacao.build(null, rs.getInt("valor"), tipo1, rs.getString("descricao"), rs.getTimestamp("realizada_em").toLocalDateTime());
            }
    );

    public static Transacao build(Long clienteId, int valor, char tipo, String descricao, LocalDateTime realizadaEm) {
        return new Transacao(0, clienteId, valor, tipo, descricao, realizadaEm);
    }

    public int getId() {
        return id;
    }

    public Long getCliente_id() {
        return cliente_id;
    }

    public int getValor() {
        return valor;
    }

    public char getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getRealizada_em() {
        return realizada_em;
    }
}
