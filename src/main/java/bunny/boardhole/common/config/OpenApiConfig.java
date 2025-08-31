package bunny.boardhole.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI boardHoleOpenAPI() {
        OpenAPI api = new OpenAPI()
                .info(new Info()
                        .title("Board-Hole API")
                        .description("간단한 게시판 시스템 REST API - Spring Boot 3.5와 CQRS 패턴을 사용한 현대적인 웹 애플리케이션")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Board-Hole 개발팀")
                                .email("admin@boardhole.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("session", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("세션 기반 인증 - 로그인 후 자동으로 설정되는 세션 쿠키"))
                        .addSecuritySchemes("basic-auth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("개발 환경 - HTTP Basic 인증 (admin/admin123 또는 user/user123)")));

        // Define ErrorField and ProblemDetailExtended schemas for consistent error documentation
        Schema<?> errorField = new Schema<>()
                .type("object")
                .addProperties("field", new Schema<>().type("string").example("title"))
                .addProperties("message", new Schema<>().type("string").example("제목을 입력해주세요"))
                .addProperties("rejectedValue", new Schema<>().type("string").example(""));
        api.getComponents().addSchemas("ErrorField", errorField);

        Schema<?> problemDetailExt = new Schema<>()
                .allOf(java.util.List.of(
                        new Schema<ProblemDetail>().$ref("#/components/schemas/ProblemDetail")
                ))
                .addProperties("code", new Schema<>().type("string").example("VALIDATION_ERROR"))
                .addProperties("path", new Schema<>().type("string").example("/api/boards"))
                .addProperties("method", new Schema<>().type("string").example("POST"))
                .addProperties("timestamp", new Schema<>().type("string").format("date-time"))
                .addProperties("traceId", new Schema<>().type("string"))
                .addProperties("errors", new Schema<>().type("array").items(new Schema<>().$ref("#/components/schemas/ErrorField")));
        api.getComponents().addSchemas("ProblemDetailExtended", problemDetailExt);

        return api;
    }

    /**
     * 모든 엔드포인트에 공통 오류 응답(ProblemDetail, application/problem+json)을 추가합니다.
     */
    @Bean
    public OpenApiCustomizer globalProblemDetailResponses() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
            pathItem.readOperations().forEach(operation -> {
                ApiResponses responses = operation.getResponses();
                addProblemResponse(responses, "400", "Bad Request");
                addProblemResponse(responses, "401", "Unauthorized");
                addProblemResponse(responses, "403", "Forbidden");
                addProblemResponse(responses, "404", "Not Found");
                addProblemResponse(responses, "500", "Internal Server Error");
            });
        });
    }

    private void addProblemResponse(ApiResponses responses, String code, String description) {
        if (responses.containsKey(code)) return;
        Schema<?> schema = new Schema<ProblemDetail>().$ref("#/components/schemas/ProblemDetailExtended");

        Object example = switch (code) {
            case "400" -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("type", "urn:problem-type:validation-error");
                map.put("title", "유효성 검증 실패");
                map.put("status", 400);
                map.put("detail", "입력된 데이터가 올바르지 않습니다.");
                map.put("instance", "/api/boards");
                map.put("code", "VALIDATION_ERROR");
                map.put("path", "/api/boards");
                map.put("method", "POST");
                map.put("timestamp", "2025-09-01T01:23:45Z");
                map.put("traceId", "trace-123-456-789");
                map.put("errors", java.util.List.of(
                        java.util.Map.of(
                                "field", "title",
                                "message", "제목을 입력해주세요",
                                "rejectedValue", ""
                        ),
                        java.util.Map.of(
                                "field", "content",
                                "message", "내용은 10자 이상이어야 합니다",
                                "rejectedValue", "short"
                        )
                ));
                yield map;
            }
            case "401" -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("type", "urn:problem-type:unauthorized");
                map.put("title", "인증 실패");
                map.put("status", 401);
                map.put("detail", "로그인이 필요합니다.");
                map.put("instance", "/api/boards");
                map.put("code", "UNAUTHORIZED");
                map.put("path", "/api/boards");
                map.put("method", "POST");
                map.put("timestamp", "2025-09-01T01:23:45Z");
                yield map;
            }
            case "403" -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("type", "urn:problem-type:forbidden");
                map.put("title", "접근 거부");
                map.put("status", 403);
                map.put("detail", "권한이 부족합니다.");
                map.put("instance", "/api/admin/users");
                map.put("code", "FORBIDDEN");
                map.put("path", "/api/admin/users");
                map.put("method", "GET");
                map.put("timestamp", "2025-09-01T01:23:45Z");
                map.put("traceId", "trace-987-654-321");
                yield map;
            }
            case "404" -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("type", "urn:problem-type:not-found");
                map.put("title", "리소스를 찾을 수 없음");
                map.put("status", 404);
                map.put("detail", "게시글을 찾을 수 없습니다.");
                map.put("instance", "/api/boards/999");
                map.put("code", "NOT_FOUND");
                map.put("path", "/api/boards/999");
                map.put("method", "GET");
                map.put("timestamp", "2025-09-01T01:23:45Z");
                map.put("traceId", "trace-abc-def-ghi");
                yield map;
            }
            case "409" -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("type", "urn:problem-type:duplicate-username");
                map.put("title", "중복된 사용자명");
                map.put("status", 409);
                map.put("detail", "이미 사용 중인 사용자명입니다.");
                map.put("instance", "/api/auth/signup");
                map.put("code", "USER_DUPLICATE_USERNAME");
                map.put("path", "/api/auth/signup");
                map.put("method", "POST");
                map.put("timestamp", "2025-09-01T01:23:45Z");
                yield map;
            }
            default -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("type", "urn:problem-type:internal-error");
                map.put("title", "내부 서버 오류");
                map.put("status", 500);
                map.put("detail", "서버 내부 오류가 발생했습니다.");
                map.put("instance", "/api/boards");
                map.put("code", "INTERNAL_ERROR");
                map.put("path", "/api/boards");
                map.put("method", "POST");
                map.put("timestamp", "2025-09-01T01:23:45Z");
                map.put("traceId", "trace-xxx-yyy-zzz");
                yield map;
            }
        };

        MediaType mediaType = new MediaType()
                .schema(schema)
                .example(example);
        Content content = new Content()
                .addMediaType("application/problem+json", mediaType);
        responses.addApiResponse(code, new ApiResponse().description(description).content(content));
    }
}
