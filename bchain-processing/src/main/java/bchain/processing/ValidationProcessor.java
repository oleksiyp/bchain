package bchain.processing;

import bchain.domain.Result;
import bchain.dao.UnspentDao;
import bchain.domain.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static bchain.domain.Result.ok;
import static bchain.domain.Result.validationFailed;
import static bchain.domain.Crypto.inputDigest;
import static bchain.domain.Crypto.verifySignature;

public class ValidationProcessor {
    public static final int COINBASE = 2500;
    @Autowired
    UnspentDao unspentDao;

    public Result validate(Block block) {
        Set<UnspentTxOut> utxos = new HashSet<>();
        long coinbaseSum = 0;
        long delta = 0;
        for (Tx tx : block.getTxs()) {
            if (tx.isCoinbase()) {
                if (!tx.getInputs().isEmpty()) {
                    return validationFailed("coinbase tx has inputs");
                }
                for (TxOutput txOutput : tx.getOutputs()) {
                    long value = txOutput.getValue();
                    if (value < 0) {
                        return validationFailed("output value < 0");
                    }
                    coinbaseSum += value;
                }
            } else {
                long outputSum = 0;
                for (TxOutput txOutput : tx.getOutputs()) {
                    long value = txOutput.getValue();
                    if (value < 0) {
                        return validationFailed("output value < 0");
                    }
                    outputSum += value;
                }

                long inputSum = 0;
                for (int i = 0; i < tx.getInputs().size(); i++) {
                    TxInput input = tx.getInputs().get(i);

                    UnspentTxOut txOut = unspentDao.get(
                            input.getPrevTxHash(),
                            input.getOutputIndex());

                    if (txOut == null) {
                        txOut = findTxOut(block.getTxs(), input);
                    }

                    if (txOut == null) {
                        return validationFailed("input not referencing unspent out");
                    }

                    if (!utxos.add(txOut)) {
                        return validationFailed("block double spend");
                    }

                    byte[] inputDigest = inputDigest(tx.isCoinbase(),
                            input.getPrevTxHash(),
                            input.getOutputIndex(),
                            tx.getOutputs());

                    if (!verifySignature(
                            txOut.getAddress(),
                            inputDigest,
                            input.getSignature())) {
                        return validationFailed("wrong signature");
                    }

                    inputSum += txOut.getValue();
                }

                if (inputSum < outputSum) {
                    return validationFailed("inputSum < outputSum");
                }
                delta = inputSum - outputSum;
            }
        }

        if (coinbaseSum > COINBASE + delta) {
            return validationFailed("coinbase sum is larger then threshold");
        }

        return ok();
    }

    private UnspentTxOut findTxOut(List<Tx> txs, TxInput input) {
        Optional<Tx> txOpt = txs.stream()
                .filter(tx -> tx.getHash().equals(input.getPrevTxHash()))
                .findFirst();

        if (!txOpt.isPresent()) {
            return null;
        }

        Tx tx = txOpt.get();
        int n = input.getOutputIndex();
        List<TxOutput> outputs = tx.getOutputs();
        if (n >= outputs.size()) {
            return null;
        }

        TxOutput txOut = outputs.get(n);
        return new UnspentTxOut(tx.getHash(),
                n,
                txOut.getAddress(),
                txOut.getValue());
    }
}
