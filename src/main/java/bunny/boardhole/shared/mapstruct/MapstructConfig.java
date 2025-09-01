package bunny.boardhole.shared.mapstruct;

import org.mapstruct.*;

@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
public interface MapstructConfig {
}

