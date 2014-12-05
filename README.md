# Honeybadger-java
*Java client to report exceptions to [Honeybadger.io](https://www.honeybadger.io)*

Exceptions can be captured and dispatched in Honeybadger with the following methods:

- registering a specific [Thread.UncaughtExceptionHandler](https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.UncaughtExceptionHandler.html)
- registering a custom [Filter](http://docs.oracle.com/javaee/7/api/javax/servlet/Filter.html) for web applications
- appending events in [log4j](https://logging.apache.org/log4j/1.2/) using the [honeybadger-log4j-appender](honeybadger-log4j-appender)


## Usage

### Standalone
A typical stand-alone implementation may look like:

```java
import com.workable.honeybadger.HoneybadgerUncaughtExceptionHandler;

public static void main(String argv[]) {
    HoneybadgerUncaughtExceptionHandler.registerAsUncaughtExceptionHandler();
    // The rest of the application goes here
}
```
The following system properties should be set to enable error dispatching:

System Properties:

 - honeybadger.api_key - set this to the (typically 8 character) API key displayed on your Honeybadger interface
 - honeybadger.excluded_sys_props - a comma delinated list of system property
   keys to exclude from being reported to Honeybadger. This allows you to prevent
   passwords and other sensitive information from being sent.
 - honeybadger.excluded_exception_classes - a comma delimited list of fully formed
   class names that will be excluded from error reporting.
 - JAVA_ENV / ENV - set this to configure the application's running environment

### In a servlet container
A servlet based implemantion may look like:

In your web.xml file:

```xml
    <!-- Send all uncaught servlet exceptions and servlet request details to Honeybadger -->
    <filter>
        <filter-name>HoneybadgerFilter</filter-name>
        <filter-class>com.workable.honeybadger.servlet.HoneybadgerFilter</filter-class>
        <init-param>
            <param-name>honeybadger.api_key</param-name>
            <param-value>API KEY GOES HERE</param-value>
        </init-param>
        <init-param>
            <param-name>honeybadger.excluded_sys_props</param-name>
            <param-value>bonecp.password,bonecp.username</param-value>
        </init-param>
        <init-param>
            <param-name>honeybadger.excluded_exception_classes</param-name>
            <param-value>org.apache.catalina.connector.ClientAbortException</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>HoneybadgerFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```

### Log4J
A typical log4j properties may look like:

```properties
log4j.rootLogger=ALL, ConsoleAppender, HoneybadgerAppender

log4j.appender.HoneybadgerAppender=com.workable.honeybadger.log4j.HoneybadgerAppender
log4j.appender.HoneybadgerAppender.layout=org.apache.log4j.EnhancedPatternLayout
log4j.appender.HoneybadgerAppender.layout.ConversionPattern=[%-5p] %c - %m%n%throwable{none}
log4j.appender.HoneybadgerAppender.threshold=ERROR

log4j.appender.HoneybadgerAppender.apiKey=XXXXXXXX
log4j.appender.HoneybadgerAppender.async=true
log4j.appender.HoneybadgerAppender.maxThreads=1
log4j.appender.HoneybadgerAppender.queueSize=50000
log4j.appender.HoneybadgerAppender.priority=1


log4j.appender.ConsoleAppender=org.apache.log4j.ConsoleAppender
log4j.appender.ConsoleAppender.layout=org.apache.log4j.EnhancedPatternLayout
log4j.appender.ConsoleAppender.layout.ConversionPattern=[WORKABLE] [%-5p] %c - %m%n%throwable{none}
```
