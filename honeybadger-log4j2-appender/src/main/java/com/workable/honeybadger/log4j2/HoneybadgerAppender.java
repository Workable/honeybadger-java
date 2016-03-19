package com.workable.honeybadger.log4j2;

import com.workable.honeybadger.*;
import com.workable.honeybadger.Error;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Appender for log4j responsible to send events carrying exceptions to Honeybadger
 */
@Plugin(name="HoneybadgerAppender", category="Core", elementType="appender", printObject=true)
public class HoneybadgerAppender extends AbstractAppender {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();

    /**
     * The client to send errors to Honeybadger
     */
    private static HoneybadgerClient client;

    protected HoneybadgerAppender(HoneybadgerClient client, String name, Filter filter,
            Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        this.client = client;
    }

    @Override
    public void append(LogEvent event) {
        readLock.lock();
        try {
            Error error = new Error(getLayout().toByteArray(event).toString(), event.getThrown());
            error.setReporter(event.getLoggerName());
            client.reportError(error);
        } catch (Exception ex) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(ex);
            }
        } finally {
            readLock.unlock();
        }
    }

    @PluginFactory
    public static HoneybadgerAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("apiKey") String apiKey,
            @PluginAttribute("ignoredSystemProperties") String ignoredSystemProperties,
            @PluginAttribute("ignoredExceptions") String ignoredExceptions,
            @PluginAttribute("async") boolean async,
            @PluginAttribute("maxThreads") int maxThreads,
            @PluginAttribute("priority") int priority,
            @PluginAttribute("queueSize") int queueSize) {
        if (name == null) {
            LOGGER.error("No name provided for HoneybadgerAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        if (client == null) {
            client = new HoneybadgerClient(apiKey, ignoredSystemProperties, ignoredExceptions);
            client.setAsync(async);
            client.setMaxThreads(maxThreads);
            client.setPriority(priority);
            client.setQueueSize(queueSize);
        }
        return new HoneybadgerAppender(client, name, filter, layout, true);
    }
}
