package com.workable.honeybadger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Facade giving a simple interface for sending error to the Honeybadger API
 */
public class HoneybadgerClient {

    /**
     * System property key identifying the Honeybadger URL to use.
     */
    public static final String HONEYBADGER_URL_SYS_PROP_KEY =
        "honeybadger.url";

    /**
     * System property key identifying the Honeybadger API key to use.
     */
    public static final String HONEYBADGER_API_KEY_SYS_PROP_KEY =
        "honeybadger.api_key";

    /**
     * Comma delimited list of system properties to not include.
     */
    public static final String HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY =
        "honeybadger.excluded_sys_props";

    /**
     * Comma delimited list of packages or classes that throw exceptions to ignore.
     */
    public static final String HONEYBADGER_EXCLUDED_CAUSES_SYS_PROP_KEY =
        "honeybadger.excluded_exception_causes";

    /**
     * Comma delimited list of exception classes to ignore.
     */
    public static final String HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY =
            "honeybadger.excluded_exception_classes";

    /**
     * The default Honebadger URL
     */
    public static final String DEFAULT_API_URI =
        "https://api.honeybadger.io/v1/notices";

    /**
     * If <code>true</code> errors are dispatched asynchronously (Default true)
     */
    private boolean async = true;

    /**
     * Max threads for asynchronous error dispatching. (Default: one)
     */
    private int maxThreads = 1;

    /**
     * The thread priority of the asynchronous thread dispatchers. (Default Thread.MIN)
     */
    private int priority = Thread.MIN_PRIORITY;

    /**
     * The queue size of the asynchronous dispatching mechanism (Default: Integer.MAX)
     */
    private int queueSize = Integer.MAX_VALUE;


    /**
     * Timeout of the {@link #executorService}.
     */
    private static final long SHUTDOWN_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String apiKey;

    private final Set<String> excludedExceptionClasses;
    private final Set<String> excludedExceptionCauses;

    private final JsonMarshaller marshaller;

    /**
     * Executor service in charge of sending errors asynchronously
     */
    private volatile ExecutorService executorService;

    /**
     * Default Constructor. <p>Options can be set via System properties</p>
     */
    public HoneybadgerClient() {
        this(System.getProperty(HONEYBADGER_API_KEY_SYS_PROP_KEY));
    }

    /**
     * Constructs a Client with the specified apiKey
     *
     * @param apiKey The Honeybadger API Key
     */
    public HoneybadgerClient(String apiKey) {
        this(apiKey, System.getProperty(HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY),
             System.getProperty(HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY),
                System.getProperty(HONEYBADGER_EXCLUDED_CAUSES_SYS_PROP_KEY));
    }

    /**
     * Constructs a Client with specific options
     *
     * @param apiKey                   The Honeybadger API Key
     * @param excludedSysProps         Comma delimited list of System properties that should be excluded from errors
     *                                 dispatched
     * @param excludedExceptionClasses Comma delimited list of Exceptions that should be ignored
     */
    public HoneybadgerClient(String apiKey, String excludedSysProps, String excludedExceptionClasses) {
        this(apiKey, excludedSysProps, excludedExceptionClasses, null);
    }

    /**
     * Constructs a Client with specific options
     *
     * @param apiKey                   The Honeybadger API Key
     * @param excludedSysProps         Comma delimited list of System properties that should be excluded from errors
     *                                 dispatched
     * @param excludedExceptionClasses Comma delimited list of Exceptions that should be ignored
     * @param excludedExceptionCauses  Comma delimited list of packages or classes that throw exceptions to ignore
     */
    public HoneybadgerClient(String apiKey, String excludedSysProps, String excludedExceptionClasses, String excludedExceptionCauses) {
        this.apiKey = apiKey;
        this.excludedExceptionClasses = buildExcludedClasses(excludedExceptionClasses);
        this.excludedExceptionCauses = buildExcludedCauses(excludedExceptionCauses);

        this.marshaller = new JsonMarshaller(buildExcludedSysProps(excludedSysProps));
    }

    /**
     * Reports the specific error to the Honebadger
     */
    public void reportError(Error error) {
        if (async) {
            if (executorService == null) {
                synchronized (this) {
                    if (executorService == null) {
                        initExecutorService();
                    }
                }
            }

            executorService.submit(new EventDispatcher(error));
        } else {
            doDispatchError(error);
        }
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setQueueSize(int queueSize) {
        if (queueSize > 0) {
            this.queueSize = queueSize;
        }
    }

    public void setMaxThreads(int maxThreads) {
        if (maxThreads > 0) {
            this.maxThreads = maxThreads;
        }
    }

    public void setPriority(int priority) {
        if (priority > 0) {
            this.priority = priority;
        }
    }


    /**
     * Marshals and dispatched the specified error to the Honeybadger
     */
    protected void doDispatchError(Error error) {
        if (shouldExclude(error.getError())) {
            return;
        }

        for (int retries = 0; retries < 3; retries++) {
            try {
                String json = marshaller.marshall(error);
                HttpResponse response = sendToHoneybadger(json);

                int responseCode = response.getStatusLine().getStatusCode();

                if (responseCode != 201) {
                    logger.error("Honeybadger did not respond with the " +
                                 "correct code. Response was [{}]. Retries={}",
                                 responseCode, retries);
                } else {
                    logger.trace("Honeybadger logged error correctly: {}",
                            json);
                    return;
                }
            } catch (IOException e) {
                String msg = String.format("There was an error when trying " +
                                           "to send the error to " +
                                           "Honeybadger. Retries=%d", retries);
                logger.error(msg, new HoneybadgerException(e));

            }
        }
    }

    /**
     * Checks if the Exception should be excluded or not
     * @param error
     * @return
     */
    protected boolean shouldExclude(Throwable error){

        if (error == null){
            return false;
        }

        String errorClassName = error.getClass().getName();
        if (!isNullOrEmpty(errorClassName)){
            for (String className : excludedExceptionClasses){
                if (errorClassName.startsWith(className)){
                    return true;
                }
            }
        }

        if (!excludedExceptionCauses.isEmpty()) {
            StackTraceElement[] stackTraceElements = error.getStackTrace();
            if (stackTraceElements != null && stackTraceElements.length > 0) {
                for (String className : excludedExceptionCauses) {
                    if (stackTraceElements[0].getClassName().startsWith(className)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    /**
     * Send an error encoded in JSON to the Honeybadger API.
     *
     * @param jsonError Error JSON payload
     * @return Status code from the Honeybadger API
     * @throws IOException thrown when a network was encountered
     */
    private HttpResponse sendToHoneybadger(String jsonError) throws IOException {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(honeybadgerUrl());
        StringEntity entity = new StringEntity(jsonError);
        entity.setContentType(new BasicHeader("Content-Type", "application/json"));
        post.setEntity(entity);
        post.setHeader("X-API-Key", apiKey);
        return client.execute(post, new BasicHttpContext());
    }


    /**
     * Finds the Honeybadger endpoint to send erros to.
     *
     * @return the default URL unless it is overriden by a system property
     */
    private URI honeybadgerUrl() {
        try {
            final String url;
            final String sysProp =
                System.getProperty(HONEYBADGER_URL_SYS_PROP_KEY);

            if (sysProp != null) {
                url = sysProp;
            } else {
                url = DEFAULT_API_URI;
            }

            return URI.create(url);
        } catch (IllegalArgumentException e) {
            String format = "Honeybadger URL was not correctly formed. " +
                            "Double check the [%s] system property and " +
                            "verify that it is a valid URL.";
            String msg = String.format(format, HONEYBADGER_URL_SYS_PROP_KEY);

            throw new HoneybadgerException(msg, e);
        }
    }

    /**
     * Initializes the Executor service used for async error dispatching
     */
    private void initExecutorService() {

        BlockingDeque<Runnable> queue = new LinkedBlockingDeque<>(queueSize);

        final ExecutorService executorService = new ThreadPoolExecutor(
            maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, queue,
            new DaemonThreadFactory(priority), new ThreadPoolExecutor.DiscardOldestPolicy());

        this.executorService = executorService;

        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                executorService.shutdown();

                try {
                    if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)) {
                        logger.warn("Graceful shutdown took too much time, forcing the shutdown.");
                        List<Runnable> tasks = executorService.shutdownNow();
                        logger.info("{} tasks failed to execute before the shutdown.", tasks.size());
                    }
                } catch (InterruptedException e) {
                    logger.error("Graceful shutdown interrupted, forcing the shutdown.");
                    List<Runnable> tasks = executorService.shutdownNow();
                    logger.info("{} tasks failed to execute before the shutdown.", tasks.size());
                }

            }

        });
    }

    private Set<String> buildExcludedSysProps(String excluded) {
        HashSet<String> set = new HashSet<>();

        set.add(HONEYBADGER_API_KEY_SYS_PROP_KEY);
        set.add(HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY);
        set.add(HONEYBADGER_URL_SYS_PROP_KEY);

        return buildExcluded(set, excluded);
    }

    private Set<String> buildExcludedClasses(String excluded) {
        HashSet<String> set = new HashSet<>();
        set.add(HoneybadgerException.class.getCanonicalName());
        return buildExcluded(set, excluded);
    }

    private Set<String> buildExcludedCauses(String excluded) {
        HashSet<String> set = new HashSet<>();
        return buildExcluded(set, excluded);
    }

    private Set<String> buildExcluded(Set<String>set, String excluded) {

        if (isNullOrEmpty(excluded)) {
            return set;
        }

        for (String item : excluded.split(",")) {
            set.add(item.trim());
        }

        return set;
    }

    /**
     * Runnable to dispatch asynchronously errors
     */
    private final class EventDispatcher implements Runnable {

        private final Error error;
        private final Map<String, String> context;

        public EventDispatcher(Error error) {
            this.error = error;
            this.context = MDC.getCopyOfContextMap();;
        }

        @Override
        public void run() {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            setMdcContext(context);
            try {
                doDispatchError(error);
            } catch (Exception e) {
                logger.error("An exception occurred while dispatching the error", new HoneybadgerException(e));
            } finally {
                setMdcContext(previous);
            }
        }

        private void setMdcContext(Map<String, String> context) {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
        }
    }

    private boolean isNullOrEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

}
