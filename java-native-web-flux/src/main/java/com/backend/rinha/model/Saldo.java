package com.backend.rinha.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("saldos")
public class Saldo {
    @Id
    private final Integer id;
    @Column("cliente_id")
    private final int clienteId;
    private final int valor;
    private final int limite;

    public Saldo(int id, int clienteId, int valor, int limite) {
        this.id = id;
        this.clienteId = clienteId;
        this.valor = valor;
        this.limite = limite;
    }

    public int getId() {
        return id;
    }

    public int getCliente_id() {
        return clienteId;
    }

    public int getValor() {
        return valor;
    }

    public int getLimite() {
        return limite;
    }

    public Saldo withSaldo(int novoSaldo) {
        return new Saldo(id, clienteId, novoSaldo, limite);
    }
}
