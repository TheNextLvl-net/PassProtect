package net.nonswag.passprotect.api.authentication;

import com.warrenstrange.googleauth.ICredentialRepository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class CredentialService implements ICredentialRepository {

    @Nonnull
    private final HashMap<String, String> credentials = new HashMap<>();

    @Nullable
    @Override
    public String getSecretKey(@Nonnull String username) {
        return credentials.get(username);
    }

    @Override
    public void saveUserCredentials(@Nonnull String username, @Nonnull String secretKey, int validationCode, @Nonnull List<Integer> scratchCodes) {
        credentials.put(username, secretKey);
    }
}
