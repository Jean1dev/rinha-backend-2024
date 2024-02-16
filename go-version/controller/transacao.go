package controller

import (
	"context"
	"encoding/json"
	"errors"
	"log"
	"net/http"
	"rinha/dto"
	postgresoperations "rinha/postgres-operations"
	"rinha/utils"
	"time"

	"github.com/jackc/pgx/v5"
)

func validateDto(transacao dto.TransacaoRequest) error {
	if transacao.Descricao == "" || len(transacao.Descricao) > 10 {
		return errors.New("descricao invalida")
	}

	if transacao.Tipo != "d" && transacao.Tipo != "c" {
		return errors.New("tipo invalido")
	}

	return nil
}

func salvar(transacao dto.TransacaoRequest, id int) (*dto.NovaTransacaoResponse, error) {
	poll := postgresoperations.GetPgOperations()
	transaction, err := poll.Db.Begin(context.Background())

	if err != nil {
		log.Fatal(err)
		return nil, err
	}

	defer transaction.Rollback(context.Background())
	var limite int
	var saldo int
	var novoSaldo int

	_ = transaction.QueryRow(context.Background(), "SELECT limite, saldo FROM saldos WHERE id = $1", id).Scan(&limite, &saldo)

	if transacao.Tipo == "d" {
		novoSaldo = saldo - transacao.Valor
	} else {
		novoSaldo = saldo + transacao.Valor
	}

	if (limite + novoSaldo) < 0 {
		return nil, errors.New("limite excedido")
	}

	batch := &pgx.Batch{}

	now := time.Now()

	batch.Queue("INSERT INTO transacoes (valor, tipo, descricao, realizada_em, id_cliente) VALUES ($1, $2, $3, $4, $5)", transacao.Valor, transacao.Tipo, transacao.Descricao, now, id)
	batch.Queue("UPDATE saldos SET saldo = $1 WHERE id = $2", novoSaldo, id)

	batchResults := transaction.SendBatch(context.Background(), batch)
	_, err = batchResults.Exec()

	if err != nil {
		log.Fatal(err)
		return nil, err
	}

	err = batchResults.Close()

	if err != nil {
		log.Fatal(err)
		return nil, err
	}

	err = transaction.Commit(context.Background())

	if err != nil {
		log.Fatal(err)
		return nil, err
	}

	novaTransacao := dto.NovaTransacaoResponse{
		Limite: limite,
		Saldo:  novoSaldo,
	}

	return &novaTransacao, nil
}

func CreateTransacao(w http.ResponseWriter, r *http.Request) {
	id, err := utils.ParseIDFromURL(r.URL.Path)
	if err != nil {
		log.Println(err)
		w.WriteHeader(http.StatusUnprocessableEntity)
		return
	}

	var transacao dto.TransacaoRequest
	err = json.NewDecoder(r.Body).Decode(&transacao)
	if err != nil {
		http.Error(w, err.Error(), http.StatusUnprocessableEntity)
		return
	}

	if id < 1 || id > 5 {
		w.WriteHeader(http.StatusNotFound)
		return
	}

	err = validateDto(transacao)
	if err != nil {
		w.WriteHeader(http.StatusUnprocessableEntity)
		return
	}

	result, err := salvar(transacao, id)
	if err != nil {
		w.WriteHeader(http.StatusUnprocessableEntity)
		return
	}

	jsonResult, err := json.Marshal(result)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.Write(jsonResult)
}
