package bchain_poc.domain;

import java.util.List;

public interface Block {
    boolean isGenesis();

    Hash getPrevBlockHash();

    Hash getHash();

    List<Hash> getTxs();

    boolean verify();

    boolean validate();
}
