#!/bin/bash

# Lucee Toolbox Build Script
# Advanced CFML Linter and Formatter

set -e

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
PACKAGE_ONLY=false
NATIVE_IMAGE=false

print_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Build script for Lucee Toolbox"
    echo ""
    echo "Options:"
    echo "  -c, --clean          Clean before building"
    echo "  -s, --skip-tests     Skip running tests"
    echo "  -v, --verbose        Verbose output"
    echo "  -p, --profile        Enable profiling"
    echo "  --package-only       Only package, don't compile"
    echo "  --native-image       Create native image executable with GraalVM"
    echo "  -h, --help           Show this help"
    echo ""
    echo "Examples:"
    echo "  $0                   # Standard build"
    echo "  $0 -c -v             # Clean build with verbose output"
    echo "  $0 -s --profile      # Build with profiling, skip tests"
    echo "  $0 --native-image    # Build using GraalVM native image"
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
        --package-only)
            PACKAGE_ONLY=true
            shift
            ;;
        --native-image)
            NATIVE_IMAGE=true
            shift
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            print_usage
            exit 1
            ;;
    esac
done

echo -e "${BLUE}🚀 Lucee Toolbox Build Script${NC}"
echo -e "${BLUE}===============================${NC}"

# Set Maven options
MAVEN_OPTS=""
if [ "$VERBOSE" = true ]; then
    MAVEN_OPTS="$MAVEN_OPTS -X"
fi

if [ "$PROFILE" = true ]; then
    MAVEN_OPTS="$MAVEN_OPTS -Dprofile"
fi

# Clean if requested
if [ "$CLEAN" = true ]; then
    echo -e "${YELLOW}🧹 Cleaning project...${NC}"
    mvn clean $MAVEN_OPTS
fi

# Skip compilation if package-only
if [ "$PACKAGE_ONLY" = false ]; then
    echo -e "${YELLOW}🔨 Compiling sources...${NC}"
    mvn compile $MAVEN_OPTS
    
    if [ "$SKIP_TESTS" = false ]; then
        echo -e "${YELLOW}🧪 Running tests...${NC}"
        mvn test $MAVEN_OPTS
    else
        echo -e "${YELLOW}⏭️  Skipping tests...${NC}"
    fi
fi

# Package
echo -e "${YELLOW}📦 Packaging...${NC}"
if [ "$SKIP_TESTS" = true ]; then
    mvn package -DskipTests $MAVEN_OPTS
else
    mvn package $MAVEN_OPTS
fi

# Check if build was successful
if [ $? -eq 0 ]; then
    # Create dist directory
    mkdir -p dist
    
    # Copy JAR to dist
    cp target/lucee-toolbox-1.0.0.jar dist/
    
    echo ""
    echo -e "${GREEN}✅ Build completed successfully!${NC}"
    echo -e "${GREEN}📋 Executable JAR: dist/lucee-toolbox-1.0.0.jar${NC}"
    echo ""
    echo -e "${BLUE}Quick test:${NC}"
    echo -e "${BLUE}java -jar dist/lucee-toolbox-1.0.0.jar --version${NC}"
    echo ""
else
    echo -e "${RED}❌ Build failed!${NC}"
    exit 1
fi

# Optional: Create native image
if [ "$NATIVE_IMAGE" = true ]; then
    echo -e "${YELLOW}🔥 Building native image with GraalVM...${NC}"
    
    # Check if native-image is available
    if ! command -v native-image &> /dev/null; then
        echo -e "${RED}❌ native-image not found!${NC}"
        echo -e "${YELLOW}Please install GraalVM and native-image:${NC}"
        echo -e "${BLUE}  sdk install java 17.0.7-graal${NC}"
        echo -e "${BLUE}  gu install native-image${NC}"
        exit 1
    fi
    
    # Build native image
    native-image -jar target/lucee-toolbox-1.0.0.jar \
        --no-fallback \
        --enable-https \
        --report-unsupported-elements-at-runtime \
        -H:Name=dist/lucee-toolbox \
        -H:+ReportExceptionStackTraces
    
    # Check if native image build was successful
    if [ $? -eq 0 ]; then
        echo ""
        echo -e "${GREEN}✅ Native image created successfully!${NC}"
        echo -e "${GREEN}📋 Native executable: dist/lucee-toolbox${NC}"
        echo ""
        echo -e "${BLUE}Quick test:${NC}"
        echo -e "${BLUE}./dist/lucee-toolbox --version${NC}"
        echo ""
    else
        echo -e "${RED}❌ Native image build failed!${NC}"
        exit 1
    fi
fi
