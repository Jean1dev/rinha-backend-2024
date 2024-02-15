package com.backend.rinha.model;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SaldoRepository extends ReactiveCrudRepository<Saldo, Integer> {
    Mono<Saldo> findByClienteId(Integer cliente_id);
}
