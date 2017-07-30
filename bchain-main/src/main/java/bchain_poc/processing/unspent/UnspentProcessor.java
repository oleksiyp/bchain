package bchain_poc.processing.unspent;

import bchain_poc.Factory;
import bchain_poc.dao.*;
import bchain_poc.domain.*;
import bchain_poc.processing.Processor;
import bchain_poc.processing.path.HeadSwitcher;
import bchain_poc.processing.path.PathFinder;
import bchain_poc.processing.path.PathItem;

import java.util.List;

public class UnspentProcessor extends Processor {
    RefsDAO refsDAO;

    PathFinder pathFinder;

    TxDAO txDAO;

    BlockDAO blockDAO;

    UnspentTxOutputsDAO utxoDAO;

    UnspentDAO unspentDAO;

    private final HeadSwitcher pathSwitcher;

    public UnspentProcessor(Factory factory, Processor next) {
        super(next);

        pathSwitcher = factory.create(HeadSwitcher.class);
        refsDAO = factory.create(RefsDAO.class);
        pathFinder = factory.create(PathFinder.class);
        txDAO = factory.create(TxDAO.class);
        blockDAO = factory.create(BlockDAO.class);
        utxoDAO = factory.create(UnspentTxOutputsDAO.class);
        unspentDAO = factory.create(UnspentDAO.class);
    }

    @Override
    public void addBlock(Block block) {
        Hash head;
        if (block.isGenesis()) {
            head = null;
        } else {
            head = refsDAO.getHead();
            if (head == null) {
                return;
            }
        }

        List<PathItem> path = pathFinder.findPath(head, block.getHash());

        pathSwitcher.setPath(path);
        pathSwitcher.setProcessTx(this::processTx);

        if (!pathSwitcher.perform()) {
            return;
        }

        super.addBlock(block);
    }


    private void processTx(Tx tx, boolean add) {
        for (TxInput input : tx.getInputs()) {
            Hash outTxHash = input.getOutputTxHash();
            int n = input.getOutputN();

            Tx outTx = txDAO.getTx(outTxHash);
            TxOutput output = outTx.getOutputs().get(n);

            addRemove(add, outTxHash, n, output);
        }

        for (int n = 0; n < tx.getOutputs().size(); n++) {
            TxOutput output = tx.getOutputs().get(n);

            addRemove(!add, tx.getHash(), n, output);
        }
    }

    private void addRemove(boolean add, Hash outTxHash, int n, TxOutput output) {
        unspentDAO.unspentPrepareAddRemove(output.getAddress(), add, output.getValue());
        boolean res = utxoDAO.unspentOutAddRemove(add, outTxHash, n);
        unspentDAO.unspentCommitAddRemove(output.getAddress(), add, output.getValue(), !res);
    }

}
