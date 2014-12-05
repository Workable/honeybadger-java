# honeybadger-log4j-appender
[log4j](https://logging.apache.org/log4j/1.2/) support for honeybadger.
It provides an [`Appender`](https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/Appender.html)
for log4j to send the logged events to honeybadger.

## Usage
### Configuration
In the `log4j.properties` file configure an appender named `HoneybadgerAppender`:

```properties
log4j.appender.HoneybadgerAppender=com.workable.honeybadger.log4j.HoneybadgerAppender
log4j.appender.HoneybadgerAppender.layout=org.apache.log4j.EnhancedPatternLayout
log4j.appender.HoneybadgerAppender.layout.ConversionPattern=[%-5p] %c - %m%n%throwable{none}
log4j.appender.HoneybadgerAppender.threshold=ERROR

log4j.appender.HoneybadgerAppender.apiKey=key
log4j.appender.HoneybadgerAppender.async=false
log4j.appender.HoneybadgerAppender.maxThreads=1
log4j.appender.HoneybadgerAppender.queueSize=50000
log4j.appender.HoneybadgerAppender.priority=1
```

### Additional data and information
It's possible to add extra details to events captured by the Log4j module
thanks to both [the MDC](https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/MDC.html)
and [the NDC](https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/NDC.html) systems provided by Log4j are
usable, allowing to attach extras information to the event.

