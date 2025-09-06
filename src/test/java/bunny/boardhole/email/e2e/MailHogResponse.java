package bunny.boardhole.email.e2e;

import java.util.List;

/**
 * MailHog API 응답 래퍼
 */
record MailHogResponse(int total, int count, int start, List<MailHogMessage> items) {
}