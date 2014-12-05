package com.workable.honeybadger.servlet;

import com.workable.honeybadger.*;
import com.workable.honeybadger.Error;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static com.workable.honeybadger.HoneybadgerClient.HONEYBADGER_API_KEY_SYS_PROP_KEY;
import static com.workable.honeybadger.HoneybadgerClient.HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY;
import static com.workable.honeybadger.HoneybadgerClient.HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY;
import static com.workable.honeybadger.HoneybadgerClient.HONEYBADGER_URL_SYS_PROP_KEY;

/**
 * Servlet filter that reports all unhandled servlet errors to Honeybadger.
 */
public class HoneybadgerFilter implements Filter {

    private HoneybadgerClient reporter;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_URL_SYS_PROP_KEY);
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_API_KEY_SYS_PROP_KEY);
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY);
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY);

        reporter = new HoneybadgerClient();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
        throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable e) {
            Error error = new Error(e);
            error.setContext(request);
            reporter.reportError(error);
            throw e;
        }
    }

    @Override
    public void destroy() {
        // do nothing
    }

    /**
     * Sets a system property based on the servlet config.
     */
    private void setSysPropFromfilterConfig(FilterConfig filterConfig,
                                            String param) {
        final String val = filterConfig.getInitParameter(param);

        // Don't overwrite already set properties
        if (System.getProperty(param) != null) {
            return;
        }

        if (val != null && !val.trim().equals("")) {
            System.setProperty(param, val);
        }
    }
}
