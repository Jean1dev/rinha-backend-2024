package com.backend.rinha.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Table("transacoes")
public class Transacao {
    @Id
    private final Integer id;
    @Column("cliente_id")
    private final Integer clienteId;
    private final int valor;
    private final char tipo;
    private final String descricao;
    private final LocalDateTime realizada_em;

    public Transacao(Integer id, Integer clienteId, int valor, char tipo, String descricao, LocalDateTime realizada_em) {
        this.id = id;
        this.clienteId = clienteId;
        this.valor = valor;
        this.tipo = tipo;
        this.descricao = descricao;
        this.realizada_em = realizada_em;
    }


    public static Transacao build(Integer clienteId, int valor, char tipo, String descricao, LocalDateTime realizadaEm) {
        return new Transacao(0, clienteId, valor, tipo, descricao, realizadaEm);
    }

    public int getId() {
        return id;
    }

    public Integer getCliente_id() {
        return clienteId;
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
