# Lucee Toolbox - Linting Rules Documentation

This document provides a comprehensive overview of all linting rules supported by the Lucee Toolbox.

## Implementation Status Summary

| Category | Implemented | Planned | Total |
|----------|-------------|---------|-------|
| **Whitespace Rules** | 2 | 4 | 6 |
| **Naming Convention Rules** | 7 | 0 | 7 |
| **Code Structure Rules** | 8 | 1 | 9 |
| **Best Practice Rules** | 0 | 8 | 8 |
| **Security Rules** | 0 | 5 | 5 |
| **CFLint Compatible Rules** | 0 | 13 | 13 |
| **Total** | **16** | **32** | **48** |

### Recently Implemented ✅
- Complete **Naming Convention Rules** suite (7 rules)
- Complete **Code Structure Rules** suite (8 of 9 rules)
- All component, function, variable, constant, file, interface, and abstract naming standards
- Code quality enforcement: curly braces, line lengths, init methods, return types, argument types, accessors
- Smart detection with CFML-specific exclusions (event handlers, built-in scopes, etc.)

### Testing the Implemented Rules

To test the currently implemented rules, you can use the provided test files:

```bash
# Test basic naming violations
java -jar target/lucee-toolbox-1.0.0.jar -i test-files/bad/userService.cfc -m lint

# Test comprehensive naming violations
java -jar target/lucee-toolbox-1.0.0.jar -i test-files/bad/bad_naming_example.cfc -m lint

# View detailed output in JSON format
java -jar target/lucee-toolbox-1.0.0.jar -i test-files/bad/ -m lint --format json
```

## Table of Contents

- [Currently Implemented Rules](#currently-implemented-rules)
- [Planned Rules by Category](#planned-rules-by-category)
  - [Naming Convention Rules](#naming-convention-rules)
  - [Whitespace & Formatting Rules](#whitespace--formatting-rules)
  - [Code Structure Rules](#code-structure-rules)
  - [Best Practice Rules](#best-practice-rules)
  - [Security Rules](#security-rules)
  - [CFLint Compatible Rules](#cflint-compatible-rules)

---

## Currently Implemented Rules

These rules are **actively implemented** and working in the current version:

### Whitespace Rules

| Rule ID | Severity | Category | Description | Configuration | Examples |
|---------|----------|----------|-------------|---------------|----------|
| `TRAILING_WHITESPACE` | Warning | Whitespace | Detects lines with trailing whitespace characters | `trimTrailingWhitespace: true` | ❌ `var x = 1;   ` ✅ `var x = 1;` |
| `EXCESSIVE_EMPTY_LINES` | Info | Whitespace | Flags consecutive empty lines that exceed the maximum allowed | `maxEmptyLines: 1` | ❌ 3+ empty lines ✅ 1 empty line |

### Naming Convention Rules

| Rule ID | Severity | Category | Description | Configuration | Examples |
|---------|----------|----------|-------------|---------------|----------|
| `COMPONENT_NAMING` | Warning | Naming | Component filenames and declarations must follow PascalCase | `componentCase: "PascalCase"` | ✅ `UserService.cfc` ❌ `userService.cfc` |
| `FUNCTION_NAMING` | Warning | Naming | Function names must follow camelCase (excludes event handlers) | `functionCase: "camelCase"` | ✅ `getUserData()` ❌ `GetUserData()` |
| `VARIABLE_NAMING` | Warning | Naming | Variable names and function arguments must follow camelCase | `variableCase: "camelCase"` | ✅ `userName` ❌ `user_name` |
| `CONSTANT_NAMING` | Warning | Naming | Constants (based on heuristics) should follow UPPER_CASE | `constantCase: "UPPER_CASE"` | ✅ `MAX_RESULTS` ❌ `maxResults` |
| `FILE_NAMING` | Warning | Naming | CFC files must be PascalCase, CFM files should be camelCase | `cfcFileCase: "PascalCase"`, `cfmFileCase: "camelCase"` | ✅ `UserService.cfc` ✅ `userHelper.cfm` |
| `INTERFACE_PREFIX` | Info | Naming | Interface components should start with specified prefix | `interfacePrefix: "I"` | ✅ `IUserService.cfc` ❌ `UserService.cfc` |
| `ABSTRACT_SUFFIX` | Info | Naming | Abstract classes should end with specified suffix | `abstractSuffix: "Abstract"` | ✅ `BaseServiceAbstract.cfc` |

---

## Planned Rules by Category

The following rules are defined in the configuration and planned for future implementation:

### Whitespace & Formatting Rules

| Rule ID | Severity | Description | Configuration | Example |
|---------|----------|-------------|---------------|---------|
| `INDENT_SIZE` | Warning | Enforces consistent indentation size | `indentSize: 4` | ✅ 4 spaces ❌ 2 spaces |
| `INDENT_TYPE` | Warning | Enforces spaces vs tabs for indentation | `indentType: "spaces"` | ✅ Spaces ❌ Tabs |
| `FINAL_NEWLINE` | Info | Files should end with a newline | `insertFinalNewline: true` | ✅ File ends with `\n` |
| `SPACE_AFTER_KEYWORDS` | Warning | Requires space after control flow keywords | `requireSpaceAfterKeywords: true` | ✅ `if (condition)` ❌ `if(condition)` |
| `SPACE_AROUND_OPERATORS` | Warning | Requires spaces around operators | `requireSpaceAroundOperators: true` | ✅ `a = b + c` ❌ `a=b+c` |
| `NO_SPACE_IN_PARENTHESES` | Warning | No spaces inside parentheses | `noSpaceInParentheses: true` | ✅ `func(arg)` ❌ `func( arg )` |

### Code Structure Rules

| Rule ID | Severity | Description | Configuration | Example | Status |
|---------|----------|-------------|---------------|---------|--------|
| `REQUIRE_CURLY_BRACES` | Warning | All control structures must use curly braces | `requireCurlyBraces: true` | ✅ `if (x) { ... }` ❌ `if (x) doSomething();` | ✅ Implemented |
| `CURLY_BRACE_STYLE` | Info | Enforces curly brace placement style | `curlyBraceStyle: "same-line"` | ✅ `if (x) {` ❌ `if (x)\n{` | ✅ Implemented |
| `MAX_FUNCTION_LENGTH` | Warning | Functions should not exceed maximum line count | `maxFunctionLength: 50` | ❌ Function with 60 lines | 🚧 In Progress |
| `MAX_FILE_LENGTH` | Warning | Files should not exceed maximum line count | `maxFileLength: 1000` | ❌ File with 1200 lines | ✅ Implemented |
| `MAX_LINE_LENGTH` | Warning | Lines should not exceed maximum character count | `maxLineLength: 120` | ❌ Line with 140 characters | ✅ Implemented |
| `REQUIRE_INIT` | Warning | Components should have an init() method | `requireInit: true` | ✅ `function init()` in component | ✅ Implemented |
| `REQUIRE_RETURN_TYPES` | Warning | Functions should specify return types | `requireReturnTypes: true` | ✅ `string function getName()` | ✅ Implemented |
| `REQUIRE_ARGUMENT_TYPES` | Warning | Function arguments should specify types | `requireArgumentTypes: true` | ✅ `function test(string name)` | ✅ Implemented |
| `USE_ACCESSORS` | Info | Components with properties should use accessors | `useAccessors: true` | ✅ `accessors="true"` attribute | ✅ Implemented |

### Best Practice Rules

| Rule ID | Severity | Description | Configuration | Example |
|---------|----------|-------------|---------------|---------|
| `USE_VAR_SCOPING` | Warning | Variables should be properly scoped with var | `useVarScoping: true` | ✅ `var result = query;` ❌ `result = query;` |
| `USE_NAMED_ARGUMENTS` | Info | Prefer named arguments for clarity | `useNamedArguments: true` | ✅ `func(name="John")` ❌ `func("John")` |
| `PREFER_DOUBLE_QUOTES` | Info | Prefer double quotes over single quotes | `preferDoubleQuotes: true` | ✅ `"string"` ❌ `'string'` |
| `AVOID_EVALUATE` | Warning | Avoid using evaluate() function | `avoidEvaluate: true` | ❌ `evaluate("expression")` |
| `AVOID_IIF` | Warning | Avoid using iif() function | `avoidIif: true` | ❌ `iif(condition, "true", "false")` |
| `REQUIRE_CFPARAM` | Warning | Use cfparam for uninitialized variables | `requireCfparam: true` | ✅ `<cfparam name="variables.x" default="">` |
| `USE_CFLOCK` | Warning | Use cflock for shared scope access | `useCflock: true` | ✅ `<cflock scope="session">` |
| `PREFER_NEW_OVER_CREATEOBJECT` | Info | Prefer new operator over createObject | `preferNewOverCreateObject: true` | ✅ `new Component()` ❌ `createObject("component", "Component")` |

### Security Rules

| Rule ID | Severity | Description | Configuration | Example |
|---------|----------|-------------|---------------|---------|
| `SQL_INJECTION_CHECK` | Error | Detect potential SQL injection vulnerabilities | `checkSqlInjection: true` | ❌ `"SELECT * FROM users WHERE id = #arguments.id#"` |
| `XSS_CHECK` | Error | Detect potential XSS vulnerabilities | `checkXss: true` | ❌ Unescaped user input in HTML output |
| `DIRECTORY_TRAVERSAL_CHECK` | Error | Detect potential directory traversal attacks | `checkDirectoryTraversal: true` | ❌ `<cffile action="read" file="#arguments.path#">` |
| `REQUIRE_PARAM_TYPES` | Warning | Function parameters should specify types for security | `requireParamTypes: true` | ✅ `required string userId` |
| `CHECK_UNSCOPED` | Warning | Variables should be properly scoped | `checkUnscoped: true` | ❌ Accessing variables without scope prefix |

### CFLint Compatible Rules

These rules provide compatibility with CFLint tool:

| Rule ID | Severity | Description | Status |
|---------|----------|-------------|--------|
| `ARG_VAR_CONFLICT` | Warning | Argument and variable name conflicts | Planned |
| `ARG_VAR_MIXED` | Warning | Mixed argument and variable usage | Planned |
| `AVOID_USING_CFEXECUTE_TAG` | Warning | Avoid using cfexecute tag | Planned |
| `AVOID_USING_CFSETTING_DEBUG` | Error | Avoid debug settings in production | Planned |
| `AVOID_USING_DEBUG_ATTR` | Error | Avoid debug attributes | Planned |
| `AVOID_USING_ISDEFINED` | Warning | Prefer structKeyExists over isDefined | Planned |
| `CFQUERYPARAM_REQ` | Error | Require cfqueryparam for dynamic SQL | Planned |
| `GLOBAL_VAR` | Warning | Avoid global variables | Planned |
| `NESTED_CFOUTPUT` | Error | Avoid nested cfoutput tags | Planned |
| `OUTPUT_ATTR` | Warning | Check output attribute usage | Planned |
| `QUERYNEW_DATATYPE` | Warning | Specify data types in queryNew | Planned |
| `UNUSED_LOCAL_VARIABLE` | Warning | Detect unused local variables | Planned |
| `VAR_INVALID_NAME` | Warning | Invalid variable names | Planned |
| `VAR_IS_TEMPORARY` | Warning | Temporary variable naming | Planned |

---

## Rule Configuration

### Global Configuration

Rules can be configured globally in `lucee-toolbox.json`:

```json
{
  "linting": {
    "enabled": true,
    "severity": {
      "error": ["SECURITY_VIOLATION", "SYNTAX_ERROR"],
      "warning": ["NAMING_CONVENTION", "BEST_PRACTICE"],
      "info": ["STYLE_GUIDE", "DOCUMENTATION"]
    }
  }
}
```

### Rule-Specific Configuration

Individual rules can be configured with specific parameters:

```json
{
  "linting": {
    "rules": {
      "whitespace": {
        "trimTrailingWhitespace": true,
        "maxEmptyLines": 1,
        "indentSize": 4
      },
      "naming": {
        "componentCase": "PascalCase",
        "functionCase": "camelCase"
      }
    }
  }
}
```

### Command Line Override

Rules can be enabled/disabled via command line:

```bash
# Enable specific rules
lucee-toolbox -i src/ --rules +TRAILING_WHITESPACE,+COMPONENT_NAMING

# Disable specific rules
lucee-toolbox -i src/ --rules -EXCESSIVE_EMPTY_LINES

# Use predefined rule sets
lucee-toolbox -i src/ --rule-set cflint
lucee-toolbox -i src/ --rule-set minimal
lucee-toolbox -i src/ --rule-set strict
```

---

## Rule Severity Levels

| Severity | Description | Exit Code Impact |
|----------|-------------|------------------|
| **Error** | Critical issues that must be fixed | Causes non-zero exit code |
| **Warning** | Important issues that should be addressed | Causes non-zero exit code (default) |
| **Info** | Style suggestions and minor improvements | Does not affect exit code |

---

## Adding Custom Rules

Custom rules can be added by:

1. Implementing the `LintingRule` interface
2. Adding rule configuration to `lucee-toolbox.json`
3. Registering the rule in `LintingRuleEngine`

See the [Development Guide](docs/DEVELOPMENT.md) for detailed instructions on creating custom rules.

---

## Rule Status Legend

- ✅ **Implemented** - Rule is active and working
- 🚧 **In Progress** - Rule is being developed
- 📋 **Planned** - Rule is planned for future implementation
- ❌ **Disabled** - Rule is available but disabled by default
