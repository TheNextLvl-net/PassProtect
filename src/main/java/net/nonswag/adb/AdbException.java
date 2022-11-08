package net.nonswag.adb;

import javax.annotation.Nonnull;

public class AdbException extends Exception {

    public AdbException() {
    }

    public AdbException(@Nonnull String message) {
        super(message);
    }

    public AdbException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }

    public AdbException(@Nonnull Throwable cause) {
        super(cause);
    }
}
