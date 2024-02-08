package com.backend.rinha.model;

import org.springframework.jdbc.core.RowMapper;

public class Saldo {
    private final int id;
    private final int cliente_id;
    private final int valor;
    private final int limite;

    public Saldo(int id, int clienteId, int valor, int limite) {
        this.id = id;
        cliente_id = clienteId;
        this.valor = valor;
        this.limite = limite;
    }

    public static RowMapper<Saldo> mapper = (
            (rs, rowNum) -> new Saldo(rs.getInt("id"), rs.getInt("cliente_id"), rs.getInt("valor"), rs.getInt("limite")));

    public static RowMapper<Saldo> simplificado = (
            (rs, rowNum) -> new Saldo(0, 0, rs.getInt("valor"), rs.getInt("limite")));

    public int getId() {
        return id;
    }

    public int getCliente_id() {
        return cliente_id;
    }

    public int getValor() {
        return valor;
    }

    public int getLimite() {
        return limite;
    }
}
