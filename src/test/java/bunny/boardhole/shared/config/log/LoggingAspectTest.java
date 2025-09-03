package bunny.boardhole.shared.config.log;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.*;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingAspectTest {

    private LoggingAspect loggingAspect;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        LoggingProperties properties = new LoggingProperties();
        LogFormatter formatter = new LogFormatter(ms, properties);
        loggingAspect = new LoggingAspect(ms, formatter);
    }

    @Test
    void methodEndDoesNotLogReturnValue() throws InterruptedException {
        DummyService target = new DummyService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(loggingAspect);
        factory.getProxy();

        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(LoggingAspect.class);
        logger.setLevel(Level.DEBUG);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        DummyService.doWork();

        assertThat(appender.list.stream().anyMatch(e -> e.getFormattedMessage().contains("sensitive"))).isFalse();
    }

    static class DummyService {
        static void doWork() throws InterruptedException {
            Thread.sleep(20); // ensure tookMs > 10 for logging
        }
    }
}

