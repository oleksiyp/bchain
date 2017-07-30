package bchain_poc;

import bchain_poc.dao.mem.InMemDaoFactory;
import bchain_poc.domain.Hash;
import bchain_poc.domain.Tx;
import bchain_poc.domain.impl.BlockImpl;
import bchain_poc.domain.impl.TxImpl;
import bchain_poc.domain.impl.TxInputImpl;
import bchain_poc.domain.impl.TxOutputImpl;
import bchain_poc.processing.Processor;
import bchain_poc.processing.ProcessorBuilder;
import bchain_poc.processing.election.ElectionProcessor;
import bchain_poc.processing.orphan.OrphanBlockProcessor;
import bchain_poc.processing.orphan.OrphanTxProcessor;
import bchain_poc.processing.unspent.UnspentProcessor;
import bchain_poc.processing.receive.ReceiveBlockProcessor;
import bchain_poc.processing.receive.ReceiveTxProcessor;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Class.forName;

public class Main {
    public static void main(String[] args) {
        Processor processor = new ProcessorBuilder<Factory, Processor>()
                .add(ReceiveBlockProcessor::new)
                .add(ReceiveTxProcessor::new)
                .add(OrphanBlockProcessor::new)
                .add(OrphanTxProcessor::new)
                .add(UnspentProcessor::new)
                .add(ElectionProcessor::new)
                .build(new InMemDaoFactory());

        Tx tx = tx(processor, "h1");
        tx.getOutputs().add(new TxOutputImpl());

        block(processor, "gen_hash", null, tx);

        tx = tx(processor, "h2");
        tx.getInputs().add(new TxInputImpl());
        tx.getOutputs().add(new TxOutputImpl());

        block(processor, "block2", "gen_hash", tx);

    }

    private static void block(Processor processor, String hash, String prevHash, Tx... tx) {
        BlockImpl block = new BlockImpl();
        block.setHash(Hash.of(hash));
        block.setGenesis(prevHash == null);
        if (prevHash != null) {
            block.setPrevBlockHash(Hash.of(prevHash));
        }
        block.setTxs(Stream.of(tx)
                .map(Tx::getHash)
                .collect(Collectors.toList()));

        processor.addBlock(block);
    }

    private static Tx tx(Processor processor, String hash) {
        TxImpl tx = new TxImpl();

        tx.setHash(Hash.of(hash));
        tx.setInputs(new ArrayList<>());
        tx.setOutputs(new ArrayList<>());

        processor.addTransaction(tx);
        return tx;
    }

}
