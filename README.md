# Lucee Toolbox

> Advanced CFML Linter and Formatter for Lucee projects

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/cybersonic/lucee-toolbox)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Java Version](https://img.shields.io/badge/java-17%2B-orange)](https://openjdk.java.net/projects/jdk/17/)
[![Maven Central](https://img.shields.io/badge/maven-1.0.0-blue)](https://mvnrepository.com/artifact/com.lucee.toolbox/lucee-toolbox)

## 🚀 Features

- **Multiple Parser Support**: BoxLang ANTLR, Lucee native parser, and regex fallback
- **Comprehensive Linting**: Standard CFML code standards + CFLint rules integration
- **Advanced Formatting**: Based on cfformat with extensive customization
- **Multiple Output Formats**: Console, JSON, Bitbucket Pipelines, HTML, CSV, JUnit, SARIF
- **Performance Optimized**: Designed for large codebases and very long files
- **Documentation Integration**: Lucee docs integration for built-in vs user-defined functions
- **VSCode Ready**: Language server protocol support for seamless editor integration
- **Parallel Processing**: Multi-threaded analysis for large projects

## 📦 Installation

### Download JAR
```bash
wget https://github.com/cybersonic/lucee-toolbox/releases/latest/download/lucee-toolbox-1.0.0.jar
```

### Build from Source
```bash
git clone https://github.com/cybersonic/lucee-toolbox.git
cd lucee-toolbox
./build.sh
```

### Build Native Executable (GraalVM)

#### Linux/macOS
```bash
# Install GraalVM (using SDKMAN)
sdk install java 21.0.1-graal
gu install native-image

# Build native executable
./build.sh --native-image

# Or use the dedicated script for cross-platform builds
./build-native.sh
```

#### Windows
```cmd
# Install GraalVM for Windows
# 1. Download GraalVM from: https://github.com/graalvm/graalvm-ce-builds/releases
# 2. Set JAVA_HOME to GraalVM directory
# 3. Add %JAVA_HOME%\bin to PATH
# 4. Install native-image component
gu install native-image

# Build using batch script
build-windows.cmd

# Or build using PowerShell script
.\build-windows.ps1
```

### Maven Dependency
```xml
<dependency>
    <groupId>com.lucee.toolbox</groupId>
    <artifactId>lucee-toolbox</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🎯 Quick Start

### Lint CFML Files
```bash
# Using JAR (cross-platform)
java -jar dist/lucee-toolbox-1.0.0.jar -i MyComponent.cfc

# Using native executable (faster startup)
# Linux/macOS
./dist/lucee-toolbox -i MyComponent.cfc

# Windows
dist\lucee-toolbox-windows-x64.exe -i MyComponent.cfc

# Lint entire project
./dist/lucee-toolbox -i src/ -f console

# Generate Bitbucket Pipelines report
./dist/lucee-toolbox -i src/ -f bitbucket -o report.json
```

### Format CFML Files
```bash
# Format a file (dry run)
./dist/lucee-toolbox -i MyComponent.cfc -m format --dry-run

# Format and show diff
./dist/lucee-toolbox -i src/ -m format --diff

# Format with backup
./dist/lucee-toolbox -i src/ -m format --backup
```

### Both Lint and Format
```bash
# Lint and format in one pass
./dist/lucee-toolbox -i src/ -m both --performance
```

## ⚙️ Configuration

Create a `lucee-toolbox.json` file in your project root:

```json
{
  "extends": ["standard", "cflint-basic"],
  "linting": {
    "rules": {
      "naming": {
        "componentCase": "PascalCase",
        "functionCase": "camelCase"
      },
      "bestPractices": {
        "useVarScoping": true,
        "preferDoubleQuotes": true
      }
    }
  },
  "formatting": {
    "indentation": {
      "type": "spaces",
      "size": 4
    },
    "braces": {
      "style": "same-line"
    }
  }
}
```

### Predefined Rule Sets

- **`standard`**: Standard CFML coding conventions (default)
- **`cflint`**: CFLint compatibility rules
- **`minimal`**: Essential rules only
- **`strict`**: All rules enabled with strict enforcement

## 🔧 Command Line Options

### Main Options
- `-i, --input <PATH>`: Input file or directory (required)
- `-m, --mode <MODE>`: Processing mode: `lint`, `format`, or `both`
- `-p, --parser <PARSER>`: Parser: `auto`, `boxlang`, `lucee`, `regex`
- `-c, --config <FILE>`: Configuration file path
- `-f, --format <FORMAT>`: Output format
- `-o, --output <FILE>`: Output file path

### Output Formats
- `console`: Colored console output (default)
- `json`: Machine-readable JSON
- `bitbucket`: Bitbucket Pipelines annotations
- `html`: Rich HTML report with metrics
- `csv`: Comma-separated values
- `junit`: JUnit XML for CI/CD
- `sarif`: SARIF format for security tools

### Performance Options
- `--performance`: Enable optimizations for large codebases
- `--max-threads <N>`: Maximum parallel threads
- `--timeout <SECONDS>`: Per-file processing timeout
- `--no-cache`: Disable caching

### Filtering Options
- `--include <PATTERN>`: Include files matching pattern
- `--exclude <PATTERN>`: Exclude files matching pattern
- `--severity <LEVEL>`: Minimum severity: `error`, `warning`, `info`
- `--rules <RULES>`: Enable/disable specific rules: `+rule1,-rule2`

## 📊 Integration Examples

### Bitbucket Pipelines
```yaml
image: openjdk:17

pipelines:
  default:
    - step:
        name: CFML Lint
        script:
          - wget https://github.com/cybersonic/lucee-toolbox/releases/latest/download/lucee-toolbox-1.0.0.jar
          - java -jar lucee-toolbox-1.0.0.jar -i src/ -f bitbucket -o cfml-report.json
        artifacts:
          - cfml-report.json
```

### GitHub Actions
```yaml
name: CFML Quality Check
on: [push, pull_request]

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Download Lucee Toolbox
        run: wget https://github.com/cybersonic/lucee-toolbox/releases/latest/download/lucee-toolbox-1.0.0.jar
      - name: Lint CFML
        run: java -jar lucee-toolbox-1.0.0.jar -i src/ -f sarif -o cfml.sarif
      - name: Upload SARIF
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: cfml.sarif
```

### VSCode Integration
The toolbox includes Language Server Protocol support. Install the companion VSCode extension:

```bash
# Install from VSCode marketplace
ext install lucee.lucee-toolbox
```

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Lucee Toolbox Core                      │
├─────────────────────────────────────────────────────────────┤
│  CLI Interface  │  Configuration  │  File Discovery       │
├─────────────────────────────────────────────────────────────┤
│           Parser Selection & Management                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │  BoxLang    │ │   Lucee     │ │  Regex Fallback     │   │
│  │   ANTLR     │ │   Native    │ │    Parser           │   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│              Analysis Engines                               │
│  ┌─────────────────────────┐ ┌─────────────────────────┐   │
│  │    Linting Engine       │ │   Formatting Engine     │   │
│  │  • Standard Rules       │ │  • cfformat inspired    │   │
│  │  • CFLint Integration   │ │  • Multi-line support   │   │
│  │  • Custom Rules         │ │  • Whitespace control   │   │
│  └─────────────────────────┘ └─────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│                   Output Formatters                        │
│  Console │ JSON │ Bitbucket │ HTML │ CSV │ JUnit │ SARIF   │
└─────────────────────────────────────────────────────────────┘
```

## 🧪 Testing

```bash
# Run all tests
./build.sh

# Run with coverage
./build.sh -p

# Skip tests
./build.sh -s

# Integration tests only
mvn test -Dtest=**/*IntegrationTest
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Development Setup
```bash
git clone https://github.com/cybersonic/lucee-toolbox.git
cd lucee-toolbox
./build.sh -v
```

### Release Management

The project includes automated release management:

- **Development Releases**: Every push to `main` creates a development release with native binaries
- **Version Management**: Use `./version.sh [version]` to update version across all files
- **Automated Releases**: Use `./release.sh [version]` for complete release automation
- **Changelog Generation**: Use `./changelog.sh --version [version]` to generate release notes

```bash
# Interactive release with version suggestions
./release.sh

# Direct version release
./release.sh 1.0.2

# Generate changelog for upcoming release
./changelog.sh --version 1.0.2
```

See [DEVELOPMENT.md](DEVELOPMENT.md) for complete development documentation.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [CFML Community](https://cfml.slack.com/) for the comprehensive CFML style guide
- [CFLint](https://github.com/cfmleditor/CFLint) for linting rule inspiration
- [cfformat](https://github.com/jcberquist/commandbox-cfformat) for formatting standards
- [BoxLang](https://boxlang.io/) for the ANTLR parser
- [Lucee](https://lucee.org/) for the amazing CFML engine

## 🔗 Related Projects

- [CFML Editor for VSCode](https://marketplace.visualstudio.com/items?itemName=cfmleditor.cfmleditor)
- [CFLint](https://github.com/cfmleditor/CFLint)
- [CommandBox cfformat](https://github.com/jcberquist/commandbox-cfformat)
- [BoxLang](https://github.com/ortus-boxlang/BoxLang)
