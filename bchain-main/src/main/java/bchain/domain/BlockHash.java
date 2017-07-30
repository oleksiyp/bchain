package bchain.domain;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

public class BlockHash {
    public static Hash computeHash(Hash prevBlockHash, List<Tx> txs) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             DataOutputStream dataOut = new DataOutputStream(byteOut)) {

            Hash.digest(prevBlockHash, dataOut);

            for (Tx tx : txs) {
                tx.digest(dataOut);
            }

            return Hash.hashOf(byteOut.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
