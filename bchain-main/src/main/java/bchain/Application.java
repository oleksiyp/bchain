package bchain;

import bchain.app.BlockChainProcessor;
import bchain.app.UnspentProcessor;
import bchain.domain.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static bchain.domain.TxInput.input;
import static bchain.domain.TxOutput.output;

@EnableAutoConfiguration
@ComponentScan
@Slf4j
public class Application {

    @Autowired
    BlockChainProcessor blockChainProcessor;

    @Autowired
    UnspentProcessor unspentProcessor;

    @Autowired
    BlockQ blockQ;

    @AllArgsConstructor
    @Getter
    static class UTXO {
        Hash hash;
        int n;
        long value;
    }

    static int nActor = 0;

    static File keysFile = new File("keys.dat");
    static boolean read = keysFile.exists();
    static DataInput in;
    static {
        try {
            in = read ? new DataInputStream(new FileInputStream(keysFile)) : null;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static RsaKeyPair writeKeyPair(RsaKeyPair keyPair) {
        try (OutputStream out = new FileOutputStream(keysFile, true);
             DataOutputStream dataOutput = new DataOutputStream(out)) {
            keyPair.serialize(dataOutput);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return keyPair;
    }

    static RsaKeyPair readKeyPair() {
        try {
            return RsaKeyPair.deserialize(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    class Actor {
        private RsaKeyPair keyPair = read ?
                readKeyPair() :
                writeKeyPair(Crypto.rsaGen());

        public List<UTXO> coins = new ArrayList<>();

        public boolean hasCoins() {
            return amount() > 0;
        }

        public long amount() {
            return coins.stream()
                    .mapToLong(UTXO::getValue)
                    .sum();
        }

        public Tx transfer(long amount, Actor to) {
            TxBuilder builder = Tx.builder()
                    .add(output(to.keyPair.getPubKey(), amount));

            long sum = 0;
            for (Iterator<UTXO> iterator = coins.iterator(); iterator.hasNext(); ) {
                UTXO coin = iterator.next();
                iterator.remove();
                sum += coin.getValue();
                builder.add(input(coin.hash, coin.n), keyPair.getPrivKey());
                if (sum > amount) {
                    break;
                }
            }

            if (sum != amount) {
                builder.add(output(keyPair.getPubKey(), sum - amount));
            }

            Tx tx = builder.build();

            to.record(tx.getHash(), 0, amount);
            if (sum != amount) {
                record(tx.getHash(), 1, sum - amount);
            }

            return tx;
        }

        public void record(Hash hash, int n, long value) {
            coins.add(new UTXO(hash, n, value));
        }

        public void retrieveCoins() {
            for (UnspentTxOut unspent : unspentProcessor.unspents(getKeyPair().getPubKey())) {
                record(unspent.getHash(), unspent.getN(), unspent.getValue());
            }
        }
    }

    private void run() {
        Actor op = new Actor();
        List<Actor> miners = createN(5, Actor::new);
        List<Actor> services = createN(10, Actor::new);

        List<Actor> all = new ArrayList<>();
        all.add(op);
        all.addAll(miners);
        all.addAll(services);

        Tx base = Tx.builder()
                .setCoinbase(true)
                .add(output(op.getKeyPair().getPubKey(), 2500))
                .build();

        Block genesisBlock = Block.builder()
                .add(base)
                .build();

        blockChainProcessor.process(genesisBlock);


        for (Actor actor : all) {
            actor.retrieveCoins();
            log.info("Coins {} {}", actor.keyPair.getPubKey(), actor.amount());
        }

        Random rnd = new Random();

        while (true) {
            Block block;
            while ((block = blockQ.blocks.poll()) != null) {
                log.info("{} {}", block.getHash(), block.getPrevBlockHash());
                for (Tx tx : block.getTxs()) {
                    log.info("{}", tx);
                }
                blockChainProcessor.process(block);
            }

            List<Actor> hasCoins = all.stream()
                    .filter(Actor::hasCoins)
                    .collect(Collectors.toList());

            Actor from = hasCoins.get(rnd.nextInt(hasCoins.size()));
            int amount = from.amount() == 1 ? 1 : rnd.nextInt((int) from.amount() - 1) + 1;
            Actor to = all.get(rnd.nextInt(all.size()));
            Tx tx = from.transfer(amount, to);

            log.info("New tx {}", tx.getHash());
            blockChainProcessor.process(tx);
        }
    }

    private <T> List<T> createN(int n, Supplier<T> constructor) {
        List<T> list = new ArrayList<>();
        while (n-- > 0) {
            list.add(constructor.get());
        }
        return list;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        ctx.getBean(Application.class)
                .run();
    }
}