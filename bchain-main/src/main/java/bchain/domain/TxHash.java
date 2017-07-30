package bchain.domain;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

public class TxHash {
    public static Hash computeHash(boolean coinbase,
                                   List<TxInput> inputs,
                                   List<TxOutput> outputs) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             DataOutputStream dataOut = new DataOutputStream(byteOut)) {

            dataOut.writeBoolean(coinbase);

            for (TxInput input : inputs) {
                input.digest(dataOut);
            }

            for (TxOutput output : outputs) {
                output.digest(dataOut);
            }

            return Hash.hashOf(byteOut.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
