package bunny.boardhole.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 작업 설정
 * 비동기 메소드 실행을 위한 스레드 풀 설정을 담당합니다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 비동기 작업 실행자 빈 설정
     *
     * @return 스레드 풀 작업 실행자
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 현재 설정 (기본값)
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);

        // TODO: 성능 최적화 - 실제 부하 발생 시 아래 설정 활성화
        // 트래픽 증가로 비동기 작업 처리가 지연될 경우:
        // - 동시 처리 능력 향상을 위한 최적화 설정
        // - 모니터링 후 실제 부하 패턴에 맞게 조정 필요
        /*
        executor.setCorePoolSize(4);      // 기본 스레드 수 증가
        executor.setMaxPoolSize(20);      // 최대 스레드 수 증가
        executor.setQueueCapacity(200);   // 큐 용량 증가
        executor.setKeepAliveSeconds(60); // 유휴 스레드 유지 시간
        */

        // 추가 최적화 옵션 (필요시 활성화)
        /*
        // 스레드 풀 모니터링을 위한 설정
        executor.setTaskDecorator(runnable -> {
            // MDC 컨텍스트 복사 등 작업 데코레이터
            return () -> {
                long startTime = System.currentTimeMillis();
                try {
                    runnable.run();
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    if (duration > 1000) { // 1초 이상 걸린 작업 로깅
                        log.warn("Async task took {}ms", duration);
                    }
                }
            };
        });
        */

        executor.setThreadNamePrefix("Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);

        // RejectedExecutionHandler 설정 - 큐가 가득 찼을 때 호출한 스레드에서 실행
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}