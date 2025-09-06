package bunny.boardhole.email.e2e;

import java.util.List;
import java.util.Map;

/**
 * MailHog API 응답을 위한 DTO
 */
record MailHogMessage(String ID, From from, List<To> to, Content content, String created, String subject) {
    private record From(String relays, String mailbox, String domain, String params) {
    }

    private record To(String relays, String mailbox, String domain, String params) {
    }

    record Content(Map<String, List<String>> headers, String body, int size, String mIME) {
        String getSubject() {
            List<String> subjectHeaders = headers.get("Subject");
            return subjectHeaders != null && !subjectHeaders.isEmpty() ? subjectHeaders.getFirst() : "";
        }

        String getToHeader() {
            List<String> toHeaders = headers.get("To");
            return toHeaders != null && !toHeaders.isEmpty() ? toHeaders.getFirst() : "";
        }

    }
}