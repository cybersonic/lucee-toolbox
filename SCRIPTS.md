# Development Scripts

This directory contains scripts to help with development tasks. All scripts use SDKMan to ensure the correct Java version (17.0.12-graal) is being used.

## Quick Start

```bash
# Set up development environment
./scripts/setup-dev.sh

# Build the project
./scripts/build.sh

# Run tests
./scripts/test.sh

# Run the application
./scripts/run.sh --help
```

## Available Scripts

### `setup-dev.sh`
Sets up the development environment by:
- Installing SDKMan if not present
- Installing the correct Java version (17.0.12-graal)
- Creating `.sdkmanrc` file for automatic Java version switching
- Verifying Maven is available

**Usage:**
```bash
./scripts/setup-dev.sh
```

### `build.sh`
Builds the project with various options.

**Usage:**
```bash
./scripts/build.sh [OPTIONS]
```

**Options:**
- `-c, --clean` - Clean before building
- `-s, --skip-tests` - Skip running tests
- `-v, --verbose` - Verbose Maven output
- `-p, --profile` - Enable profiling with JaCoCo
- `-m, --minimal` - Build with minimal dependencies
- `-P, --production` - Production build (optimized)
- `-j, --parallel` - Enable parallel builds
- `-h, --help` - Show help message

**Examples:**
```bash
./scripts/build.sh                  # Standard build
./scripts/build.sh -c -v            # Clean build with verbose output
./scripts/build.sh -m -s            # Minimal build without tests
./scripts/build.sh -P               # Production build
```

### `test.sh`
Runs tests with various options.

**Usage:**
```bash
./scripts/test.sh [OPTIONS]
```

**Options:**
- `-c, --coverage` - Generate test coverage report
- `-v, --verbose` - Verbose test output
- `-t, --test NAME` - Run specific test class
- `-i, --integration` - Run integration tests only
- `-u, --unit` - Run unit tests only
- `-h, --help` - Show help message

**Examples:**
```bash
./scripts/test.sh                   # Run all tests
./scripts/test.sh -c                # Run tests with coverage
./scripts/test.sh -t BadUserServiceTest  # Run specific test
./scripts/test.sh -i                # Run integration tests only
```

### `clean.sh`
Cleans build artifacts and caches.

**Usage:**
```bash
./scripts/clean.sh [OPTIONS]
```

**Options:**
- `-d, --deep` - Deep clean (including Maven cache)
- `-c, --cache` - Clean only caches
- `-h, --help` - Show help message

**Examples:**
```bash
./scripts/clean.sh                  # Standard clean
./scripts/clean.sh -d               # Deep clean with Maven cache
./scripts/clean.sh -c               # Clean caches only
```

### `run.sh`
Runs the application with the correct Java version.

**Usage:**
```bash
./scripts/run.sh [LUCEE_TOOLBOX_OPTIONS...]
```

All arguments are passed through to the Lucee Toolbox application.

**Examples:**
```bash
./scripts/run.sh --help                     # Show Lucee Toolbox help
./scripts/run.sh --version                  # Show version
./scripts/run.sh -i src/ -m lint            # Lint src/ directory
./scripts/run.sh -i file.cfc -m format      # Format a single file
./scripts/run.sh -i src/ --dry-run          # Show what would be changed
```

## Java Version Management

All scripts automatically handle Java version management using SDKMan:

1. **Automatic Detection**: Scripts check for `.sdkmanrc` file and use `sdk env`
2. **Fallback**: If no `.sdkmanrc`, manually sets Java version to 17.0.12-graal
3. **Verification**: Shows Java version being used before running commands

## Environment Variables

The scripts set the following environment variables for optimal performance:

- `MAVEN_OPTS`: `-Xmx2g -XX:MaxMetaspaceSize=512m`
- `JAVA_OPTS`: `-Xmx1g -XX:+UseG1GC -XX:+UseStringDeduplication` (for run.sh)

## SDKMan Configuration

The project includes a `.sdkmanrc` file:

```
# Enable auto-env through the sdkman_auto_env config
# Add key=value pairs of SDKs to use below
java=17.0.12-graal
```

This allows automatic Java version switching when entering the project directory (if SDKMan auto-env is enabled).

## Troubleshooting

### Script Permission Issues
If you get permission denied errors:
```bash
chmod +x scripts/*.sh
```

### Java Version Issues
If you see Java version errors:
```bash
./scripts/setup-dev.sh  # Re-run setup
sdk use java 17.0.12-graal  # Manually set version
```

### SDKMan Issues
If SDKMan isn't working:
```bash
# Reinstall SDKMan
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

### Build Issues
If builds are failing:
```bash
./scripts/clean.sh -d    # Deep clean
./scripts/build.sh -v    # Verbose build to see errors
```

## IDE Integration

These scripts can be integrated into your IDE:

### VS Code
Add to `.vscode/tasks.json`:
```json
{
    "version": "2.0.0",
    "tasks": [
        {
            "label": "Build",
            "type": "shell",
            "command": "./scripts/build.sh",
            "group": "build",
            "presentation": {
                "echo": true,
                "reveal": "always",
                "focus": false,
                "panel": "shared"
            }
        },
        {
            "label": "Test",
            "type": "shell",
            "command": "./scripts/test.sh",
            "group": "test"
        }
    ]
}
```

### IntelliJ IDEA
1. Go to Run/Debug Configurations
2. Add new "Shell Script" configuration
3. Set script path to `./scripts/build.sh`
4. Set working directory to project root

### Eclipse
1. Right-click project → Properties
2. Go to Builders → New → Program
3. Set location to full path of script
4. Set working directory to `${project_loc}`

## Contributing

When adding new scripts:

1. Follow the existing naming convention
2. Include proper help text and option parsing
3. Use the same color coding for output
4. Include Java version verification
5. Add error handling and validation
6. Update this documentation

## Script Dependencies

The scripts have minimal dependencies:
- **bash** - For shell scripting
- **SDKMan** - For Java version management
- **Maven** - For building (installed via SDKMan)
- **Java 17** - Runtime (installed via SDKMan)

All dependencies are automatically installed by `setup-dev.sh`.
