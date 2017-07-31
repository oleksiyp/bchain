create table Tx(
    hash blob(32) primary key not null,
    coinbase boolean not null,
    nInputs integer not null,
    nOutputs integer not null
);

create table TxInput(
    hash blob(32) not null,
    n integer not null,
    prevTxHash blob(32) not null,
    outputIndex integer not null,
    signature blob(32) not null,
    PRIMARY KEY (hash, n)
);

create table TxOutput(
    hash blob(32) not null,
    n integer not null,
    modulus blob(64) not null,
    exponent blob(64) not null,
    value int8 not null,
    PRIMARY KEY (hash, n)
);

create table Block (
    hash blob(32) primary key,
    prevBlockHash blob(32),
    nTxs integer not null
);

create table BlockTx (
    hash blob(32) not null,
    n integer not null,
    txHash blob(32) not null,
    PRIMARY KEY (hash, n)
);

create table OrphanedTx (
    hash blob(32) primary key
);

create table OrphanedBlock (
    hash blob(32) primary key
);

