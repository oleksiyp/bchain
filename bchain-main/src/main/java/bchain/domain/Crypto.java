package bchain.domain;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

public class Crypto {
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

    public static byte []inputDigest(boolean coinbase, TxInput input, List<TxOutput> outputs) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             DataOutputStream dataOut = new DataOutputStream(byteOut)) {

            dataOut.writeBoolean(coinbase);

            input.digest(dataOut);

            for (TxOutput output : outputs) {
                output.digest(dataOut);
            }

            return byteOut.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifySignature(PubKey address, byte[] bytes, byte[] signature) {
        return true;
    }
}
