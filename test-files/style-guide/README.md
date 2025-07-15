# Lucee Code Style Examples

This directory contains examples of good and bad Lucee/CFML code based on the CFML Standards and Best Practices guide.

## Directory Structure

```
style-guide/
├── good/           # Examples following the style guide
└── bad/            # Examples violating the style guide
```

## Examples Overview

### Good Examples (`examples/good/`)

1. **UserService.cfc** - A well-structured CFC demonstrating:
   - Proper naming conventions (PascalCase for CFC, camelCase for methods)
   - Correct use of accessors and encapsulation
   - Proper constructor with `return this`
   - Var scoping with `localMode="true"`
   - Named arguments and parameter validation
   - Proper SQL parameter binding
   - Method chaining with correct formatting
   - Static constants properly defined

2. **userProfile.cfm** - A CFM file showing:
   - Proper use of `cfparam` for parameter validation
   - Correct tag casing (lowercase)
   - Named arguments in function calls
   - Proper error handling with try/catch
   - Good whitespace and formatting
   - Appropriate use of `cfoutput` blocks

3. **apiClient.cfc** - HTTP handling demonstrating:
   - Proper `cfhttp` usage with `result` attribute
   - Error handling without `throwOnError=true`
   - Status code checking with `status_code`
   - Proper JSON handling with error checking
   - Encapsulation of HTTP logic

4. **batchProcessor.cfc** - Thread usage showing:
   - Proper thread management and cleanup
   - Correct locking for shared resources
   - Timeout handling for threads
   - Error handling within threads
   - Double-conditional checks for race conditions

### Bad Examples (`examples/bad/`)

1. **userservice.cfc** - Demonstrates common violations:
   - Incorrect file naming (should be `UserService.cfc`)
   - Poor constant naming and placement
   - No accessors, exposing internal variables
   - Missing constructor `return this`
   - No var scoping (thread safety issues)
   - SQL injection vulnerabilities
   - Direct session/application scope access
   - Poor whitespace and formatting
   - No curly braces for control structures
   - Race conditions on shared resources

2. **userProfile.cfm** - Shows bad CFM practices:
   - Uppercase tag names
   - No `cfparam` usage
   - SQL injection vulnerabilities
   - No error handling
   - Poor formatting and inconsistent casing
   - Direct session scope access
   - No validation of user input

3. **apiClient.cfc** - HTTP handling anti-patterns:
   - Using `throwOnError=true` with `cfhttp`
   - No proper error handling
   - Poor encapsulation (exposing internal data)
   - Thread safety issues
   - Using deprecated functions like `evaluate()` and `iif()`
   - Race conditions on shared resources

## Key Violations Demonstrated

### Naming Conventions
- **Bad**: `userservice.cfc`, `getUserFromDB()`, `MAX_users`
- **Good**: `UserService.cfc`, `getUserFromDatabase()`, `MAX_USERS`

### Whitespace and Formatting
- **Bad**: `if(condition){`, `function test(arg1,arg2,arg3){`
- **Good**: `if (condition) {`, `function test(arg1, arg2, arg3) {`

### Security Issues
- **Bad**: Direct string concatenation in SQL, no parameter binding
- **Good**: Using `queryExecute()` with parameterized queries

### Error Handling
- **Bad**: `cfhttp` with `throwOnError=true`, no try/catch blocks
- **Good**: Checking `status_code`, proper exception handling

### Thread Safety
- **Bad**: No var scoping, direct manipulation of shared scopes
- **Good**: `localMode="true"`, proper locking, double-conditional checks

### Encapsulation
- **Bad**: Exposing internal variables, direct scope access
- **Good**: Using accessors, private methods, proper data hiding

## Usage

These examples are designed to be used as:
1. **Training material** - Show developers what to do and what to avoid
2. **Code review reference** - Compare against existing code
3. **Linting configuration** - Use as test cases for automated tools
4. **Documentation** - Concrete examples of the style guide rules

## Best Practices Highlighted

1. **Always use proper naming conventions** for files, functions, and variables
2. **Implement proper error handling** instead of relying on exceptions
3. **Use parameterized queries** to prevent SQL injection
4. **Employ var scoping** to ensure thread safety
5. **Apply proper locking** for shared resources
6. **Follow whitespace rules** for consistency and readability
7. **Use named arguments** for better code clarity
8. **Implement proper encapsulation** to hide internal details

## Running the Examples

To test these examples:
1. Place the good examples in your development environment
2. Run them to see proper behavior
3. Compare with the bad examples to understand the differences
4. Use linting tools to catch violations automatically

