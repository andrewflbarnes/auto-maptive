package net.aflb.maptive.auto.simple.app;

/**
 * A simple exception for which we should just emit the message to the user and exit
 * <strong>without</strong> a stack trace.
 */
public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }
}
