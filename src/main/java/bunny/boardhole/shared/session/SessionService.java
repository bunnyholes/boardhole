package bunny.boardhole.shared.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 세션 관리 서비스
 * Redis에 저장된 세션을 관리하고 모니터링하는 유틸리티 서비스입니다.
 * 활성 세션 조회, 특정 사용자 세션 무효화, 세션 통계 등을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String SESSION_NAMESPACE = "board-hole:session:sessions:";
    private static final String INDEX_NAMESPACE = "board-hole:session:index:";
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 현재 활성 세션 수 조회
     *
     * @return 활성 세션 수
     */
    public long getActiveSessionCount() {
        Set<String> sessionKeys = redisTemplate.keys(SESSION_NAMESPACE + "*");
        return sessionKeys != null ? sessionKeys.size() : 0;
    }

    /**
     * 특정 사용자의 모든 세션 조회
     *
     * @param username 사용자명
     * @return 세션 ID 목록
     */
    public Set<String> getUserSessions(String username) {
        String indexKey = INDEX_NAMESPACE +
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME +
                ":" + username;

        Set<Object> sessionIds = redisTemplate.opsForSet().members(indexKey);

        if (sessionIds != null) {
            return sessionIds.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }

    /**
     * 특정 사용자의 모든 세션 무효화
     *
     * @param username 사용자명
     * @return 무효화된 세션 수
     */
    public int invalidateUserSessions(String username) {
        Set<String> sessionIds = getUserSessions(username);
        int count = 0;

        for (String sessionId : sessionIds) {
            if (invalidateSession(sessionId)) {
                count++;
            }
        }

        log.info("사용자 {} 의 {} 개 세션이 무효화되었습니다.", username, count);
        return count;
    }

    /**
     * 특정 세션 무효화
     *
     * @param sessionId 세션 ID
     * @return 성공 여부
     */
    public boolean invalidateSession(String sessionId) {
        String sessionKey = SESSION_NAMESPACE + sessionId;
        Boolean deleted = redisTemplate.delete(sessionKey);

        if (deleted) {
            log.info("세션 {} 이(가) 무효화되었습니다.", sessionId);
            return true;
        }

        return false;
    }

    /**
     * 세션 정보 조회
     *
     * @param sessionId 세션 ID
     * @return 세션 정보 맵
     */
    public Map<String, Object> getSessionInfo(String sessionId) {
        String sessionKey = SESSION_NAMESPACE + sessionId;
        Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(sessionKey);

        if (sessionData.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("sessionId", sessionId);

        // 세션 데이터 변환
        sessionData.forEach((key, value) -> {
            String keyStr = key.toString();

            switch (keyStr) {
                case "creationTime":
                    sessionInfo.put("creationTime", parseTime(value));
                    break;
                case "lastAccessedTime":
                    sessionInfo.put("lastAccessedTime", parseTime(value));
                    break;
                case "maxInactiveInterval":
                    sessionInfo.put("maxInactiveInterval", value);
                    break;
                case "sessionAttr:SPRING_SECURITY_CONTEXT":
                    // 보안 컨텍스트는 민감정보이므로 존재 여부만 표시
                    sessionInfo.put("authenticated", true);
                    break;
                default:
                    if (keyStr.startsWith("sessionAttr:")) {
                        String attrName = keyStr.substring("sessionAttr:".length());
                        sessionInfo.put(attrName, value);
                    }
            }
        });

        // TTL 계산
        Long ttl = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);
        if (ttl != null && ttl > 0) {
            sessionInfo.put("ttlSeconds", ttl);
        }

        return sessionInfo;
    }

    /**
     * 세션 통계 조회
     *
     * @return 세션 통계 정보
     */
    public Map<String, Object> getSessionStatistics() {
        Map<String, Object> stats = new HashMap<>();

        Set<String> sessionKeys = redisTemplate.keys(SESSION_NAMESPACE + "*");
        if (sessionKeys == null || sessionKeys.isEmpty()) {
            stats.put("totalSessions", 0);
            stats.put("authenticatedSessions", 0);
            stats.put("anonymousSessions", 0);
            return stats;
        }

        int totalSessions = sessionKeys.size();
        int authenticatedSessions = 0;
        int anonymousSessions = 0;
        List<Long> ttlList = new ArrayList<>();

        for (String key : sessionKeys) {
            Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(key);

            // 인증 여부 확인
            boolean isAuthenticated = sessionData.containsKey("sessionAttr:SPRING_SECURITY_CONTEXT");
            if (isAuthenticated) {
                authenticatedSessions++;
            } else {
                anonymousSessions++;
            }

            // TTL 수집
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl != null && ttl > 0) {
                ttlList.add(ttl);
            }
        }

        stats.put("totalSessions", totalSessions);
        stats.put("authenticatedSessions", authenticatedSessions);
        stats.put("anonymousSessions", anonymousSessions);

        // 평균 TTL 계산
        if (!ttlList.isEmpty()) {
            double avgTtl = ttlList.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            stats.put("averageTtlSeconds", Math.round(avgTtl));
        }

        return stats;
    }

    /**
     * 만료 임박 세션 조회 (5분 이내)
     *
     * @return 만료 임박 세션 ID 목록
     */
    public Set<String> getExpiringSessions() {
        return getExpiringSessions(300);  // 5분 = 300초
    }

    /**
     * 지정된 시간 이내에 만료될 세션 조회
     *
     * @param withinSeconds 초 단위 시간
     * @return 만료 임박 세션 ID 목록
     */
    public Set<String> getExpiringSessions(long withinSeconds) {
        Set<String> expiringSessions = new HashSet<>();
        Set<String> sessionKeys = redisTemplate.keys(SESSION_NAMESPACE + "*");

        if (sessionKeys != null) {
            for (String key : sessionKeys) {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0 && ttl <= withinSeconds) {
                    String sessionId = key.substring(SESSION_NAMESPACE.length());
                    expiringSessions.add(sessionId);
                }
            }
        }

        return expiringSessions;
    }

    /**
     * 세션 TTL 연장
     *
     * @param sessionId         세션 ID
     * @param additionalSeconds 추가할 초
     * @return 성공 여부
     */
    public boolean extendSessionTtl(String sessionId, long additionalSeconds) {
        String sessionKey = SESSION_NAMESPACE + sessionId;

        Long currentTtl = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);
        if (currentTtl != null && currentTtl > 0) {
            long newTtl = currentTtl + additionalSeconds;
            Boolean result = redisTemplate.expire(sessionKey, newTtl, TimeUnit.SECONDS);

            if (result) {
                log.info("세션 {} 의 TTL이 {} 초 연장되었습니다.", sessionId, additionalSeconds);
                return true;
            }
        }

        return false;
    }

    /**
     * 시간 값 파싱 헬퍼 메서드
     */
    private String parseTime(Object value) {
        if (value instanceof Long) {
            return Instant.ofEpochMilli((Long) value).toString();
        }
        return value.toString();
    }
}