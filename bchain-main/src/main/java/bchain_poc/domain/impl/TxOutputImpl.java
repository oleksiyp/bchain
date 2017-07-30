package bchain_poc.domain.impl;

import bchain_poc.domain.PubKey;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class TxOutputImpl implements bchain_poc.domain.TxOutput {
    private PubKey address;

    private long value;
}
