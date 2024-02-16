package infra

import (
	"log"
	"net/http"
	"os"
	"rinha/controller"
	postgresoperations "rinha/postgres-operations"
)

func routes() {
	http.HandleFunc("/clientes/{id}/transacoes", controller.CreateTransacao)
	http.HandleFunc("/clientes/{id}/extrato", controller.Extrato)
}

func StartServer() {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	log.Print("server running on ", port)

	db, err := StartDbPool()
	if err != nil {
		panic(err)
	}
	defer db.Close()

	postgresoperations.Instanciar(db)
	routes()

	if err := http.ListenAndServe(":"+port, nil); err != nil {
		panic(err)
	}
}
