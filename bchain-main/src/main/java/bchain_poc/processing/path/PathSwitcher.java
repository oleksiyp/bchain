package bchain_poc.processing.path;

import bchain_poc.Factory;
import bchain_poc.dao.*;
import bchain_poc.domain.Block;
import bchain_poc.domain.Tx;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.BiConsumer;

public abstract class PathSwitcher {
    @Getter
    @Setter
    protected List<PathItem> path;

    @Getter
    @Setter
    private BiConsumer<Tx, Boolean> processTx;

    TxDAO txDAO;

    BlockDAO blockDAO;

    public PathSwitcher(Factory factory) {
        txDAO = factory.create(TxDAO.class);
        blockDAO = factory.create(BlockDAO.class);
    }

    protected abstract void commitOp(Block itemBlock);

    protected abstract void prepareOp(Block itemBlock, boolean isPush);

    protected abstract boolean validate(int i, Block itemBlock);

    public boolean perform() {
        for (int i = 0; i < this.path.size(); i++) {
            PathItem item = path.get(i);
            Block itemBlock = blockDAO.getBlock(item.getHash());

            boolean isPush = item.getType() == PathItem.Type.PUSH;
            if (isPush) {
                if (!validate(i, itemBlock)) {
                    return false;
                }
            }

            prepareOp(itemBlock, isPush);

            List<Tx> txList = txDAO.getAllTx(itemBlock.getTxs());

            for (Tx tx : txList) {
                this.processTx.accept(tx, isPush);
            }

            commitOp(itemBlock);
        }
        return true;
    }


}
