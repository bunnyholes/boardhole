package dev.xiyo.bunnyholes.boardhole.testsupport.container;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class ContainersConfig {

    /**
     * 테스트용 JPA 프로퍼티 동적 설정
     */
    @DynamicPropertySource
    static void jpaProperties(DynamicPropertyRegistry registry) {
        // Testcontainers 환경에서는 매번 새로운 DB이므로 create 사용
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        // PostgreSQL 17 dialect 명시
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    /**
     * PostgreSQL 컨테이너 - 모든 테스트에서 공유 가능한 Bean
     *
     * @ServiceConnection 으로 DataSource 자동 구성
     */
    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"));
    }
}
