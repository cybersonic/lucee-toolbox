# Lucee Toolbox - Development Guide

> Complete development documentation for building, testing, and extending the Lucee Toolbox

## 📋 Table of Contents

1. [Getting Started](#getting-started)
2. [Building the Project](#building-the-project)
3. [Running Tests](#running-tests)
4. [Project Architecture](#project-architecture)
5. [Extending Parsers](#extending-parsers)
6. [Adding New Linting Rules](#adding-new-linting-rules)
7. [Adding New Formatting Rules](#adding-new-formatting-rules)
8. [Development Workflow](#development-workflow)
9. [Contributing Guidelines](#contributing-guidelines)
10. [Troubleshooting](#troubleshooting)

---

## 🚀 Getting Started

### Prerequisites

- **Java 11+** - Required for development
- **Maven 3.8+** - For building and dependency management
- **GraalVM 21+** - Optional, for native image builds
- **Git** - For version control

### Development Setup

```bash
# Clone the repository
git clone https://github.com/cybersonic/lucee-toolbox.git
cd lucee-toolbox

# Install GraalVM (optional, for native builds)
sdk install java 21.0.1-graal
sdk use java 21.0.1-graal
gu install native-image

# Build the project
./build.sh

# Run quick test
java -jar target/lucee-toolbox-1.0.0.jar --version
```

---

## 🏗️ Building the Project

### Standard Build Options

```bash
# Standard JAR build
./build.sh

# Clean build with verbose output
./build.sh -c -v

# Build with test coverage profiling
./build.sh -p

# Skip tests (faster build)
./build.sh -s

# Package only (no compilation)
./build.sh --package-only
```

### Native Image Build

```bash
# Build native executable
./build.sh --native-image

# Or use the specialized script
./build-native.sh
```

### Build Script Options

| Option | Description |
|--------|-------------|
| `-c, --clean` | Clean before building |
| `-s, --skip-tests` | Skip running tests |
| `-v, --verbose` | Verbose Maven output |
| `-p, --profile` | Enable profiling with JaCoCo |
| `--package-only` | Only package, don't compile |
| `--native-image` | Create native image executable |
| `-h, --help` | Show help message |

### Manual Maven Commands

```bash
# Compile only
mvn compile

# Run tests
mvn test

# Package with tests
mvn package

# Package skipping tests
mvn package -DskipTests

# Generate test coverage report
mvn clean test jacoco:report
```

---

## 🧪 Running Tests

### Test Structure

```
src/test/java/
├── org/lucee/toolbox/
│   ├── core/
│   │   ├── util/EncodingDetectorTest.java
│   │   └── ...
│   ├── linting/
│   │   ├── BadUserServiceTest.java
│   │   └── ...
│   └── repl/
│       ├── ScriptEngineTest.java
│       └── ...
```

### Running Tests

```bash
# Run all tests
./build.sh

# Run tests with coverage
./build.sh -p

# Run specific test class
mvn test -Dtest=BadUserServiceTest

# Run tests matching pattern
mvn test -Dtest=*NamingTest

# Run integration tests only
mvn test -Dtest=**/*IntegrationTest

# Run tests with verbose output
mvn test -X
```

### Test Coverage

Coverage reports are generated in `target/site/jacoco/`:

```bash
# Generate coverage report
mvn jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Data

The `test-files/` directory contains sample CFML files for testing:

```bash
# Test with sample files
java -jar target/lucee-toolbox-1.0.0.jar -i test-files/bad/userService.cfc -m lint
java -jar target/lucee-toolbox-1.0.0.jar -i test-files/bad/bad_naming_example.cfc -m lint
```

---

## 🏛️ Project Architecture

### Core Components

```
src/main/java/org/lucee/toolbox/
├── LuceeToolbox.java           # Main entry point
├── cli/                        # Command line interface
├── core/
│   ├── config/                 # Configuration management
│   ├── engine/                 # Linting & formatting engines
│   ├── model/                  # Data models
│   ├── parser/                 # Parser interfaces & implementations
│   ├── rules/                  # Linting rules
│   └── util/                   # Utility classes
├── output/                     # Output formatters
└── repl/                       # CFML REPL implementation
```

### Key Interfaces

1. **CfmlParser** - Base interface for all parsers
2. **LintingRule** - Interface for linting rules
3. **OutputFormatter** - Interface for output formats

### Data Flow

```
Input Files → Parser → AST → Rules Engine → Violations → Output Formatter → Results
```

---

## 🔧 Extending Parsers

### Parser Architecture

The toolbox supports multiple parsers with automatic fallback:

1. **BoxLang ANTLR Parser** - Most comprehensive, handles complex syntax
2. **Lucee Native Parser** - Fast, uses Lucee's internal parser
3. **Regex Parser** - Fallback for simple pattern matching

### Adding a New Parser

#### 1. Implement the CfmlParser Interface

```java
package org.lucee.toolbox.core.parser.impl;

import org.lucee.toolbox.core.parser.CfmlParser;
import org.lucee.toolbox.core.parser.ParseResult;
import org.lucee.toolbox.core.parser.ParseException;

public class MyCustomParser implements CfmlParser {
    
    @Override
    public ParseResult parse(String content, String filePath) throws ParseException {
        // Your parsing logic here
        ParseResult result = new ParseResult();
        result.setContent(content);
        result.setFilePath(filePath);
        result.setParserType("custom");
        
        // Parse and populate result
        // - Extract functions, components, variables
        // - Build AST representation
        // - Handle parsing errors
        
        return result;
    }
    
    @Override
    public String getParserType() {
        return "custom";
    }
    
    @Override
    public boolean canParse(String content) {
        // Logic to determine if this parser can handle the content
        // Return true if your parser can process this content
        return content.contains("component") || content.contains("function");
    }
}
```

#### 2. Register the Parser in ParserFactory

```java
// In src/main/java/org/lucee/toolbox/core/parser/ParserFactory.java
public class ParserFactory {
    
    public static CfmlParser createParser(String parserType) {
        switch (parserType.toLowerCase()) {
            case "boxlang":
                return new BoxLangParser();
            case "lucee":
                return new LuceeParser();
            case "regex":
                return new RegexParser();
            case "custom":  // Add your parser here
                return new MyCustomParser();
            case "auto":
            default:
                return createAutoParser();
        }
    }
    
    private static CfmlParser createAutoParser() {
        // Add your parser to the auto-detection chain
        List<CfmlParser> parsers = Arrays.asList(
            new BoxLangParser(),
            new LuceeParser(),
            new MyCustomParser(),  // Add here
            new RegexParser()
        );
        
        return new AutoParser(parsers);
    }
}
```

#### 3. Add Configuration Support

```java
// In lucee-toolbox.json
{
  "parsing": {
    "defaultParser": "custom",
    "parserSettings": {
      "custom": {
        "enableAdvancedFeatures": true,
        "strictMode": false
      }
    }
  }
}
```

#### 4. Add Tests

```java
// In src/test/java/org/lucee/toolbox/core/parser/MyCustomParserTest.java
@Test
public void testParseComponent() throws ParseException {
    MyCustomParser parser = new MyCustomParser();
    String content = "component { function test() {} }";
    
    ParseResult result = parser.parse(content, "test.cfc");
    
    assertEquals("custom", result.getParserType());
    assertNotNull(result.getComponent());
    assertEquals(1, result.getFunctions().size());
}
```

### Parser Best Practices

1. **Error Handling** - Always handle parsing errors gracefully
2. **Performance** - Consider caching for large files
3. **Completeness** - Extract all relevant AST information
4. **Compatibility** - Test with various CFML syntax variations

---

## 📏 Adding New Linting Rules

### Rule Architecture

Linting rules implement the `LintingRule` interface and are organized by category:

```
src/main/java/org/lucee/toolbox/core/rules/
├── LintingRule.java            # Base interface
├── LintingRuleEngine.java      # Rule execution engine
├── naming/                     # Naming convention rules
├── structure/                  # Code structure rules
├── whitespace/                 # Whitespace rules
├── security/                   # Security rules
└── bestpractices/              # Best practice rules
```

### Creating a New Rule

#### 1. Implement the LintingRule Interface

```java
package org.lucee.toolbox.core.rules.bestpractices;

import org.lucee.toolbox.core.model.LintingViolation;
import org.lucee.toolbox.core.model.Severity;
import org.lucee.toolbox.core.parser.ParseResult;
import org.lucee.toolbox.core.rules.LintingRule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AvoidEvaluateRule implements LintingRule {
    
    private static final String RULE_ID = "AVOID_EVALUATE";
    private static final Pattern EVALUATE_PATTERN = 
        Pattern.compile("\\bevaluate\\s*\\(", Pattern.CASE_INSENSITIVE);
    
    @Override
    public String getRuleId() {
        return RULE_ID;
    }
    
    @Override
    public List<LintingViolation> analyze(ParseResult parseResult) {
        List<LintingViolation> violations = new ArrayList<>();
        String content = parseResult.getContent();
        String[] lines = content.split("\\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            if (EVALUATE_PATTERN.matcher(line).find()) {
                violations.add(new LintingViolation(
                    RULE_ID,
                    "Avoid using evaluate() function - use direct variable access instead",
                    Severity.WARNING,
                    parseResult.getFilePath(),
                    i + 1,
                    findColumn(line, "evaluate"),
                    line.trim()
                ));
            }
        }
        
        return violations;
    }
    
    private int findColumn(String line, String pattern) {
        return line.toLowerCase().indexOf(pattern.toLowerCase()) + 1;
    }
}
```

#### 2. Register the Rule

```java
// In src/main/java/org/lucee/toolbox/core/rules/LintingRuleEngine.java
public class LintingRuleEngine {
    
    public void initializeRules() {
        // Existing rules...
        
        // Best practices rules
        rules.add(new AvoidEvaluateRule());
        rules.add(new AvoidIifRule());
        rules.add(new UseVarScopingRule());
        
        // Your new rule here
        rules.add(new MyCustomRule());
    }
}
```

#### 3. Add Configuration

```java
// In lucee-toolbox.json
{
  "linting": {
    "rules": {
      "bestPractices": {
        "avoidEvaluate": true,
        "avoidIif": true,
        "useVarScoping": true
      }
    }
  }
}
```

#### 4. Add Tests

```java
// In src/test/java/org/lucee/toolbox/core/rules/bestpractices/AvoidEvaluateRuleTest.java
@Test
public void testDetectsEvaluateUsage() {
    AvoidEvaluateRule rule = new AvoidEvaluateRule();
    String content = "var result = evaluate('variables.myVar');";
    
    ParseResult parseResult = createParseResult(content);
    List<LintingViolation> violations = rule.analyze(parseResult);
    
    assertEquals(1, violations.size());
    assertEquals("AVOID_EVALUATE", violations.get(0).getRuleId());
    assertEquals(Severity.WARNING, violations.get(0).getSeverity());
}
```

### Rule Categories

#### Naming Rules
- Component naming conventions
- Function naming conventions
- Variable naming conventions
- File naming conventions

#### Structure Rules
- Code organization
- Function length limits
- Line length limits
- Curly brace requirements

#### Security Rules
- SQL injection detection
- XSS vulnerability detection
- Path traversal detection
- Parameter validation

#### Best Practice Rules
- Var scoping usage
- Preferred syntax patterns
- Performance optimizations
- Code clarity improvements

### Rule Configuration

Rules can be configured at multiple levels:

```json
{
  "linting": {
    "enabled": true,
    "rules": {
      "AVOID_EVALUATE": {
        "enabled": true,
        "severity": "warning",
        "exceptions": ["test/**/*"]
      }
    },
    "rulesets": {
      "strict": {
        "AVOID_EVALUATE": "error"
      }
    }
  }
}
```

---

## 🎨 Adding New Formatting Rules

### Formatting Architecture

Formatting rules work on the parsed AST and apply transformations:

```
src/main/java/org/lucee/toolbox/core/formatting/
├── FormattingRule.java         # Base interface
├── FormattingEngine.java       # Rule execution engine
├── whitespace/                 # Whitespace formatting
├── indentation/               # Indentation rules
├── braces/                    # Brace placement rules
└── structure/                 # Code structure formatting
```

### Creating a Formatting Rule

#### 1. Implement the FormattingRule Interface

```java
package org.lucee.toolbox.core.formatting.whitespace;

import org.lucee.toolbox.core.formatting.FormattingRule;
import org.lucee.toolbox.core.model.FormattingChange;
import org.lucee.toolbox.core.parser.ParseResult;

import java.util.ArrayList;
import java.util.List;

public class SpaceAroundOperatorsRule implements FormattingRule {
    
    private static final String RULE_ID = "SPACE_AROUND_OPERATORS";
    
    @Override
    public String getRuleId() {
        return RULE_ID;
    }
    
    @Override
    public List<FormattingChange> format(ParseResult parseResult) {
        List<FormattingChange> changes = new ArrayList<>();
        String content = parseResult.getContent();
        String[] lines = content.split("\\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String formattedLine = addSpaceAroundOperators(line);
            
            if (!line.equals(formattedLine)) {
                changes.add(new FormattingChange(
                    RULE_ID,
                    "Add spaces around operators",
                    parseResult.getFilePath(),
                    i + 1,
                    line,
                    formattedLine
                ));
            }
        }
        
        return changes;
    }
    
    private String addSpaceAroundOperators(String line) {
        // Add spaces around = operator
        line = line.replaceAll("([^=])=([^=])", "$1 = $2");
        // Add spaces around + operator
        line = line.replaceAll("([^+])\\+([^+])", "$1 + $2");
        // Add spaces around - operator
        line = line.replaceAll("([^-])-([^-])", "$1 - $2");
        // Add more operators as needed
        
        return line;
    }
}
```

#### 2. Register the Formatting Rule

```java
// In src/main/java/org/lucee/toolbox/core/engine/FormattingEngine.java
public class FormattingEngine {
    
    public void initializeRules() {
        // Existing rules...
        
        // Whitespace rules
        rules.add(new SpaceAroundOperatorsRule());
        rules.add(new SpaceAfterKeywordsRule());
        rules.add(new NoSpaceInParenthesesRule());
        
        // Your new rule here
        rules.add(new MyCustomFormattingRule());
    }
}
```

#### 3. Add Configuration

```json
{
  "formatting": {
    "enabled": true,
    "rules": {
      "whitespace": {
        "spaceAroundOperators": true,
        "spaceAfterKeywords": true,
        "noSpaceInParentheses": true
      }
    }
  }
}
```

---

## 🔄 Development Workflow

### Code Style

Follow these conventions:

1. **Java Code Style** - Use standard Java conventions
2. **Naming** - Use descriptive names for classes and methods
3. **Comments** - Document public APIs and complex logic
4. **Tests** - Write tests for all new functionality

### Development Process

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/my-new-feature`
3. **Implement** your changes
4. **Test** thoroughly
5. **Document** new features
6. **Submit** a pull request

### Testing Your Changes

```bash
# Run all tests
./build.sh

# Test specific functionality
java -jar target/lucee-toolbox-1.0.0.jar -i test-files/bad/ -m lint

# Test with different parsers
java -jar target/lucee-toolbox-1.0.0.jar -i test.cfc -p boxlang -m lint
java -jar target/lucee-toolbox-1.0.0.jar -i test.cfc -p lucee -m lint
java -jar target/lucee-toolbox-1.0.0.jar -i test.cfc -p regex -m lint

# Test output formats
java -jar target/lucee-toolbox-1.0.0.jar -i test.cfc -f json -m lint
java -jar target/lucee-toolbox-1.0.0.jar -i test.cfc -f html -m lint

# Test stdin functionality
echo 'dump(server);' | java -jar target/lucee-toolbox-1.0.0.jar
echo 'function test(){return "hello";}' | java -jar target/lucee-toolbox-1.0.0.jar -m format
echo 'component { function test() {} }' | java -jar target/lucee-toolbox-1.0.0.jar -f json
cat test.cfc | java -jar target/lucee-toolbox-1.0.0.jar --ignore-violations
```

---

## 🤝 Contributing Guidelines

### Before Contributing

1. Check existing issues and pull requests
2. Discuss major changes in an issue first
3. Ensure your code follows the project style
4. Write comprehensive tests

### Pull Request Process

1. Update documentation for new features
2. Add tests for new functionality
3. Ensure all tests pass
4. Update CHANGELOG.md
5. Request review from maintainers

### Code Review Checklist

- [ ] Code follows project conventions
- [ ] Tests are comprehensive and passing
- [ ] Documentation is updated
- [ ] Performance impact is considered
- [ ] Security implications are addressed

---

## 🔧 Troubleshooting

### Common Build Issues

#### Maven Dependencies
```bash
# Clean and rebuild
mvn clean install

# Update dependencies
mvn dependency:resolve

# Check for conflicts
mvn dependency:tree
```

#### Native Image Issues
```bash
# Ensure GraalVM is installed
java -version

# Check native-image availability
native-image --version

# Build with verbose output
./build.sh --native-image -v
```

### Common Development Issues

#### Parser Not Working
- Check if parser is registered in `ParserFactory`
- Verify `canParse()` method logic
- Test with simple CFML files first

#### Rules Not Executing
- Ensure rule is registered in `LintingRuleEngine`
- Check rule configuration in `lucee-toolbox.json`
- Verify rule ID matches configuration

#### Tests Failing
- Check test file paths
- Verify test data is correct
- Run tests individually for debugging

### Debugging

#### Enable Verbose Logging
```bash
java -jar target/lucee-toolbox-1.0.0.jar -i test.cfc -v -m lint
```

#### Debug Parser Issues
```bash
# Test different parsers
java -jar target/lucee-toolbox-1.0.0.jar -i test.cfc -p boxlang -v -m lint
java -jar target/lucee-toolbox-1.0.0.jar -i test.cfc -p lucee -v -m lint
java -jar target/lucee-toolbox-1.0.0.jar -i test.cfc -p regex -v -m lint
```

#### Debug Configuration
```bash
# Show current configuration
java -jar target/lucee-toolbox-1.0.0.jar --show-config -i .
```

---

## 📚 Additional Resources

- [README.md](README.md) - Project overview and usage
- [BUILD.md](BUILD.md) - Detailed build instructions
- [RULES.md](RULES.md) - Complete rule documentation
- [REPL_README.md](REPL_README.md) - CFML REPL usage
- [BoxLang Documentation](https://boxlang.io/docs)
- [Lucee Documentation](https://docs.lucee.org)

---

## 🎯 Next Steps

After reading this guide, you should be able to:

1. ✅ Build the project from source
2. ✅ Run and write tests
3. ✅ Understand the project architecture
4. ✅ Create new parsers
5. ✅ Add linting rules
6. ✅ Add formatting rules
7. ✅ Contribute to the project

Happy coding! 🚀
