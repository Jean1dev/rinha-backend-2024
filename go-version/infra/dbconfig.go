package infra

import (
	"context"
	"log"
	"os"

	"github.com/jackc/pgx/v5/pgxpool"
	_ "github.com/lib/pq"
)

func StartDbPool() (*pgxpool.Pool, error) {
	databaseUrl := os.Getenv("DATABASE_URL")
	if databaseUrl == "" {
		databaseUrl = "postgres://jeanfernandes:1234@localhost:5432/app?sslmode=disable"
	}

	log.Print(databaseUrl)
	db, err := pgxpool.New(context.Background(), databaseUrl)

	if err != nil {
		return nil, err
	}

	return db, nil
}
