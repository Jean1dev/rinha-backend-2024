package dto

import "time"

type TransacaoRequest struct {
	Valor     int    `json:"valor"`
	Tipo      string `json:"tipo"`
	Descricao string `json:"descricao"`
}

type TransacaoResponse struct {
	Valor       int       `json:"valor" db:"valor"`
	Tipo        string    `json:"tipo" db:"tipo"`
	Descricao   string    `json:"descricao" db:"descricao"`
	RealizadaEm time.Time `json:"realizada_em" db:"realizada_em"`
}

type NovaTransacaoResponse struct {
	Limite int `json:"limite"`
	Saldo  int `json:"saldo"`
}

type SaldoResponse struct {
	Total       int       `json:"total" db:"saldo"`
	Limite      int       `json:"limite" db:"limite"`
	DataExtrato time.Time `json:"data_extrato" db:"data_extrato"`
}

type ExtratoResponse struct {
	Saldo             SaldoResponse       `json:"saldo"`
	UltimasTransacoes []TransacaoResponse `json:"ultimas_transacoes"`
}
