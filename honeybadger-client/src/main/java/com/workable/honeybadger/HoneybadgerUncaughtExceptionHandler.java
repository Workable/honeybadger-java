package com.workable.honeybadger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception handler class that sends errors to Honey Badger by default.
 */
public class HoneybadgerUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    protected HoneybadgerClient reporter = new HoneybadgerClient();
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            reporter.reportError(new Error(e));
        } catch (RuntimeException re) {
            if (logger.isErrorEnabled()) {
                logger.error("An error occurred when sending data to the " +
                             "Honeybadger API", re);
            }
        } finally {
            if (logger.isErrorEnabled()) {
                logger.error("An unhandled exception has occurred", e);
            }
        }
    }

    /**
     * Use {@link HoneybadgerUncaughtExceptionHandler} as the error handler for the current thread.
     */
    public static void registerAsUncaughtExceptionHandler() {
        Thread.UncaughtExceptionHandler handler =
            new HoneybadgerUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    /**
     * Use {@link HoneybadgerUncaughtExceptionHandler} as the error handler for the specified thread.
     *
     * @param t thread to register handler for
     */
    public static void registerAsUncaughtExceptionHandler(
        Thread t) {
        Thread.UncaughtExceptionHandler handler =
            new HoneybadgerUncaughtExceptionHandler();
        t.setUncaughtExceptionHandler(handler);
    }
}
