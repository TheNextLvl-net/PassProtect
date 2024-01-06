package net.thenextlvl.passprotect.api.code;

import javax.annotation.Nonnull;

public class InvalidQRCodeException extends Exception {

    public InvalidQRCodeException(String message) {
        super(message);
    }
}
