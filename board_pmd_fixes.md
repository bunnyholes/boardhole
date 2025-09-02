# Board Domain PMD Fixes Task List

## Priority Files Analysis

### 1. BoardCommandService.java
**Violations Found:**
- ✅ Field comments: Already has field Javadoc
- ❌ Law of Demeter: Lines 71, 102, 118 (saved.getAuthor().getUsername())

### 2. Board.java  
**Violations Found:**
- ✅ Field comments: Already has field Javadoc
- ❌ ShortVariable: `id` → should be `boardId` 
- ✅ CommentSize: Already has proper class comments
- ❌ ImmutableField: `viewCount` field is not final (but correct for entity)

### 3. BoardController.java
**Violations Found:**
- ✅ Field comments: Already has field Javadoc
- ❌ CommentRequired: Missing method Javadoc for methods (lines 96, 131, 164, 213, 244)

### 4. BoardResponse.java  
**Violations Found:**
- ❌ ShortVariable: `id` → should be `boardId`

### 5. BoardWebMapper.java
**Violations Found:**  
- ❌ AvoidDuplicateLiterals: Multiple string literals like "authorId", "req.title"
- ❌ ShortVariable: `id` parameter → should be `boardId`

### 6. BoardRepository.java
**Violations Found:**
- ❌ AvoidDuplicateLiterals: Multiple "author" strings
- ✅ CommentSize: Already has proper comments

### 7. GetBoardQuery.java
**Violations Found:**
- ❌ ShortVariable: `id` → should be `boardId`  
- ❌ ShortMethodName: `of()` → should be `create()` or `createQuery()`
- ❌ CommentRequired: Missing method Javadoc
- ❌ MethodArgumentCouldBeFinal: `Long id` parameter should be final

### 8. BoardResult.java
**Violations Found:**
- ❌ ShortVariable: `id` → should be `boardId`

### 9. ViewedEvent.java  
**Violations Found:**
- ❌ ShortMethodName: `of()` → should be `create()`
- ❌ CommentRequired: Missing method Javadoc  
- ❌ MethodArgumentCouldBeFinal: Parameters should be final

### 10. ViewedEventListener.java
**Violations Found:**
- ❌ CommentRequired: Missing method Javadoc for `onViewed()`
- ❌ MethodArgumentCouldBeFinal: `ViewedEvent event` should be final
- ❌ GuardLogStatement: `log.debug()` needs guard

## Fix Strategy
1. Rename `id` to `boardId` consistently across all files
2. Add `final` to method parameters
3. Extract duplicate literals to constants  
4. Add method Javadoc where missing
5. Fix Law of Demeter violations
6. Add log guards
7. Rename short method names

## Files to Update (in dependency order)
1. BoardResult.java (used by others)
2. BoardResponse.java (DTO)  
3. GetBoardQuery.java (query object)
4. ViewedEvent.java (event object)
5. Board.java (domain entity - impacts others)
6. BoardWebMapper.java (uses DTOs)  
7. BoardRepository.java (used by services)
8. BoardCommandService.java (uses repository)
9. ViewedEventListener.java (uses command service)  
10. BoardController.java (uses all services)