package bchain.util;

import java.util.Random;

public class RndUtil {
    private static final Random RANDOM = new Random();
    
    public static byte[] rndBytes(int n) {
        byte[] bytes = new byte[n];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
