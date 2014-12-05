package com.workable.honeybadger.log4j;

import com.workable.honeybadger.*;
import com.workable.honeybadger.Error;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * Appender for log4j responsible to send events carrying exceptions to Honeybadger
 */
public class HoneybadgerAppender extends AppenderSkeleton {

    /**
     * The client to send errors to Honeybadger
     */
    private HoneybadgerClient client;

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
    private boolean async;

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



    @Override
    protected void append(LoggingEvent loggingEvent) {

        ThrowableInformation info = loggingEvent.getThrowableInformation();

        if (info != null) {
            Error error = new Error(getMessage(loggingEvent), loggingEvent.getThrowableInformation().getThrowable());
            client.reportError(error);
        }
    }

    @Override
    public void activateOptions() {
        super.activateOptions();
        if (client == null) {
            client = new HoneybadgerClient(apiKey, ignoredSystemProperties, ignoredExceptions);
            client.setAsync(async);
            client.setMaxThreads(maxThreads);
            client.setPriority(priority);
            client.setQueueSize(queueSize);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return true;
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

    private String getMessage(LoggingEvent event) {
        if (layout != null) {
            return layout.format(event);
        } else {
            return null;
        }
    }
}
