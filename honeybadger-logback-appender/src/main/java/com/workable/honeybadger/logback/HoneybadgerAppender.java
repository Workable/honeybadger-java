package com.workable.honeybadger.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import com.workable.honeybadger.*;
import com.workable.honeybadger.Error;


/**
 * Appender for logback responsible to send events carrying exceptions to Honeybadger
 */
public class HoneybadgerAppender extends AppenderBase<ILoggingEvent> {

    /**
     * The client to send errors to Honeybadger
     */
    protected volatile HoneybadgerClient client;

    /**
     * The Honebadger API Key. Cannot be null
     */
    private String apiKey;

    /**
     * Comma delimited list of System Properties that should not be included in Error
     */
    private String ignoredSystemProperties;

    /**
     * Comma delimited list of Exceptions that should be ignored
     */
    private String ignoredExceptions;

    /**
     * If <code>true</code> erros are dispatched asynchronously (Default true)
     */
    private boolean async = true;

    /**
     * Max threads for asynchronous error dispatching. (Default number of processors)
     */
    private int maxThreads;

    /**
     * The thread priority of the asynchronous thread dispatchers. (Default Thread.MIN)
     */
    private int priority;

    /**
     * The queue size of the asynchronous dispatching mechanism (Default: Integer.MAX)
     */
    private int queueSize;

    /**
     * Creates an instance of HoneybadgerAppender.
     */
    public HoneybadgerAppender() {
    }

    /**
     * Creates an instance of HoneybadgerAppender.
     *
     * @param instance of Honeybadger to use with this appender.
     */
    public HoneybadgerAppender(HoneybadgerClient client, String apiKey, String ignoredExceptions, String ignoredSystemProperties,
                               boolean async, int maxThreads, int priority, int queueSize) {
        this.client = client;
        this.apiKey = apiKey;
        this.ignoredExceptions = ignoredExceptions;
        this.ignoredSystemProperties = ignoredSystemProperties;
        this.async = async;
        this.maxThreads = maxThreads;
        this.priority = priority;
        this.queueSize = queueSize;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The honeybadger instance is started in this method instead of {@link #start()} in order to avoid substitute loggers
     * being generated during the instantiation of {@link Honeybadger}.<br>
     * More on <a href="http://www.slf4j.org/codes.html#substituteLogger">www.slf4j.org/codes.html#substituteLogger</a>
     */
    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        initHoneybadger();
        try {
            IThrowableProxy info = iLoggingEvent.getThrowableProxy();
            if (info != null) {
                Throwable throwable = new Throwable(iLoggingEvent.getMessage());
                throwable.setStackTrace(toStackTraceElements(iLoggingEvent.getThrowableProxy()));
                Error error = new Error(iLoggingEvent.getFormattedMessage(),throwable);
                error.setReporter(iLoggingEvent.getLoggerName());
                client.reportError(error);
            }
        } catch (Exception e) {
            addError("An exception occurred while creating a new event in Honeybadger", e);
        }
    }

    /**
     * Initialises the Honeybadger instance.
     */
    protected synchronized void initHoneybadger() {
        try {
            if (client == null) {
                client = new HoneybadgerClient(apiKey, ignoredSystemProperties, ignoredExceptions);
                client.setAsync(async);
                client.setMaxThreads(maxThreads);
                client.setPriority(priority);
                client.setQueueSize(queueSize);
            }
        } catch (Exception e) {
            addError("An exception occurred during the creation of a Raven instance", e);
        }
    }

    private StackTraceElement[] toStackTraceElements(IThrowableProxy throwableProxy) {
        StackTraceElementProxy[] stackTraceElementProxies = throwableProxy.getStackTraceElementProxyArray();
        StackTraceElement[] stackTraceElements = new StackTraceElement[stackTraceElementProxies.length];

        for (int i = 0, stackTraceElementsLength = stackTraceElementProxies.length; i < stackTraceElementsLength; i++) {
            stackTraceElements[i] = stackTraceElementProxies[i].getStackTraceElement();
        }

        return stackTraceElements;
    }

    @Override
    public void stop() {
        if(client != null){
            client = null;
        }
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setIgnoredSystemProperties(String ignoredSystemProperties) {
        this.ignoredSystemProperties = ignoredSystemProperties;
    }

    public void setIgnoredExceptions(String ignoredExceptions) {
        this.ignoredExceptions = ignoredExceptions;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }
}
