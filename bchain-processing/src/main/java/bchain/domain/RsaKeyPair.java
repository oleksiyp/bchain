package bchain.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class RsaKeyPair {
    private final PubKey pubKey;
    private final PrivKey privKey;

    public void serialize(DataOutput dataOutput) throws IOException {
        pubKey.serialize(dataOutput);
        privKey.serialize(dataOutput);
    }

    public static RsaKeyPair deserialize(DataInput dataInput) throws IOException {
        return new RsaKeyPair(
                PubKey.deserialize(dataInput),
                PrivKey.deserialize(dataInput));
    }
}
