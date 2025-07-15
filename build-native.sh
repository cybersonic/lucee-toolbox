#!/bin/bash

# Cross-platform native build script for Lucee Toolbox
# This script creates native executables for different platforms

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔥 Lucee Toolbox Native Build Script${NC}"
echo -e "${BLUE}====================================${NC}"

# Check if native-image is available
if ! command -v native-image &> /dev/null; then
    echo -e "${RED}❌ native-image not found!${NC}"
    echo -e "${YELLOW}Please install GraalVM and native-image:${NC}"
    echo -e "${BLUE}  sdk install java 21.0.1-graal${NC}"
    echo -e "${BLUE}  gu install native-image${NC}"
    exit 1
fi

# Ensure JAR is built
if [ ! -f "target/lucee-toolbox-1.0.0.jar" ]; then
    echo -e "${YELLOW}📦 Building JAR first...${NC}"
    ./build.sh
fi

# Detect current platform
OS=$(uname -s | tr '[:upper:]' '[:lower:]')
ARCH=$(uname -m)

case $OS in
    "darwin")
        PLATFORM="macos"
        EXECUTABLE_SUFFIX=""
        ;;
    "linux")
        PLATFORM="linux"
        EXECUTABLE_SUFFIX=""
        ;;
    "mingw"*|"msys"*|"cygwin"*)
        PLATFORM="windows"
        EXECUTABLE_SUFFIX=".exe"
        ;;
    *)
        PLATFORM="unknown"
        EXECUTABLE_SUFFIX=""
        ;;
esac

echo -e "${YELLOW}🎯 Building for platform: ${PLATFORM}-${ARCH}${NC}"

# Build native executable
EXECUTABLE_NAME="lucee-toolbox-${PLATFORM}-${ARCH}${EXECUTABLE_SUFFIX}"

echo -e "${YELLOW}🔨 Creating native executable: ${EXECUTABLE_NAME}${NC}"

native-image \
    -jar target/lucee-toolbox-1.0.0.jar \
    -o "${EXECUTABLE_NAME}"

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✅ Native executable created successfully!${NC}"
    echo -e "${GREEN}📋 Executable: ${EXECUTABLE_NAME}${NC}"
    echo ""
    
    # Test the executable
    echo -e "${YELLOW}🧪 Testing native executable...${NC}"
    ./${EXECUTABLE_NAME} --version
    
    echo ""
    echo -e "${BLUE}Usage:${NC}"
    echo -e "${BLUE}  ./${EXECUTABLE_NAME} -i src/${NC}"
    echo ""
    
    # Create a generic symlink for convenience
    if [ -f "lucee-toolbox${EXECUTABLE_SUFFIX}" ]; then
        rm "lucee-toolbox${EXECUTABLE_SUFFIX}"
    fi
    ln -s "${EXECUTABLE_NAME}" "lucee-toolbox${EXECUTABLE_SUFFIX}"
    echo -e "${GREEN}📋 Generic symlink created: lucee-toolbox${EXECUTABLE_SUFFIX}${NC}"
    echo ""
else
    echo -e "${RED}❌ Native executable build failed!${NC}"
    exit 1
fi
