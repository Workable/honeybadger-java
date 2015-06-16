# honeybadger-logback-appender
[logback](http://logback.qos.ch/) support for honeybadger.
It provides an [`Appender`](http://logback.qos.ch/manual/appenders.html)
for logback to send the logged events to honeybadger.

## Usage
### Configuration
In the `logback-test.xml` file configure an appender named `HoneybadgerAppender`:

```properties
<appender name="honeybadger" class="com.workable.honeybadger.logback.HoneybadgerAppender">
     <param name="Threshold" value="ERROR" />
     <param name="apiKey" value="${honeybadgerKey}" />
     <encoder>
         <pattern>%-4relative [%thread] %-5level %logger{35} - %msg %n</pattern>
     </encoder>
</appender>
```

## Additional Options

### Async mode
The error dispatching to honebadger.io is performed asynchronously via http in order to avoid performance impact.

To disable the async mode simply set the `async` option to `false`:

```xml
<param name="async" value="false" />
```

### Max Threads
By default the thread pool used for async dispatching contains one thread per
processor available to the JVM.

It's possible to manually set the number of threads (for example if you want
only one thread) with the option `maxThreads`:

```properties
<param name="maxThreads" value="1" />
```

### Queue Size
The default queue used to store the not yet processed events doesn't have a
limit.
Depending on the environment (if the memory is sparse) it is important to be
able to control the size of that queue to avoid memory issues.

It is possible to set a maximum with the option `queuesize`:

```properties
<param name="queuesize" value="10" />
```

### Thread Priority
As in most cases sending error to Honebadger isn't as important as an application
running smoothly, the threads have a
[minimal priority](http://docs.oracle.com/javase/6/docs/api/java/lang/Thread.html#MIN_PRIORITY).

It is possible to customise this value to increase the priority of those threads
with the option `priority`:

```properties
<param name="priority" value="1" />
```

### Additional data and information
It's possible to add extra details to events captured by the Logback module
thanks to [the MDC](http://logback.qos.ch/manual/mdc.html)
systems provided by Logback are usable, allowing to attach extras information to the event.

