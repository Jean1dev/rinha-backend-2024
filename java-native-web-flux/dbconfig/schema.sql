CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE transacoes (
	id SERIAL PRIMARY KEY,
	cliente_id INTEGER NOT NULL,
	valor INTEGER NOT NULL,
	tipo CHAR(1) NOT NULL,
	descricao VARCHAR(10) NOT NULL,
	realizada_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE saldos (
	id SERIAL PRIMARY KEY,
	cliente_id INTEGER NOT NULL,
	valor INTEGER NOT NULL,
	limite INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS cliente_id ON saldos (cliente_id);

INSERT INTO saldos (id, cliente_id, valor, limite)
        VALUES (1, 1, 0, 1000 * 100),
               (2, 2, 0, 800 * 100),
               (3, 3, 0, 10000 * 100),
               (4, 4, 0, 100000 * 100),
               (5, 5, 0, 5000 * 100);
