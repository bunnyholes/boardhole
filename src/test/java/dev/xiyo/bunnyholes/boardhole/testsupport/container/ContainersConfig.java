package dev.xiyo.bunnyholes.boardhole.testsupport.container;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
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
        // MySQL 8 dialect 명시
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");
    }

    /**
     * MySQL 컨테이너 - 모든 테스트에서 공유 가능한 Bean
     *
     * @ServiceConnection 으로 DataSource 자동 구성
     */
    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysql() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.4"));
    }
}
