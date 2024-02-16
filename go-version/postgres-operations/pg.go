package postgresoperations

import "github.com/jackc/pgx/v5/pgxpool"

var (
	singleton *PgOp
)

type PgOp struct {
	Db *pgxpool.Pool
}

func GetPgOperations() *PgOp {
	return singleton
}

func Instanciar(db *pgxpool.Pool) {
	singleton = &PgOp{Db: db}
}
