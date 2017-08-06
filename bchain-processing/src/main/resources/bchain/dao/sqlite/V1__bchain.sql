create table Tx(
    txId integer primary key not null,
    hash blob(32) not null,
    coinbase boolean not null,
    nInputs integer not null,
    nOutputs integer not null
);

create unique index TxHash on Tx(hash);

create table TxInput(
    txId integer not null,
    n integer not null,
    prevTxHash blob(32) not null,
    outputIndex integer not null,
    signature blob(32) not null,
    primary key (txId, n),
    foreign key(txId) REFERENCES Tx(txId)
);

create table Address(
    addressId integer primary key not null,
    modulus blob(64) not null,
    exponent blob(64) not null
);

create unique index AddressKey on Address(modulus, exponent);

create table TxOutput(
    txId integer not null,
    n integer not null,
    addressId integer not null,
    value int8 not null,
    primary key (txId, n),
    foreign key(txId) REFERENCES Tx(txId),
    foreign key(addressId) REFERENCES Address(addressId)
);

create table Block (
    blockId integer primary key not null,
    hash blob(32) not null,
    prevBlockHash blob(32),
    nTxs integer not null
);

create unique index BlockHash on Block(hash);

create table BlockTx (
    blockId integer not null,
    n integer not null,
    txId integer not null,
    primary key (blockId, n),
    foreign key (blockId) REFERENCES Block(blockId)
);

create table OrphanedTx (
    txId integer primary key,
    foreign key(txId) REFERENCES Tx(txId)
);

create table OrphanedBlock (
    blockId integer primary key,
    foreign key(blockId) REFERENCES Block(blockId)
);

create table Refs (
    name varchar(10) primary key,
    blockId integer,
    foreign key(blockId) REFERENCES Block(blockId)
);

create table UnspentTxOut (
    txId integer not null,
    n integer not null,
    primary key (txId, n),
    foreign key(txId) REFERENCES Tx(txId)
);

create table Unspent (
    addressId integer primary key not null,
    value int8 not null,
    foreign key(addressId) REFERENCES AddressId(addressId)
);

create table PendingTx (
    txId integer primary key not null,
    foreign key(txId) REFERENCES Tx(txId)
);

create table BlockLevel (
    blockId integer primary key not null,
    level integer not null,
    foreign key(blockId) REFERENCES Block(blockId)
);