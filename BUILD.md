# Building Lucee Toolbox

This document explains the different ways to build the Lucee Toolbox.

## 🏗️ Build Options

### Standard JAR Build
```bash
./build.sh
```
Creates a standard executable JAR file that requires Java to run.

**Output:** `target/lucee-toolbox-1.0.0.jar`

**Usage:** `java -jar target/lucee-toolbox-1.0.0.jar [options]`

### Native Executable Build (GraalVM)
```bash
./build.sh --native-image
```
Creates a native executable that doesn't require Java to be installed.

**Requirements:**
- GraalVM 21+ installed
- `native-image` component installed

**Output:** `lucee-toolbox` (or `lucee-toolbox.exe` on Windows)

**Usage:** `./lucee-toolbox [options]`

### Cross-Platform Native Build
```bash
./build-native.sh
```
Creates platform-specific native executables with enhanced configuration.

**Output:** `lucee-toolbox-<platform>-<arch>`

## 🚀 GraalVM Setup

### Install GraalVM (using SDKMAN)
```bash
# Install SDKMAN (if not already installed)
curl -s "https://get.sdkman.io" | bash

# Install GraalVM
sdk install java 21.0.1-graal
sdk use java 21.0.1-graal

# Install native-image component
gu install native-image
```

### Alternative: Direct Download
1. Download GraalVM from: https://github.com/graalvm/graalvm-ce-builds/releases
2. Extract and set `JAVA_HOME` to GraalVM directory
3. Run `gu install native-image`

## 📊 Performance Comparison

| Build Type | Startup Time | Memory Usage | Distribution Size |
|------------|-------------|--------------|-------------------|
| JAR        | ~2000ms     | ~150MB       | ~50MB + JRE       |
| Native     | ~50ms       | ~50MB        | ~30MB             |

## 🛠️ Build Script Options

| Option | Description |
|--------|-------------|
| `-c, --clean` | Clean before building |
| `-s, --skip-tests` | Skip running tests |
| `-v, --verbose` | Verbose output |
| `-p, --profile` | Enable profiling |
| `--package-only` | Only package, don't compile |
| `--native-image` | Create native image executable |
| `-h, --help` | Show help message |

## 🔧 Native Image Configuration

The project includes optimized native image configuration:

- **Resource inclusion**: Properties, JSON, XML, YAML files
- **Reflection config**: Common classes that use reflection
- **HTTPS support**: Enabled for documentation integration
- **Error reporting**: Enhanced stack traces for debugging

## 📦 Distribution

### For End Users
- **JAR**: Requires Java 11+ to be installed
- **Native**: No dependencies, faster startup

### For Developers
- **JAR**: Easier debugging and development
- **Native**: Production deployment, CI/CD integration

## 🐛 Troubleshooting

### Native Image Build Fails
1. Ensure GraalVM is properly installed
2. Check that `native-image` is in your PATH
3. Verify Java version: `java -version`
4. Try building with verbose output: `./build.sh --native-image -v`

### Missing Dependencies
Some dependencies might not be compatible with native image. Check:
- BoxLang ANTLR parser compatibility
- Reflection usage in dependencies
- Dynamic class loading

### Performance Issues
- Native image builds take longer (~5-10 minutes)
- First run may be slower due to profile-guided optimization
- Use `--performance` flag for large codebases

## 🚀 CI/CD Integration

### GitHub Actions
```yaml
- name: Build Native Image
  run: |
    sdk install java 21.0.1-graal
    gu install native-image
    ./build.sh --native-image
```

### Docker
```dockerfile
FROM ghcr.io/graalvm/graalvm-ce:java17-22.3.0 AS builder
RUN gu install native-image
COPY . /app
WORKDIR /app
RUN ./build.sh --native-image

FROM scratch
COPY --from=builder /app/lucee-toolbox /lucee-toolbox
ENTRYPOINT ["/lucee-toolbox"]
```
