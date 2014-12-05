package com.workable.honeybadger;

/**
 * Exception class representing an error state with Honeybadger error reporting.
 */
@SuppressWarnings("serial")
public class HoneybadgerException extends RuntimeException {

    public HoneybadgerException() {
    }

    public HoneybadgerException(String message) {
        super(message);
    }

    public HoneybadgerException(String message, Throwable cause) {
        super(message, cause);
    }

    public HoneybadgerException(Throwable cause) {
        super(cause);
    }

    public HoneybadgerException(String message, Throwable cause,
                                boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
