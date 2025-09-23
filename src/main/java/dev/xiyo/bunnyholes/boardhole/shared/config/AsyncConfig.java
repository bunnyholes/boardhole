package dev.xiyo.bunnyholes.boardhole.shared.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
     * application.yml의 spring.task.execution 설정과 함께 작동
     *
     * @param properties Spring Boot TaskExecutionProperties
     * @return 커스터마이징된 TaskExecutor
     */
    @Bean
    public ThreadPoolTaskExecutor applicationTaskExecutor(TaskExecutionProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Spring Boot properties 적용
        TaskExecutionProperties.Pool pool = properties.getPool();
        executor.setCorePoolSize(pool.getCoreSize());
        executor.setMaxPoolSize(pool.getMaxSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setThreadNamePrefix("app-async-");

        // RejectedExecutionHandler 설정
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}