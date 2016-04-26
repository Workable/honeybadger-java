import com.workable.honeybadger.HoneybadgerException;
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
    public void testNestedException() throws Exception {

        try {
            Delegate delegate = new Delegate();
            delegate.run();
        }
        catch(Exception e){
            log.error("Exception while runing", e);
        }


    }

    @Test
    public void shouldLogError2() throws Exception {

        for (int i = 0; i < 25; i++){
            log.error("This is an error" + i, new IllegalStateException("Oups" + i));
        }
    }

    @Test
    public void shouldExcludeHoneybadgerException() throws Exception {
        log.error("This is an error", new HoneybadgerException("Oups"));
    }

    @Test
    public void shouldLogErrorWithMDC() throws Exception {
        MDC.put("MDC Entry", "MDC Value");

        log.error("This is an error with MDC", new UnsupportedOperationException("Something went wrong...", new NullPointerException()));
    }

    private class Buggy {

        private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Buggy.class);

        public void fail(){
            log.error("Error from buggy", new IllegalStateException("From buggy"));
        }
    }

    private class Delegate{

        public void run(){
            try {
                Failer failer = new Failer();
                failer.fail();
            }
            catch (Exception e){
                throw new IllegalStateException("Error while running", e);
            }
        }
    }
    private class Failer {

        public void fail(){
            throw new NullPointerException("Point to null");
        }
    }

}
