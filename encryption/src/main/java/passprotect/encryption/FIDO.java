package passprotect.encryption;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECGenParameterSpec;

/**
 * FIDO class represents a FIDO (Fast Identity Online) object that supports generation of KeyPairs.
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class FIDO {
    public static final FIDO P_256 = new FIDO("secp256r1");
    public static final FIDO P_384 = new FIDO("secp384r1");
    public static final FIDO P_521 = new FIDO("secp521r1");

    private String algorithm = "EC";
    private final String curve;

    /**
     * Generates a KeyPair using the specified elliptic curve algorithm and key size.
     *
     * @return the generated KeyPair
     * @throws NoSuchAlgorithmException           if the requested algorithm is not available
     * @throws InvalidAlgorithmParameterException if the algorithm parameters are invalid
     */
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        var keySpec = new ECGenParameterSpec(curve);
        var instance = KeyPairGenerator.getInstance(algorithm);
        instance.initialize(keySpec);
        return instance.generateKeyPair();
    }
}