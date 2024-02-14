package com.backend.rinha.model;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;

public interface TransacaoRepository extends
        ReactiveSortingRepository<Transacao, Integer>,
        ReactiveCrudRepository<Transacao, Integer> {
    Flux<Transacao> findAllByClienteId(Integer cliente_id, Pageable pageable);
}
