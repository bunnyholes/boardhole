-- 검색 성능 개선을 위한 인덱스 추가

-- Board 테이블 검색 인덱스
CREATE INDEX idx_board_title ON boards(title);
CREATE INDEX idx_board_created_at ON boards(created_at DESC);

-- 복합 인덱스 (제목과 내용 검색용)
-- MySQL의 경우 Full-text 인덱스 사용 가능
-- H2의 경우 일반 인덱스 사용
CREATE INDEX idx_board_title_content ON boards(title, content);

-- User 테이블 검색 인덱스
CREATE INDEX idx_user_username ON user(username);
CREATE INDEX idx_user_email ON user(email);
CREATE INDEX idx_user_name ON user(name);

-- Foreign Key 인덱스 (조인 성능 개선)
CREATE INDEX idx_board_author_id ON boards(author_id);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);