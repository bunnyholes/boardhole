# Supabase PostgreSQL Connection Modes

Supabase는 PostgreSQL에 접근할 수 있는 세 가지 주요 연결 방식을 제공한다. 각 모드는 **커넥션 관리 방식(pgBouncer 여부)**과 네트워크 호환성(IPv4/IPv6), 기능적 제약에 따라 선택된다.

## 1. Direct Connection

### 개요
Supabase의 PostgreSQL 서버에 직접 연결하는 방식. pgbouncer(풀러)를 거치지 않고, 클라이언트가 Postgres 인스턴스와 바로 TCP 세션을 맺는다.

### 연결 문자열
```
postgresql://postgres:[YOUR_PASSWORD]@db.<project_ref>.supabase.co:5432/postgres
```

### 특징

| 항목 | 지원 여부 |
|------|---------|
| **커넥션 풀링** | ❌ 비활성 (직접 연결) |
| **IPv4 지원** | ❌ (IPv6 전용) |
| **기능 제한** | 없음 — 모든 PostgreSQL 기능 사용 가능 |
| **LISTEN/NOTIFY** | ✅ 지원 |
| **Logical Replication** | ✅ 지원 |
| **WAL 레플리케이션** | ✅ 지원 |
| **Supabase 공식 지원** | ✅ 정식 지원 (IPv6 네트워크에서만) |

### 권장 용도
- 장기 실행 서버 (항상 연결 유지)
- PowerSync 사용 시
- Logical Replication / CDC 필요
- WAL 리플리케이션 필요
- IPv6 기반 서버 환경

### 제약사항
- IPv6 환경에서만 사용 가능
- 연속적인 커넥션 유지 필요 (서버리스 환경 부적합)

---

## 2. Transaction Pooler (트랜잭션 풀러)

### 개요
Supabase가 제공하는 pgBouncer 풀러를 **트랜잭션 단위**로 사용하는 연결 모드. 각 트랜잭션이 끝날 때마다 커넥션이 풀로 반환되어 다른 클라이언트에게 재사용된다.

### 연결 문자열
```
postgresql://postgres.[project_ref]:[PASSWORD]@aws-0-[region].pooler.supabase.com:5432/postgres
```

### 특징

| 항목 | 지원 여부 |
|------|---------|
| **커넥션 풀링** | ✅ 트랜잭션 단위 |
| **IPv4 지원** | ✅ |
| **세션 상태 유지** | ❌ 불가능 |
| **SET LOCAL** | ❌ 불가능 |
| **임시 테이블** | ❌ 불가능 |
| **Cursor** | ❌ 불가능 |
| **LISTEN/NOTIFY** | ❌ 불가능 |
| **복제 기능** | ❌ 불가능 |
| **Supabase 공식 지원** | ✅ 정식 지원 (기본 Pooler) |

### 권장 용도
- AWS Lambda / Google Cloud Functions 등 서버리스 함수
- REST API (단일 쿼리 중심)
- Edge Functions
- 단기 연결 워크로드
- 높은 동시성이 필요한 CRUD 작업

### 제약사항
- **영속 세션 기반 ORM 부적합** (Hibernate, Prisma 등)
- 트랜잭션 간 상태 유지 불가능
- 복잡한 트랜잭션 처리 제한

---

## 3. Session Pooler (세션 풀러)

### 개요
Supabase의 Session-level pgBouncer 풀러를 통해 **IPv4 네트워크에서도 연결 가능**하게 만든 모드. 커넥션이 "세션 단위"로 유지되어 트랜잭션 풀러보다 안정적이다.

### 연결 문자열
```
postgresql://user=postgres.[project_ref] password=[PASSWORD] host=aws-0-[region].pooler.supabase.com port=5432 dbname=postgres
```

또는 JDBC 형식:
```
jdbc:postgresql://aws-0-[region].pooler.supabase.com:5432/postgres?user=postgres.[project_ref]&password=[PASSWORD]
```

### 특징

| 항목 | 지원 여부 |
|------|---------|
| **커넥션 풀링** | ✅ 세션 단위 |
| **IPv4 지원** | ✅ |
| **세션 상태 유지** | ✅ (트랜잭션 간 일부 상태 유지) |
| **SET LOCAL** | ⚠️ 제한적 지원 |
| **임시 테이블** | ⚠️ 제한적 지원 |
| **Logical Replication** | ❌ 불가능 |
| **LISTEN/NOTIFY** | ❌ 불가능 |
| **WAL 리플리케이션** | ❌ 불가능 |
| **Supabase 공식 지원** | ✅ 정식 지원 (IPv4 전용) |

### 권장 용도
- **Spring Boot + JPA/Hibernate 애플리케이션** ⭐
- 일반 웹서버 (IPv4 환경)
- 세션 기반 인증 시스템
- 연결 풀링이 필요한 중규모 애플리케이션
- 다중 트랜잭션 처리

### 제약사항
- 일부 PostgreSQL 시스템 수준 기능 제한 (Logical Replication, WAL)
- LISTEN/NOTIFY 사용 불가

---

## 전체 비교 표

| 구분 | Direct | Transaction Pooler | Session Pooler |
|------|--------|-------------------|-----------------|
| **커넥션 관리** | 직접 연결 (no pooling) | 트랜잭션 단위 | 세션 단위 |
| **IPv4 지원** | ❌ | ✅ | ✅ |
| **IPv6 지원** | ✅ | ✅ | ✅ |
| **세션 상태 유지** | ✅ 완전 | ❌ | ✅ 일부 |
| **ORM 호환성** | ✅ 최상 | ❌ | ✅ 우수 |
| **LISTEN/NOTIFY** | ✅ | ❌ | ❌ |
| **Replication** | ✅ | ❌ | ❌ |
| **서버리스 환경** | ❌ | ✅ 최적 | ⚠️ 제한적 |
| **웹서버 환경** | ✅ | ✅ | ✅ 최적 |
| **공식 지원** | ✅ | ✅ | ✅ |

---

## 프로젝트별 추천

### Spring Boot + JPA/Hibernate
**✅ Session Pooler 권장**

```yaml
이유:
  - IPv4 환경의 일반적인 클라우드 배포 지원
  - JPA/Hibernate 세션 상태 유지 필수
  - 다중 트랜잭션 처리 안정성
  - 세션 기반 인증 완벽 호환
```

### AWS Lambda / Serverless Functions
**✅ Transaction Pooler 권장**

```yaml
이유:
  - 단기 연결 워크로드 최적화
  - 높은 동시성 처리
  - 비용 효율적 커넥션 관리
  - 상태 유지 불필요
```

### 실시간 기능 필요 (PowerSync, CDC)
**✅ Direct Connection 권장**

```yaml
이유:
  - Logical Replication 필수
  - LISTEN/NOTIFY 지원
  - 모든 PostgreSQL 기능 활용
  - 주의: IPv6 환경 필수
```

---

## 마이그레이션 가이드

### 로컬 PostgreSQL → Supabase Session Pooler

#### application.yml (Spring Boot)
```yaml
# 로컬 (현재)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: ${DB_PASSWORD}

# Supabase로 변경
spring:
  datasource:
    url: jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?user=postgres.YOUR_PROJECT_REF
    username: postgres
    password: ${SUPABASE_DB_PASSWORD}
    hikari:
      maximum-pool-size: 10  # 세션 풀러 권장 설정
      minimum-idle: 2
      connection-timeout: 30000
```

#### 환경 변수 설정
```bash
# .env (배포용)
SUPABASE_DB_PASSWORD=your_supabase_password
DATABASE_URL=postgresql://user=postgres.PROJECT_REF password=PASSWORD host=aws-0-REGION.pooler.supabase.com port=5432 dbname=postgres
```

---

## 성능 고려사항

### 커넥션 풀 크기 설정

**Session Pooler + HikariCP 권장값**:
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
```

### 모니터링
Supabase 대시보드에서 커넥션 활용률을 정기적으로 확인하여 풀 크기 조정

---

## 참고 자료
- [Supabase 공식 문서 - Connection Strings](https://supabase.com/docs/guides/database/connecting-to-postgres)
- [Supabase Database - Pooling & Connection Limits](https://supabase.com/docs/guides/database/connecting-to-postgres#connection-pooling)
- [pgBouncer 공식 문서](https://www.pgbouncer.org/)
