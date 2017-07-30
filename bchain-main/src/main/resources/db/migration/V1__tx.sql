create table Tx(
    hash blob(32) primary key,
    coinbase boolean,
    nInputs integer,
    nOutputs integer
);

create table TxInput(
    hash blob(32),
    n integer,
    prevTxHash blob(32),
    outputIndex integer,
    signature blob(32),
    PRIMARY KEY (hash, n)
);

create table TxOutput(
    hash blob(32),
    n integer,
    modulus blob(64),
    exponent blob(64),
    value int8,
    PRIMARY KEY (hash, n)
);

