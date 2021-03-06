import ch.qos.logback.core.*
import ch.qos.logback.classic.encoder.PatternLayoutEncoder

appender(name="CONSOLE", clazz=ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "date=%date{ISO8601} level=%level class=%logger{25} actor=%X{akkaSource} message=%msg\n"
    }
}

root(level=INFO, appenderNames=["CONSOLE"])
