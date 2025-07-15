#!/bin/bash

# Lucee Toolbox - Build Script
# This script builds the project with the correct Java version

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default options
CLEAN=false
SKIP_TESTS=false
VERBOSE=false
PROFILE=false
MINIMAL=false
PRODUCTION=false
PARALLEL=false

# Required Java version
REQUIRED_JAVA_VERSION="17.0.12-graal"

# Function to show help
show_help() {
    echo "Lucee Toolbox Build Script"
    echo
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "Options:"
    echo "  -c, --clean         Clean before building"
    echo "  -s, --skip-tests    Skip running tests"
    echo "  -v, --verbose       Verbose Maven output"
    echo "  -p, --profile       Enable profiling with JaCoCo"
    echo "  -m, --minimal       Build with minimal dependencies"
    echo "  -P, --production    Production build (optimized)"
    echo "  -j, --parallel      Enable parallel builds"
    echo "  -h, --help          Show this help message"
    echo
    echo "Examples:"
    echo "  $0                  Standard build"
    echo "  $0 -c -v            Clean build with verbose output"
    echo "  $0 -m -s            Minimal build without tests"
    echo "  $0 -P               Production build"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -c|--clean)
            CLEAN=true
            shift
            ;;
        -s|--skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -p|--profile)
            PROFILE=true
            shift
            ;;
        -m|--minimal)
            MINIMAL=true
            shift
            ;;
        -P|--production)
            PRODUCTION=true
            shift
            ;;
        -j|--parallel)
            PARALLEL=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Lucee Toolbox Build${NC}"
echo -e "${BLUE}========================================${NC}"
echo

# Initialize SDKMan if available
if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
fi

# Check if we're in a directory with .sdkmanrc
if [ -f "$PROJECT_DIR/.sdkmanrc" ]; then
    echo -e "${BLUE}Using SDKMan environment from .sdkmanrc${NC}"
    cd "$PROJECT_DIR"
    sdk env
else
    # Fallback to manual Java version check
    if command -v sdk &> /dev/null; then
        echo -e "${BLUE}Setting Java version to $REQUIRED_JAVA_VERSION${NC}"
        sdk use java "$REQUIRED_JAVA_VERSION"
    fi
fi

# Verify Java version
echo -e "${BLUE}Java version:${NC}"
java -version
echo

# Change to project directory
cd "$PROJECT_DIR"

# Set Maven options for better performance
export MAVEN_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"

# Build Maven command
MVN_CMD="mvn"

# Add clean if requested
if [ "$CLEAN" = true ]; then
    echo -e "${YELLOW}🧹 Cleaning previous build...${NC}"
    mvn clean
fi

# Determine build goal
if [ "$PROFILE" = true ]; then
    GOAL="clean test jacoco:report package"
    echo -e "${BLUE}📊 Building with test coverage profiling...${NC}"
else
    GOAL="package"
    echo -e "${BLUE}🔨 Building project...${NC}"
fi

# Build Maven options
MVN_OPTIONS=""

if [ "$VERBOSE" = true ]; then
    MVN_OPTIONS="$MVN_OPTIONS -X"
fi

if [ "$SKIP_TESTS" = true ]; then
    MVN_OPTIONS="$MVN_OPTIONS -DskipTests"
fi

if [ "$PARALLEL" = true ]; then
    MVN_OPTIONS="$MVN_OPTIONS -T 1C"
fi

# Profile options
if [ "$MINIMAL" = true ]; then
    MVN_OPTIONS="$MVN_OPTIONS -Pminimal"
    echo -e "${YELLOW}📦 Using minimal profile (fewer dependencies)${NC}"
fi

if [ "$PRODUCTION" = true ]; then
    MVN_OPTIONS="$MVN_OPTIONS -Dmaven.test.skip=true -Dminimal=true"
    echo -e "${YELLOW}🚀 Production build (optimized, no tests)${NC}"
fi

# Execute Maven build
echo -e "${BLUE}Running: mvn $GOAL $MVN_OPTIONS${NC}"
echo

if ! mvn $GOAL $MVN_OPTIONS; then
    echo -e "${RED}❌ Build failed!${NC}"
    exit 1
fi

echo
echo -e "${GREEN}✅ Build completed successfully!${NC}"

# Show build artifacts
if [ -f "target/lucee-toolbox-1.0.1.jar" ]; then
    echo -e "${BLUE}📦 Build artifacts:${NC}"
    ls -la target/lucee-toolbox-*.jar
    echo
    echo -e "${GREEN}Ready to use:${NC}"
    echo "java -jar target/lucee-toolbox-1.0.1.jar --help"
fi

# Show coverage report if profiling was enabled
if [ "$PROFILE" = true ] && [ -f "target/site/jacoco/index.html" ]; then
    echo -e "${BLUE}📊 Coverage report generated:${NC}"
    echo "open target/site/jacoco/index.html"
fi
