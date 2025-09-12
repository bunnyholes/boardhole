package bunny.boardhole.testsupport.jpa;

import java.time.Duration;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * JVM 전체에서 한 번만 기동되는 MySQL Testcontainer (싱글톤).
 * 테스트 실행 내내 유지되며, 세부 옵션은 최소화합니다.
 */
public final class TestMySQL extends MySQLContainer<TestMySQL> {

    private static final String IMAGE = "mysql:8.4";

    public static final TestMySQL INSTANCE = new TestMySQL();

    private TestMySQL() {
        super(DockerImageName.parse(IMAGE));
        // 컨테이너가 실제로 접속 가능한 상태가 될 때까지 명확히 대기
        waitingFor(Wait.forLogMessage(".*ready for connections.*", 1)
                      .withStartupTimeout(Duration.ofSeconds(90)));
        withStartupTimeout(Duration.ofSeconds(90));
    }

    static {
        // 테스트 JVM 시작 후 최초 접근 시 한 번만 기동
        INSTANCE.start();
    }
}

