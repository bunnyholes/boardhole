# Constants PMD Fixes TODO

## Major Issues Found

### ApiPaths.java
- ✅ CommentSize: Comment is too large - Fixed with comprehensive Javadoc
- ❌ DataClass: Suspected to be Data Class - This is expected for constants utility class
- ❌ LongVariable: AUTH_PUBLIC_ACCESS, USER_EMAIL_VERIFICATION - Need evaluation
- ❌ CommentSize: Constructor comment too large - Need to reduce size

### ValidationConstants.java  
- ✅ CommentSize: Comment is too large - Fixed with detailed documentation
- ❌ DataClass: Suspected to be Data Class - This is expected for constants utility class
- ❌ LongVariable: Multiple long variable names - Need evaluation
- ❌ UseUnderscoresInNumericLiterals: 10000 should be 10_000
- ❌ CommentSize: PASSWORD_PATTERN comment too large - Need to reduce size

### LogConstants.java
- ❌ CommentSize: Comment is too large - Already has good docs, may need reduction
- ❌ DataClass: Suspected to be Data Class - This is expected for constants utility class  
- ❌ LongVariable: REQUEST_START_ICON - Need evaluation

### ErrorCode.java (enum)
- ❌ LongVariable: Multiple long variable names - Need evaluation

### PermissionType.java
- ❌ DataClass: Suspected to be Data Class - This is expected for constants utility class
- ❌ Missing UnsupportedOperationException in constructor - Need to fix

## Strategy

1. **DataClass violations**: These are expected for utility classes with only constants - suppress if needed
2. **LongVariable**: Evaluate if names can be shortened without losing clarity
3. **CommentSize**: Reduce comment sizes while preserving essential information
4. **UseUnderscoresInNumericLiterals**: Fix numeric literal formatting
5. **Missing UnsupportedOperationException**: Add to utility class constructors