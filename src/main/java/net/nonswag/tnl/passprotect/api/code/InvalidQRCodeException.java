package net.nonswag.tnl.passprotect.api.code;

import javax.annotation.Nonnull;

public class InvalidQRCodeException extends Exception {

    public InvalidQRCodeException(@Nonnull String message) {
        super(message);
    }
}
