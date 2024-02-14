package com.backend.rinha;

import com.backend.rinha.dto.*;
import com.backend.rinha.model.Saldo;
import com.backend.rinha.model.SaldoRepository;
import com.backend.rinha.model.Transacao;
import com.backend.rinha.model.TransacaoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
public class ServiceApplication {

    private final TransacaoRepository transacaoRepository;
    private final SaldoRepository saldoRepository;
    private static final ConcurrentLinkedQueue<Transacao> transacaos = new ConcurrentLinkedQueue<>();

    public ServiceApplication(TransacaoRepository transacaoRepository, SaldoRepository saldoRepository) {
        this.transacaoRepository = transacaoRepository;
        this.saldoRepository = saldoRepository;
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Mono<ExtratoOutputDto> extrato(Integer cliente_id) {
        validateId(cliente_id);
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("id")));
        return saldoRepository.findByClienteId(cliente_id)
                .cache()
                .subscribeOn(Schedulers.parallel())
                .map(saldo ->
                        new ExtratoOutputDto(new SaldoDto(saldo.getValor(), LocalDateTime.now(), saldo.getLimite()), null))
                .map(extratoOutputDto -> {
                    Mono<ExtratoOutputDto> dtoMono = transacaoRepository.findAllByClienteId(cliente_id, pageable)
                            .cache()
                            .subscribeOn(Schedulers.parallel())
                            .map(transacao -> new TransacaoDto(transacao.getValor(), transacao.getTipo(), transacao.getDescricao(), transacao.getRealizada_em()))
                            .collect(Collectors.toUnmodifiableList())
                            .map(transacaoDtos1 -> new ExtratoOutputDto(
                                            extratoOutputDto.saldo(),
                                            transacaoDtos1
                                    )
                            );

                    return dtoMono;
                }).flatMap(extratoOutputDtoMono -> extratoOutputDtoMono)
                .onErrorResume(throwable -> {
                    throw new HttpClientErrorException(HttpStatusCode.valueOf(404), throwable.getLocalizedMessage());
                });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Mono<TransacaoOutputDto> makeTransacao(Integer cliente_id, TransacaoInputDto dto) {
        validateId(cliente_id);
        validDto(dto);
        Mono<Transacao> saved = inserirTransacao(new Transacao(null, cliente_id, dto.valor(), dto.tipo(), dto.descricao(), LocalDateTime.now()));
        return saldoRepository.findByClienteId(cliente_id)
                .flatMap(saldo -> {
                    if ('d' == dto.tipo()) {
                        return Mono.just(realizarTransacaoNoDebito(saldo, dto, cliente_id));
                    } else {
                        return Mono.just(realizarTransacaoNoCredito(saldo, dto, cliente_id));
                    }
                })
                .flatMap(saldoRepository::save)
                .flatMap(saved::thenReturn)
                .flatMap(saldo -> Mono.just(new TransacaoOutputDto(saldo.getLimite(), saldo.getValor())))
                .onErrorResume(throwable -> {
                    throw new HttpClientErrorException(HttpStatusCode.valueOf(422), throwable.getLocalizedMessage());
                });
    }

    private void validDto(TransacaoInputDto dto) {
        if (Objects.isNull(dto.descricao()) || dto.descricao().length() > 10) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(422), "Descrição muito longa");
        }

        if ('c' != dto.tipo() && 'd' != dto.tipo()) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(422), "Tipo inválido");
        }
    }

    private Mono<Transacao> inserirTransacao(Transacao build) {
        return transacaoRepository.save(build);
    }

    private Saldo realizarTransacaoNoCredito(Saldo saldo, TransacaoInputDto dto, Integer cliente_id) {
        var novoLimite = saldo.getValor() + dto.valor();
        return saldo.withSaldo(novoLimite);
    }

    private Saldo realizarTransacaoNoDebito(Saldo saldo, TransacaoInputDto dto, Integer cliente_id) {
        var novoSaldo = saldo.getValor() - dto.valor();

        if (saldo.getLimite() + novoSaldo < 0) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(422), "Saldo insuficiente");
        }
        return saldo.withSaldo(novoSaldo);
    }

    private void validateId(Integer clienteId) {
        if (clienteId < 1 || clienteId > 5) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(404), "Cliente não encontrado");
        }
    }
}
