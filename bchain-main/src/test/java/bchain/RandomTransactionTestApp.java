package bchain;

import bchain.processing.BlockAcceptor;
import bchain.processing.BlockChainProcessor;
import bchain.processing.ProcessorConfig;
import bchain.processing.UnspentProcessor;
import bchain.dao.sqlite.SqliteConfig;
import bchain.domain.*;
import bchain.util.ExtendedJdbcTemplateConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static bchain.domain.TxInput.input;
import static bchain.domain.TxOutput.output;

@Import({SqliteConfig.class,
        ProcessorConfig.class,
        RandomTransactionTestApp.BlockQConfig.class,
        ExtendedJdbcTemplateConfig.class,
        FlywayAutoConfiguration.class})
@Slf4j
public class RandomTransactionTestApp {

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

    private void run() {
        Account op = new Account();
        List<Account> miners = createN(5, Account::new);
        List<Account> services = createN(10, Account::new);

        List<Account> all = new ArrayList<>();
        all.add(op);
        all.addAll(miners);
        all.addAll(services);

        Tx niceDeposit = Tx.builder()
                .setCoinbase(true)
                .add(output(op.getKeyPair().getPubKey(), 2500))
                .build();

        Block genesisBlock = Block.builder()
                .add(niceDeposit)
                .build();

        blockChainProcessor.process(genesisBlock);

        for (Account account : all) {
            account.retrieveCoins();
            log.info("Coins {} {}", account.keyPair.getPubKey(), account.amount());
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

            List<Account> hasCoins = all.stream()
                    .filter(Account::hasCoins)
                    .collect(Collectors.toList());

            Account from = hasCoins.get(rnd.nextInt(hasCoins.size()));
            int amount = from.amount() == 1 ? 1 : rnd.nextInt((int) from.amount() - 1) + 1;
            Account to = all.get(rnd.nextInt(all.size()));
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
        ConfigurableApplicationContext ctx = SpringApplication.run(RandomTransactionTestApp.class, args);

        ctx.getBean(RandomTransactionTestApp.class)
                .run();
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
    class Account {
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

        public Tx transfer(long amount, Account to) {
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

    public static class BlockQ implements BlockAcceptor {
        Queue<Block> blocks;

        public BlockQ() {
            blocks = new ConcurrentLinkedQueue<>();
        }

        @Override
        public boolean accept(Block block) {
            if (!block.getHash().toString().startsWith("0")) {
                return false;
            }

            blocks.add(block);
            return true;
        }
    }

    @Configuration
    public static class BlockQConfig {
        @Bean
        public BlockQ blockQ() {
            return new BlockQ();
        }
    }
}