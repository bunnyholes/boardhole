package bunny.boardhole.testsupport.e2e;

public record SessionCookie(String name, String value) {
    public boolean isPresent() {
        return value != null && !value.isBlank();
    }
}

