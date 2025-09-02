package bunny.boardhole.admin.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 시스템 설정 업데이트 명령
 * CQRS 패턴의 Command 객체로 관리자의 시스템 설정 변경 요청을 나타냅니다.
 */
@Schema(name = "UpdateSystemSettingsCommand", description = "시스템 설정 업데이트 명령 - CQRS 패턴의 Command 객체")
public record UpdateSystemSettingsCommand(
        @Schema(description = "설정 키", example = "max_file_upload_size")
        @NotBlank
        @Size(min = 1, max = 100)
        String settingKey,

        @Schema(description = "설정 값", example = "10485760")
        @NotBlank
        @Size(min = 1, max = 1000)
        String settingValue
) {
}