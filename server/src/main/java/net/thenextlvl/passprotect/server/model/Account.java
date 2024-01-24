package net.thenextlvl.passprotect.server.model;

import lombok.*;
import lombok.experimental.Accessors;
import net.thenextlvl.passprotect.server.Server;
import net.thenextlvl.passprotect.server.hashing.PBKDF2;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Date;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Accessors(fluent = true)
public class Account {
    public static final int SALT_LENGTH = 16;
    public static final int TOKEN_LENGTH = 128;

    private final UUID uuid;
    private @Setter String email;

    private transient int iterations;
    private transient byte[] password;
    private transient byte[] salt;

    private String token;
    private Date tokenValidity;

    /**
     * Initializes a new Account object with the provided email and password.
     *
     * @param email    the email address of the account
     * @param password the password of the account
     * @throws InvalidKeySpecException  if the provided key specification is invalid
     * @throws NoSuchAlgorithmException if the algorithm used for key generation is not found
     */
    public Account(String email, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this(email, password, generateString(SALT_LENGTH));
    }

    private Account(String email, String password, String salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this(email, new PBKDF2(password, salt));
    }

    private Account(String email, PBKDF2 hash) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this(UUID.randomUUID(), email,
                hash.iterations(), hash.hashBytes(), hash.salt(),
                generateString(TOKEN_LENGTH), generateExpirationDate()
        );
    }

    /**
     * Regenerates the token for the account.
     */
    public void regenerateToken() {
        this.token = generateString(TOKEN_LENGTH);
        this.tokenValidity = generateExpirationDate();
    }

    /**
     * Updates the password for the account using the provided password.
     *
     * @param password the new password to set
     * @throws InvalidKeySpecException  if the provided key specification is invalid
     * @throws NoSuchAlgorithmException if the algorithm used for key generation is not found
     */
    public void password(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        password(password, generateString(SALT_LENGTH));
    }

    /**
     * Updates the password for the account using the provided password and salt.
     *
     * @param password the new password to set
     * @param salt     the salt to use for password hashing
     * @throws InvalidKeySpecException  if the provided key specification is invalid
     * @throws NoSuchAlgorithmException if the algorithm used for key generation is not found
     */
    public void password(String password, String salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        var hash = new PBKDF2(password, salt);
        this.iterations = hash.iterations();
        this.password = hash.hashBytes();
        this.salt = hash.salt();
        regenerateToken();
    }

    /**
     * Checks if the provided password matches the stored hashed password.
     *
     * @param password the password to check
     * @return true if the password matches the stored hashed password, false otherwise
     * @throws InvalidKeySpecException  if the provided key specification is invalid
     * @throws NoSuchAlgorithmException if the algorithm used for key generation is not found
     */
    public boolean passwordMatches(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return Arrays.equals(new PBKDF2(password, salt, iterations).hashBytes(), this.password);
    }

    /**
     * Checks if the token is valid.
     *
     * @return true if the token is still valid, false otherwise
     */
    public boolean isTokenValid() {
        return System.currentTimeMillis() < tokenValidity().getTime();
    }

    /**
     * Generates a random string of the specified length.
     *
     * @param length the length of the generated string
     * @return the generated string
     */
    private static String generateString(int length) {
        byte[] randomBytes = new byte[length];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Generates an expiration date for the token.
     *
     * @return the expiration date as a Date object
     */
    private static Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + Server.CONFIG.tokenValidity());
    }
}
