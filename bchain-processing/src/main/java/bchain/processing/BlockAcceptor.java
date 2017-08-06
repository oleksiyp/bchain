package bchain.processing;

import bchain.domain.Block;

public interface BlockAcceptor {
    boolean accept(Block block);
}
