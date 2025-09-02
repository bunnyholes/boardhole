package bunny.boardhole.shared.mapstruct;

import org.mapstruct.*;

/**
 * MapStruct 매퍼 전역 설정을 정의하는 컴포넌트입니다.
 * 모든 MapStruct 매퍼에서 공통으로 사용되는 설정을 중앙 집중화합니다.
 * Spring 컴포넌트로 등록되며, 매핑되지 않은 필드에 대해 에러를 발생시킵니다.
 * 
 * <p>주요 설정:</p>
 * <ul>
 *   <li><strong>componentModel = "spring":</strong> Spring 컴포넌트로 생성</li>
 *   <li><strong>unmappedTargetPolicy = ERROR:</strong> 매핑되지 않은 필드 에러 발생</li>
 *   <li><strong>nullValuePropertyMappingStrategy = SET_TO_NULL:</strong> null 값 매핑 전략</li>
 * </ul>
 * 
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
public interface MapstructConfig {
}

