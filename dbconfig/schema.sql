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

INSERT INTO saldos (cliente_id, valor, limite)
        VALUES (1, 0, 80000),
               (2, 0, 100000),
               (3, 0, 1000000),
               (4, 0, 500000),
               (5, 0, 10000000);
