package bunny.boardhole.testsupport.mvc;

import bunny.boardhole.shared.exception.GlobalExceptionHandler;
import bunny.boardhole.shared.properties.ProblemProperties;

/**
 * 컨트롤러 단위 테스트를 위한 GlobalExceptionHandler 설정 유틸리티
 */
public class GlobalExceptionHandlerTestSetup {

    /**
     * 테스트용 GlobalExceptionHandler 인스턴스 생성
     * 
     * @return GlobalExceptionHandler 인스턴스
     */
    public static GlobalExceptionHandler createTestGlobalExceptionHandler() {
        // 테스트용 ProblemProperties 생성
        ProblemProperties problemProperties = new ProblemProperties("");
        return new GlobalExceptionHandler(problemProperties);
    }
}