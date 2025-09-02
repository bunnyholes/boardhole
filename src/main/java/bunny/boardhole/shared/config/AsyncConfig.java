package bunny.boardhole.shared.config;

import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 설정 - Spring Boot 기본 설정 활용
 * 대부분의 설정은 application.yml의 spring.task.execution으로 관리
 */
@Configuration
@EnableAsync
@SuppressWarnings("PMD.DoNotUseThreads") // 비동기 설정을 위한 정당한 스레드 사용
public class AsyncConfig {

    /**
     * Spring Boot TaskExecutor 빈 설정
     *
     * @param taskProps Spring Boot TaskExecutionProperties  
     * @return 커스터마이징된 TaskExecutor
     */
    @Bean
    public ThreadPoolTaskExecutor applicationTaskExecutor(final TaskExecutionProperties taskProps) {
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

        // 상수 선언
        final String threadNamePrefix = "app-async-";
        
        // Spring Boot properties 적용 - Law of Demeter 준수
        final TaskExecutionProperties.Pool pool = taskProps.getPool();
        final int coreSize = pool.getCoreSize();
        final int maxSize = pool.getMaxSize();
        final int queueCapacity = pool.getQueueCapacity();
        
        taskExecutor.setCorePoolSize(coreSize);
        taskExecutor.setMaxPoolSize(maxSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setThreadNamePrefix(threadNamePrefix);

        // RejectedExecutionHandler 설정
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // MDC 컨텍스트 전파를 위한 TaskDecorator
        taskExecutor.setTaskDecorator(originalRunnable -> {
            final Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                try {
                    originalRunnable.run();
                } finally {
                    MDC.clear();
                }
            };
        });

        taskExecutor.initialize();
        return taskExecutor;
    }
}