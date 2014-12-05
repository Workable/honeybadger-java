package com.workable.honeybadger;

/**
 * Value Object representing an error that should be dispatched to Honeybadger
 */
public class Error {

    private String message;

    private String reporter;

    private Throwable error;

    private Object context;

    public Error(Throwable error) {
        this.error = error;
    }

    public Error(String message, Throwable error) {
        this.message = message;
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "Error{" +
               "message='" + message + '\'' +
               ", reporter='" + reporter + '\'' +
               ", error=" + error +
               ", context=" + context +
               '}';
    }
}
