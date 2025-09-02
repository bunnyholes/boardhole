# Exception Classes PMD Violations Fix Plan

## Identified PMD Violations

### 1. CommentRequired - Missing Javadoc for fields in GlobalExceptionHandler
- Multiple constants lack field comments
- Need to add /** */ documentation for all constant fields

### 2. CommentSize - Comments too large in all exception classes
- Class-level Javadoc comments exceed PMD size limits
- Method-level Javadoc comments are too verbose

### 3. TooManyMethods - GlobalExceptionHandler has too many methods
- Cannot reduce methods as each handles specific exception type
- Will need to suppress or reorganize

### 4. MethodArgumentCouldBeFinal - Missing final keywords on parameters
- ValidationException constructors have non-final parameters
- GlobalExceptionHandler methods have non-final parameters

### 5. LocalVariableCouldBeFinal - Missing final keywords on local variables
- ValidationException methods have non-final local variables
- GlobalExceptionHandler methods have non-final local variables

## Files to Fix

1. **ConflictException.java** - CommentSize violation
2. **DuplicateEmailException.java** - CommentSize violation  
3. **DuplicateUsernameException.java** - CommentSize violation
4. **ResourceNotFoundException.java** - CommentSize violation
5. **UnauthorizedException.java** - CommentSize violation
6. **ValidationException.java** - CommentSize, MethodArgumentCouldBeFinal, LocalVariableCouldBeFinal violations
7. **GlobalExceptionHandler.java** - CommentRequired, TooManyMethods, MethodArgumentCouldBeFinal, LocalVariableCouldBeFinal violations

## Fix Strategy

1. **Reduce comment sizes** while maintaining essential information
2. **Add final keywords** to all method parameters and local variables
3. **Add field documentation** for constants in GlobalExceptionHandler
4. **Preserve exception handling logic** (CRITICAL requirement)
5. **Add proper error codes and messages** where missing