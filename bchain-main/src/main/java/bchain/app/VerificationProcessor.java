package bchain.app;

import bchain.app.result.Result;
import bchain.domain.*;

import static bchain.app.result.Result.ok;
import static bchain.app.result.Result.verificationFailed;
import static bchain.domain.BlockHash.computeBlockHash;
import static bchain.domain.Crypto.computeHash;

public class VerificationProcessor {
    public Result verify(Tx tx) {
        return verify(tx, false);
    }

    private Result verify(Tx tx, boolean allowCoinbase) {
        Hash computedHash = computeHash(tx.isCoinbase(), tx.getInputs(), tx.getOutputs());

        if (!computedHash.equals(tx.getHash())) {
            return verificationFailed("tx hash");
        }

        if (tx.isCoinbase()) {
            if (allowCoinbase) {
                if (!tx.getInputs().isEmpty()) {
                    return verificationFailed("coinbase tx has inputs");
                }
            } else {
                return verificationFailed("coinbase is allowed only in blocks");
            }
        }

        for (TxOutput txOutput : tx.getOutputs()) {
            long value = txOutput.getValue();
            if (value < 0) {
                return verificationFailed("output value < 0");
            }
        }

        return ok();
    }

    public Result verify(Block block) {
        Hash computedHash = computeBlockHash(block.getPrevBlockHash(), block.getTxs());

        if (!computedHash.equals(block.getHash())) {
            return verificationFailed("block hash");
        }

        for (Tx tx : block.getTxs()) {
            Result res = verify(tx, true);
            if (!res.isOk()) {
                return res;
            }
        }

        return ok();
    }
}
