package com.workable.honeybadger.logback;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class AppenderIT {

    private static final Logger log = LoggerFactory.getLogger(AppenderIT.class);

    @Test
    public void shouldLogErrorFromBuggy() throws Exception {
        Buggy buggy = new Buggy();
        buggy.fail();
    }


    @Test
    public void testNestedException() throws Exception {

        try {
            Delegate delegate = new Delegate();
            delegate.run();
        } catch (Exception e) {
            log.error("Exception while runing", e);
        }
    }

    @Test
    public void shouldLogError2() throws Exception {

        for (int i = 0; i < 25; i++) {
            log.error("This is an error" + i, new IllegalStateException("Oups" + i));
        }
    }

    @Test
    public void shouldLogErrorWithMDC() throws Exception {
        MDC.put("MDC Entry", "MDC Value");

        log.error("This is an error with MDC", new UnsupportedOperationException("Something went wrong...", new NullPointerException()));
    }

    private class Buggy {
        private final Logger log = LoggerFactory.getLogger(Buggy.class);

        public void fail() {
            log.error("Error from buggy", new IllegalStateException("From buggy"));
        }
    }

    private class Delegate {
        public void run() {
            try {
                Failer failer = new Failer();
                failer.fail();
            } catch (Exception e) {
                throw new IllegalStateException("Error while running", e);
            }
        }
    }

    private class Failer {

        public void fail() {
            throw new NullPointerException("Point to null");
        }
    }
}
