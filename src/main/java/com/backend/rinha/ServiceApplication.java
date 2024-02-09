package com.backend.rinha;

import com.backend.rinha.dto.*;
import com.backend.rinha.model.Saldo;
import com.backend.rinha.model.Transacao;
import org.springframework.http.HttpStatusCode;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class ServiceApplication {

    private final JdbcTemplate jdbcTemplate;
    private static final ConcurrentLinkedQueue<Transacao> transacaos = new ConcurrentLinkedQueue<>();

    public ServiceApplication(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedDelay = 1000, initialDelay = 2000)
    public void agendarInsercaoTransacao() {
        final Queue<Transacao> batchQueue = new ConcurrentLinkedQueue<>();
        transacaos.forEach(transacao -> {
            batchQueue.add(transacaos.poll());
        });

        batchInsertTransacoes(batchQueue);
    }

    public ExtratoOutputDto extrato(Long cliente_id) {
        Optional<Saldo> saldoOpt = jdbcTemplate.query("select valor, limite from saldos where cliente_id = ?",
                rs -> rs.next() ? Optional.ofNullable(Saldo.simplificado.mapRow(rs, 1)) : Optional.empty(),
                cliente_id);

        if (saldoOpt.isEmpty()) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(404), "Cliente não encontrado");
        }

        List<TransacaoDto> transacaoDtos = jdbcTemplate.query("select valor, tipo, descricao, realizada_em from transacoes where cliente_id = ? ORDER BY id DESC LIMIT 10 ",
                        Transacao.mapper,
                        cliente_id)
                .stream()
                .map(transacao -> new TransacaoDto(transacao.getValor(), transacao.getTipo(), transacao.getDescricao(), transacao.getRealizada_em()))
                .collect(Collectors.toUnmodifiableList());

        return new ExtratoOutputDto(
                new SaldoDto(saldoOpt.get().getValor(), LocalDateTime.now(), saldoOpt.get().getLimite()),
                transacaoDtos
        );
    }

    public TransacaoOutputDto makeTransacao(Long cliente_id, TransacaoInputDto dto) {
        validDto(dto);
        Optional<Saldo> saldoOpt = jdbcTemplate.query("select valor, limite from saldos where cliente_id = ?  FOR UPDATE NOWAIT",
                rs -> rs.next() ? Optional.ofNullable(Saldo.simplificado.mapRow(rs, 1)) : Optional.empty(),
                cliente_id);

        if (saldoOpt.isEmpty()) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(404), "Cliente não encontrado");
        }

        var saldo = saldoOpt.get();
        TransacaoOutputDto output;
        if ('d' == dto.tipo()) {
            output = realizarTransacaoNoDebito(saldo, dto, cliente_id);
        } else {
            output = realizarTransacaoNoCredito(saldo, dto, cliente_id);
        }

        //transacaos.add(Transacao.build(cliente_id, dto.valor(), dto.tipo(), dto.descricao(), null));
        inserirTransacao(Transacao.build(cliente_id, dto.valor(), dto.tipo(), dto.descricao(), null));
        return output;
    }

    private void validDto(TransacaoInputDto dto) {
        if (dto.descricao().length() > 10) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(422), "Descrição muito longa");
        }

        if ('c' != dto.tipo() && 'd' != dto.tipo()) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(422), "Tipo inválido");
        }
    }

    private void inserirTransacao(Transacao build) {
        jdbcTemplate.update("insert into transacoes (cliente_id, valor, tipo, descricao) values (?, ?, ?, ?)", build.getCliente_id(), build.getValor(), build.getTipo(), build.getDescricao());
    }

    private TransacaoOutputDto realizarTransacaoNoCredito(Saldo saldo, TransacaoInputDto dto, Long cliente_id) {
        var novoLimite = saldo.getLimite() - dto.valor();
        jdbcTemplate.execute("update saldos set limite = %s where cliente_id = %s".formatted(novoLimite, cliente_id));
        return new TransacaoOutputDto(novoLimite, saldo.getValor());
    }

    private TransacaoOutputDto realizarTransacaoNoDebito(Saldo saldo, TransacaoInputDto dto, Long cliente_id) {
        var novoSaldo = saldo.getValor() - dto.valor();

        if (saldo.getLimite() + novoSaldo < 0) {
            throw new HttpClientErrorException(HttpStatusCode.valueOf(422), "Saldo insuficiente");
        }

        jdbcTemplate.execute("update saldos set valor = %s where cliente_id = %s".formatted(novoSaldo, cliente_id));
        return new TransacaoOutputDto(saldo.getLimite(), novoSaldo);
    }

    private void batchInsertTransacoes(Queue<Transacao> batchQueue) {
        if (batchQueue.isEmpty())
            return;

        jdbcTemplate.batchUpdate("insert into transacoes (cliente_id, valor, tipo, descricao) values (?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Transacao polled = batchQueue.poll();
                ps.setObject(1, polled.getCliente_id());
                ps.setObject(2, polled.getValor());
                ps.setObject(3, polled.getTipo());
                ps.setObject(4, polled.getDescricao());
            }

            @Override
            public int getBatchSize() {
                return batchQueue.size();
            }
        });
    }
}
