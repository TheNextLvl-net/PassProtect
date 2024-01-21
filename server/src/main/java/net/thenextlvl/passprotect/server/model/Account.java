package net.thenextlvl.passprotect.server.model;

import core.util.StringUtil;
import lombok.*;
import lombok.experimental.Accessors;
import net.thenextlvl.passprotect.server.hashing.PBKDF2;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Accessors(fluent = true)
public class Account {
    private @Setter String email;
    private @Setter int iterations;
    private byte[] password;
    private byte[] salt;

    public Account(String email, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this(email, password, StringUtil.random(16));
    }

    public Account(String email, String password, String salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this(email, new PBKDF2(password, salt));
    }

    public Account(String email, PBKDF2 hash) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this(email, hash.iterations(), hash.hashBytes(), hash.salt());
    }

    public void password(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        password(password, StringUtil.random(16));
    }

    public void password(String password, String salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        var hash = new PBKDF2(password, salt);
        this.iterations = hash.iterations();
        this.password = hash.hashBytes();
        this.salt = hash.salt();
    }

    public boolean passwordMatches(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return Arrays.equals(new PBKDF2(password, salt, iterations).hashBytes(), this.password);
    }
}
