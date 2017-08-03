package bchain.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@Getter
@AllArgsConstructor
@ToString
public class UnspentTxOut {
    private final Hash hash;

    private final int n;

    private final PubKey address;

    private final long value;
}
