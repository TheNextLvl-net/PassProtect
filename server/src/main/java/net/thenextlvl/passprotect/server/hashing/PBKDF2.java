package net.thenextlvl.passprotect.server.hashing;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public record PBKDF2(
        char[] password,
        byte[] salt,
        int iterations,
        int length
) {
    public PBKDF2(String password, String salt) {
        this(password, salt, 65536);
    }

    public PBKDF2(String password, String salt, int iterations) {
        this(password, salt.getBytes(StandardCharsets.UTF_8), iterations);
    }

    public PBKDF2(String password, byte[] salt, int iterations) {
        this(password, salt, iterations, 128);
    }

    public PBKDF2(String password, byte[] salt, int iterations, int length) {
        this(password.toCharArray(), salt, iterations, length);
    }

    public byte[] hashBytes() throws InvalidKeySpecException, NoSuchAlgorithmException {
        var algorithm = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        var spec = new PBEKeySpec(password, salt, iterations, length);
        return algorithm.generateSecret(spec).getEncoded();
    }
}
