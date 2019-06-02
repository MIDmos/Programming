package server;

import java.util.Random;

public class RandomPassword {

    public final static char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz023456789".toCharArray();

    public static synchronized String createPassword() {
        Random random = new Random(System.currentTimeMillis());
        char[] buf = new char[12];
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = ALPHABET[random.nextInt(ALPHABET.length)];
        return new String(buf);
    }
}
