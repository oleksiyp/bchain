package bchain.domain;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;

public class Crypto {
    public static final KeyFactory RSA_KEY_FACTORY;
    public static final KeyPairGenerator RSA_KEY_PAIR_GENERATOR;
    static {
        try {
            RSA_KEY_FACTORY = KeyFactory.getInstance("RSA");
            RSA_KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance("RSA");
            RSA_KEY_PAIR_GENERATOR.initialize(512);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("init failed", e);
        }
    }

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

    public static byte []inputDigest(boolean coinbase, Hash prevTxHash, int outputIndex, List<TxOutput> outputs) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             DataOutputStream dataOut = new DataOutputStream(byteOut)) {

            dataOut.writeBoolean(coinbase);

            prevTxHash.serialize(dataOut);
            dataOut.writeInt(outputIndex);

            for (TxOutput output : outputs) {
                output.digest(dataOut);
            }

            return byteOut.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifySignature(PubKey address, byte[] bytes, byte[] signature) {
        Signature sign;
        try {
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(address.getModulus(),
                    address.getExponent());
            PublicKey publicKey = RSA_KEY_FACTORY.generatePublic(keySpec);
            sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(publicKey);
        } catch (Exception e) {
            throw new RuntimeException("init signature failed", e);
        }

        try {
            sign.update(bytes);
            return sign.verify(signature);
        } catch (SignatureException e) {
            throw new RuntimeException("verification failed", e);
        }
    }

    public static byte[] sign(PrivKey privKey, byte[] bytes) {
        Signature sign;
        try {
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(
                    privKey.getModulus(),
                    privKey.getExponent());

            PrivateKey privateKey = RSA_KEY_FACTORY.generatePrivate(keySpec);
            sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privateKey);
        } catch (Exception e) {
            throw new RuntimeException("init signature failed", e);
        }

        try {
            sign.update(bytes);
            return sign.sign();
        } catch (SignatureException e) {
            throw new RuntimeException("sign failed", e);
        }
    }

    public static RsaKeyPair rsaGen() {
        KeyPair keyPair = RSA_KEY_PAIR_GENERATOR.generateKeyPair();

        RSAPublicKeySpec pubKey;
        RSAPrivateKeySpec privKey;
        try {
            pubKey = RSA_KEY_FACTORY.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class);
            privKey = RSA_KEY_FACTORY.getKeySpec(keyPair.getPrivate(), RSAPrivateKeySpec.class);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("key spec error", e);
        }

        return new RsaKeyPair(
                new PubKey(pubKey.getModulus(), pubKey.getPublicExponent()),
                new PrivKey(privKey.getModulus(), privKey.getPrivateExponent()));
    }
}
