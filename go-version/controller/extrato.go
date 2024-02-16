package controller

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"rinha/dto"
	postgresoperations "rinha/postgres-operations"
	"rinha/utils"

	"github.com/jackc/pgx/v5"
)

func buscarExtrato(id int) (*dto.ExtratoResponse, error) {
	pool := postgresoperations.GetPgOperations()

	results, _ := pool.Db.Query(context.Background(), "SELECT saldo, limite, now() as data_extrato FROM saldos WHERE id = $1 FOR UPDATE", id)
	response, err := pgx.CollectOneRow(results, pgx.RowToStructByName[dto.SaldoResponse])

	if err != nil {
		log.Fatal(err)
		return nil, err
	}

	results, _ = pool.Db.Query(context.Background(), "SELECT valor, tipo, descricao, realizada_em FROM transacoes WHERE id_cliente = $1 ORDER BY realizada_em DESC LIMIT 10 FOR UPDATE", id)

	transacoes, err := pgx.CollectRows(results, pgx.RowToStructByPos[dto.TransacaoResponse])

	if err != nil {
		log.Fatal(err)
		return nil, err
	}

	if transacoes == nil {
		transacoes = []dto.TransacaoResponse{}
	}

	return &dto.ExtratoResponse{Saldo: response, UltimasTransacoes: transacoes}, nil
}

func Extrato(w http.ResponseWriter, r *http.Request) {
	id, err := utils.ParseIDFromURL(r.URL.Path)
	if err != nil {
		log.Println(err)
		w.WriteHeader(http.StatusUnprocessableEntity)
		return
	}

	if id < 1 || id > 5 {
		w.WriteHeader(http.StatusNotFound)
		return
	}

	result, err := buscarExtrato(id)
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
