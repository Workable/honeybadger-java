import org.apache.log4j.MDC;
import org.junit.Test;

/**
 * Created by kostas on 12/4/14.
 */
public class AppenderIT {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppenderIT.class);



    @Test
    public void shouldLogErrorFromBuggy() throws Exception {
        Buggy buggy = new Buggy();
        buggy.fail();
    }
    @Test
    public void shouldLogError2() throws Exception {

        for (int i = 0; i < 25; i++){
            log.error("This is an error" + i, new IllegalStateException("Oups" + i));
        }
    }

    @Test
    public void shouldLogErrorWithMDC() throws Exception {
        MDC.put("MDC Entry", "MDC Value");
        log.error("This is an error with MDC", new UnsupportedOperationException("Something went wrong..."));
    }

    private class Buggy {

        private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Buggy.class);

        public void fail(){
            log.error("Error from buggy", new IllegalStateException("From buggy"));
        }
    }

}
