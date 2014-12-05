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
log4j.appender.HoneybadgerAppender.apiKey=${honeybadgerKey}
```

## Additional Options

### Async mode
The error dispatching to honebadger.io is performed asynchronously via http in order to avoid performance impact.

To disable the async mode simply set the `async` option to `false`:

```properties
log4j.appender.HoneybadgerAppender.async=false
```

### Max Threads
By default the thread pool used for async dispatching contains one thread per
processor available to the JVM.

It's possible to manually set the number of threads (for example if you want
only one thread) with the option `maxThreads`:

```properties
log4j.appender.HoneybadgerAppender.maxThreads=1
```

### Queue Size
The default queue used to store the not yet processed events doesn't have a
limit.
Depending on the environment (if the memory is sparse) it is important to be
able to control the size of that queue to avoid memory issues.

It is possible to set a maximum with the option `queuesize`:

```properties
log4j.appender.HoneybadgerAppender.queueSize=50000
```

### Thread Priority
As in most cases sending error to Honebadger isn't as important as an application
running smoothly, the threads have a
[minimal priority](http://docs.oracle.com/javase/6/docs/api/java/lang/Thread.html#MIN_PRIORITY).

It is possible to customise this value to increase the priority of those threads
with the option `priority`:

```properties
log4j.appender.HoneybadgerAppender.priority=1
```

### Additional data and information
It's possible to add extra details to events captured by the Log4j module
thanks to [the MDC](https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/MDC.html)
systems provided by Log4j are usable, allowing to attach extras information to the event.

