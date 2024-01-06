package net.thenextlvl.adb;

public class AdbException extends Exception {

    public AdbException() {
    }

    public AdbException(String message) {
        super(message);
    }

    public AdbException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdbException(Throwable cause) {
        super(cause);
    }
}
