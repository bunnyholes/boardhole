# Changelog

All notable changes to the Board-Hole project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- 문서화 프로젝트 진행 중
- Standard documentation structure implementation

## [1.0.0] - 2024-12-22

### Added
- 🔐 **JWT Authentication System**
  - Spring Security + JWT token-based authentication
  - Role-based access control (USER, ADMIN)
  - Secure password hashing with BCrypt
  - Authentication endpoints (`/api/auth/login`, `/api/auth/me`)

- 🏗️ **CQRS Pattern Implementation**
  - Command/Query separation for better maintainability
  - Event-driven architecture for async operations
  - Structured application layer with Commands, Queries, Results, Events
  - MapStruct integration for object mapping

- 🌐 **Internationalization (i18n)**
  - Korean and English message support
  - `?lang=en` parameter for language switching
  - Localized error messages and validation
  - `MessageUtils` for centralized message handling

- ⚡ **Async Event Processing**
  - View count increment via async events
  - `ViewedEvent` and `ViewedEventListener` pattern
  - Non-blocking read operations with background write operations
  - Thread pool configuration for async tasks

- 📊 **RESTful API Design**
  - Comprehensive CRUD operations for Boards and Users
  - Pagination support with Spring Data Pageable
  - Search functionality for boards
  - Proper HTTP status codes and response headers

- 🧪 **Testing Infrastructure**
  - Testcontainers for integration testing
  - MockMvc for controller testing
  - JUnit 5 and AssertJ for modern testing
  - Test profiles and configurations

- 📖 **API Documentation**
  - Swagger UI integration (`/swagger-ui.html`)
  - OpenAPI 3.0 specification
  - Interactive API explorer
  - Comprehensive endpoint documentation

### Security Enhancements
- 🛡️ **Spring Security Configuration**
  - Method-level security with `@PreAuthorize`
  - Resource-based access control
  - ProblemDetails for standardized error responses
  - CORS configuration for cross-origin requests

- 🔍 **Input Validation**
  - Bean Validation with custom error messages
  - Request size limits and content type validation
  - SQL injection prevention through JPA parameterized queries
  - XSS prevention through proper output encoding

### Infrastructure
- 🐳 **Docker Support**
  - Docker Compose for local development
  - MySQL database containerization
  - Environment-specific configurations (dev, prod, test)

- 📊 **Logging and Monitoring**
  - Structured logging with Logback
  - Request/response logging with filtering
  - AOP-based method execution logging
  - Sensitive data masking in logs

### Database
- 🗄️ **Data Management**
  - MySQL for production environment
  - H2 for development and testing
  - JPA/Hibernate with optimized configurations
  - Database initialization and migration support

## [0.9.0] - 2024-11-15

### Added
- Basic Spring Boot application setup
- Initial project structure with layered architecture
- Basic CRUD operations for users and boards
- Simple authentication with Spring Security sessions

### Changed
- Migrated from session-based to JWT authentication
- Improved error handling and validation

### Removed
- Session-based authentication in favor of JWT
- Custom pagination implementation (replaced with Spring Data Page)

## [0.8.0] - 2024-10-20

### Added
- Initial project setup with Spring Boot 3.5.4
- Basic MVC pattern implementation
- User and Board entities
- Simple repository layer with Spring Data JPA

### Infrastructure
- Gradle build configuration
- Basic application properties
- Initial test setup

## Migration Notes

### From 0.x to 1.0

**Breaking Changes**:
- Authentication method changed from session to JWT
- API response format standardized to ProblemDetails for errors
- Package structure reorganized to follow CQRS pattern

**Migration Steps**:
1. Update client applications to use JWT tokens
2. Replace session-based authentication calls
3. Update error handling to use new ProblemDetails format
4. Verify internationalization parameter usage (`?lang=en`)

**Database Changes**:
- No breaking schema changes
- Existing data is preserved
- Additional indices added for performance

## Security Updates

### Version 1.0.0 Security Improvements
- **CVE Fixes**: No known CVEs at release
- **Security Enhancements**: 
  - Stronger password hashing (BCrypt with cost 12)
  - Improved JWT token security
  - Enhanced input validation
  - Secure headers configuration

## Performance Improvements

### Version 1.0.0 Performance Enhancements
- **Async Operations**: View count updates don't block read operations
- **Query Optimization**: Paginated queries with proper indexing
- **Caching**: Message source caching for i18n
- **Connection Pooling**: Optimized database connection management

---

## Future Roadmap

### Planned for v1.1.0
- 📧 **Email Notifications**: Async email sending for important events
- 🔍 **Advanced Search**: Full-text search with Elasticsearch integration
- 📱 **Mobile API**: Mobile-optimized endpoints
- 🎨 **File Upload**: Image and file attachment support

### Planned for v1.2.0
- 🔔 **Real-time Updates**: WebSocket integration for live notifications
- 📊 **Analytics**: User activity tracking and analytics
- 🔐 **2FA Support**: Two-factor authentication implementation
- 🌍 **Additional Languages**: More language support beyond Korean/English

### Planned for v2.0.0
- 🏗️ **Microservices Migration**: Split into multiple services
- 📦 **Container Orchestration**: Kubernetes deployment support
- 🔄 **Event Sourcing**: Advanced event-driven patterns
- 🎯 **GraphQL API**: Alternative API interface

---

**Note**: This changelog is maintained manually. For detailed commit history, see `git log --oneline`.