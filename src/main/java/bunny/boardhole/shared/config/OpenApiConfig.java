package bunny.boardhole.shared.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.*;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;
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
                                .description("세션 기반 인증 - 로그인 후 자동으로 설정되는 세션 쿠키")));

        // Define ErrorField schema for validation errors
        Schema<?> errorField = new Schema<>()
                .type("object")
                .description("유효성 검증 오류 필드")
                .addProperty("field", new Schema<>().type("string").description("오류가 발생한 필드명").example("title"))
                .addProperty("message", new Schema<>().type("string").description("오류 메시지").example("제목을 입력해주세요"))
                .addProperty("rejectedValue", new Schema<>().type("object").description("거부된 값").example(""));
        api.getComponents().addSchemas("ErrorField", errorField);

        // Define ProblemDetailExtended schema extending Spring's ProblemDetail
        Schema<?> problemDetailExt = new Schema<>()
                .type("object")
                .description("RFC 7807 Problem Details with Extensions")
                .addProperty("type", new Schema<>().type("string").description("문제 유형 URI").example("urn:problem-type:validation-error"))
                .addProperty("title", new Schema<>().type("string").description("문제 제목").example("유효성 검증 실패"))
                .addProperty("status", new Schema<>().type("integer").description("HTTP 상태 코드").example(400))
                .addProperty("detail", new Schema<>().type("string").description("상세 오류 메시지").example("입력된 데이터가 올바르지 않습니다."))
                .addProperty("instance", new Schema<>().type("string").description("문제가 발생한 URI").example("/api/boards"))
                .addProperty("properties", new Schema<>()
                        .type("object")
                        .description("추가 속성")
                        .addProperty("traceId", new Schema<>().type("string").description("추적 ID").example("trace-123-456-789"))
                        .addProperty("path", new Schema<>().type("string").description("요청 경로").example("/api/boards"))
                        .addProperty("method", new Schema<>().type("string").description("HTTP 메서드").example("POST"))
                        .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("발생 시간").example("2025-09-01T01:23:45Z"))
                        .addProperty("errors", new Schema<>().type("array").description("유효성 검증 오류 목록").items(new Schema<>().$ref("#/components/schemas/ErrorField"))));
        api.getComponents().addSchemas("ProblemDetailExtended", problemDetailExt);

        return api;
    }

    /**
     * 모든 엔드포인트에 공통 오류 응답(ProblemDetail, application/problem+json)을 추가합니다.
     */
    @Bean
    public OpenApiCustomizer globalProblemDetailResponses() {
        return openApi -> {
            // Ensure schemas are present in the customizer phase
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }

            // Add ErrorField schema if not present
            if (openApi.getComponents().getSchemas() == null || !openApi.getComponents().getSchemas().containsKey("ErrorField")) {
                Schema<?> errorField = new Schema<>()
                        .type("object")
                        .description("유효성 검증 오류 필드")
                        .addProperty("field", new Schema<>().type("string").description("오류가 발생한 필드명").example("title"))
                        .addProperty("message", new Schema<>().type("string").description("오류 메시지").example("제목을 입력해주세요"))
                        .addProperty("rejectedValue", new Schema<>().type("object").description("거부된 값").example(""));
                openApi.getComponents().addSchemas("ErrorField", errorField);
            }

            // Add ProblemDetailExtended schema if not present
            if (openApi.getComponents().getSchemas() == null || !openApi.getComponents().getSchemas().containsKey("ProblemDetailExtended")) {
                Schema<?> problemDetailExt = new Schema<>()
                        .type("object")
                        .description("RFC 7807 Problem Details with Extensions")
                        .addProperty("type", new Schema<>().type("string").description("문제 유형 URI").example("urn:problem-type:validation-error"))
                        .addProperty("title", new Schema<>().type("string").description("문제 제목").example("유효성 검증 실패"))
                        .addProperty("status", new Schema<>().type("integer").description("HTTP 상태 코드").example(400))
                        .addProperty("detail", new Schema<>().type("string").description("상세 오류 메시지").example("입력된 데이터가 올바르지 않습니다."))
                        .addProperty("instance", new Schema<>().type("string").description("문제가 발생한 URI").example("/api/boards"))
                        .addProperty("properties", new Schema<>()
                                .type("object")
                                .description("추가 속성")
                                .addProperty("traceId", new Schema<>().type("string").description("추적 ID").example("trace-123-456-789"))
                                .addProperty("path", new Schema<>().type("string").description("요청 경로").example("/api/boards"))
                                .addProperty("method", new Schema<>().type("string").description("HTTP 메서드").example("POST"))
                                .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("발생 시간").example("2025-09-01T01:23:45Z"))
                                .addProperty("errors", new Schema<>().type("array").description("유효성 검증 오류 목록").items(new Schema<>().$ref("#/components/schemas/ErrorField"))));
                openApi.getComponents().addSchemas("ProblemDetailExtended", problemDetailExt);
            }

            // Add error responses to all operations
            openApi.getPaths().forEach((path, pathItem) -> {
                pathItem.readOperations().forEach(operation -> {
                    ApiResponses responses = operation.getResponses();
                    addProblemResponse(responses, "400", "Bad Request");
                    addProblemResponse(responses, "401", "Unauthorized");
                    addProblemResponse(responses, "403", "Forbidden");
                    addProblemResponse(responses, "404", "Not Found");
                    addProblemResponse(responses, "500", "Internal Server Error");
                });
            });
        };
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
                java.util.Map<String, Object> properties = new java.util.HashMap<>();
                properties.put("traceId", "trace-123-456-789");
                properties.put("path", "/api/boards");
                properties.put("method", "POST");
                properties.put("timestamp", "2025-09-01T01:23:45Z");
                properties.put("errors", java.util.List.of(
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
                map.put("properties", properties);
                yield map;
            }
            case "401" -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("type", "urn:problem-type:unauthorized");
                map.put("title", "인증 필요");
                map.put("status", 401);
                map.put("detail", "로그인이 필요합니다.");
                map.put("instance", "/api/boards");
                java.util.Map<String, Object> properties = new java.util.HashMap<>();
                properties.put("traceId", "trace-456-789-123");
                properties.put("path", "/api/boards");
                properties.put("method", "POST");
                properties.put("timestamp", "2025-09-01T01:23:45Z");
                map.put("properties", properties);
                yield map;
            }
            case "403" -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("type", "urn:problem-type:forbidden");
                map.put("title", "접근 거부");
                map.put("status", 403);
                map.put("detail", "권한이 부족합니다.");
                map.put("instance", "/api/admin/users");
                java.util.Map<String, Object> properties = new java.util.HashMap<>();
                properties.put("traceId", "trace-987-654-321");
                properties.put("path", "/api/admin/users");
                properties.put("method", "GET");
                properties.put("timestamp", "2025-09-01T01:23:45Z");
                map.put("properties", properties);
                yield map;
            }
            case "404" -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("type", "urn:problem-type:not-found");
                map.put("title", "리소스를 찾을 수 없음");
                map.put("status", 404);
                map.put("detail", "게시글을 찾을 수 없습니다.");
                map.put("instance", "/api/boards/999");
                java.util.Map<String, Object> properties = new java.util.HashMap<>();
                properties.put("traceId", "trace-abc-def-ghi");
                properties.put("path", "/api/boards/999");
                properties.put("method", "GET");
                properties.put("timestamp", "2025-09-01T01:23:45Z");
                map.put("properties", properties);
                yield map;
            }
            case "409" -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("type", "urn:problem-type:duplicate-username");
                map.put("title", "중복된 사용자명");
                map.put("status", 409);
                map.put("detail", "이미 사용 중인 사용자명입니다.");
                map.put("instance", "/api/auth/signup");
                java.util.Map<String, Object> properties = new java.util.HashMap<>();
                properties.put("traceId", "trace-jkl-mno-pqr");
                properties.put("path", "/api/auth/signup");
                properties.put("method", "POST");
                properties.put("timestamp", "2025-09-01T01:23:45Z");
                map.put("properties", properties);
                yield map;
            }
            default -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("type", "urn:problem-type:internal-error");
                map.put("title", "내부 서버 오류");
                map.put("status", 500);
                map.put("detail", "서버 내부 오류가 발생했습니다.");
                map.put("instance", "/api/boards");
                java.util.Map<String, Object> properties = new java.util.HashMap<>();
                properties.put("traceId", "trace-xxx-yyy-zzz");
                properties.put("path", "/api/boards");
                properties.put("method", "POST");
                properties.put("timestamp", "2025-09-01T01:23:45Z");
                map.put("properties", properties);
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
