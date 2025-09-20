package dev.xiyo.bunnyholes.boardhole.testsupport.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.xiyo.bunnyholes.boardhole.user.domain.User;

class EntityTestBaseSmokeTest extends EntityTestBase {

    @Test
    @DisplayName("EntityTestBase 컨테이너 연결 및 JPA 작동 스모크 테스트")
    void canPersistUserViaEntityManager() {
        // given
        User user = createTestUser();

        // when
        User saved = entityManager.persistAndFlush(user);

        // then
        assertThat(saved.getId()).isNotNull();
    }
}

