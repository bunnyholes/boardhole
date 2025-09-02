package bunny.boardhole.shared.config;

import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class AsyncConfig {

    /**
     * Spring Boot TaskExecutor 빈 설정
     * - RejectedExecutionHandler: 큐가 가득 찼을 때 처리 방식
     * - TaskDecorator: MDC 컨텍스트 전파
     * application.yml의 spring.task.execution 설정과 함께 작동
     * 
     * @param properties Spring Boot TaskExecutionProperties
     * @return 커스터마이징된 TaskExecutor
     */
    @Bean
    public ThreadPoolTaskExecutor applicationTaskExecutor(TaskExecutionProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Spring Boot properties 적용
        executor.setCorePoolSize(properties.getPool().getCoreSize());
        executor.setMaxPoolSize(properties.getPool().getMaxSize());
        executor.setQueueCapacity(properties.getPool().getQueueCapacity());
        executor.setThreadNamePrefix("app-async-");
        
        // RejectedExecutionHandler 설정
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // MDC 컨텍스트 전파를 위한 TaskDecorator
        executor.setTaskDecorator(runnable -> {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                try {
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        });
        
        executor.initialize();
        return executor;
    }
}