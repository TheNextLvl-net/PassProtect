package net.nonswag.tnl.passprotect.api.authentication;

import com.warrenstrange.googleauth.GoogleAuthenticator;

import javax.annotation.Nonnull;

public class Authenticator {

    @Nonnull
    public static final GoogleAuthenticator INSTANCE = new GoogleAuthenticator();

    static {
        INSTANCE.setCredentialRepository(new CredentialService());
    }
}
