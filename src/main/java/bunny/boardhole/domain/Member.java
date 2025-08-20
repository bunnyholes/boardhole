package bunny.boardhole.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 회원 도메인 클래스
 * MyBatis와 매핑되는 엔티티
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    /**
     * 회원 ID (Primary Key)
     */
    private Long id;

    /**
     * 회원 이름 (Unique)
     */
    private String memberName;

    /**
     * 비밀번호
     */
    private String password;

    /**
     * 생성일시
     */
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    private LocalDateTime updatedAt;

    /**
     * 회원 생성을 위한 편의 생성자
     */
    public Member(String memberName, String password) {
        this.memberName = memberName;
        this.password = password;
    }
}