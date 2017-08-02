package bchain.app;

import bchain.domain.Block;

public interface BlockAcceptor {
    boolean accept(Block block);
}
