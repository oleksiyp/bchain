package bchain.util;

import bchain.domain.PubKey;

import java.util.Random;

public class RndUtil {
    private static final Random RANDOM = new Random();

    public static byte[] rndBytes(int n) {
        byte[] bytes = new byte[n];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    public static PubKey rndPubKey() {
        return PubKey.pubKey(rndBytes(64), rndBytes(64));
    }
}
