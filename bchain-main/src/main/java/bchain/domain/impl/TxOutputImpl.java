package bchain.domain.impl;

import bchain.domain.PubKey;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class TxOutputImpl implements bchain.domain.TxOutput {
    private PubKey address;

    private long value;
}
