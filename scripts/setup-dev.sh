#!/bin/bash

# Lucee Toolbox - Development Environment Setup Script
# This script sets up the development environment with the correct Java version

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Required Java version
REQUIRED_JAVA_VERSION="17.0.12-graal"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Lucee Toolbox Development Setup${NC}"
echo -e "${BLUE}========================================${NC}"
echo

# Check if SDKMan is installed
if ! command -v sdk &> /dev/null; then
    echo -e "${RED}❌ SDKMan is not installed${NC}"
    echo -e "${YELLOW}Installing SDKMan...${NC}"
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
else
    echo -e "${GREEN}✅ SDKMan is available${NC}"
fi

# Initialize SDKMan in current session
if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
fi

# Check if required Java version is installed
if ! sdk list java | grep -q "$REQUIRED_JAVA_VERSION"; then
    echo -e "${YELLOW}⚠️  Java $REQUIRED_JAVA_VERSION is not installed${NC}"
    echo -e "${YELLOW}Installing Java $REQUIRED_JAVA_VERSION...${NC}"
    sdk install java "$REQUIRED_JAVA_VERSION"
else
    echo -e "${GREEN}✅ Java $REQUIRED_JAVA_VERSION is available${NC}"
fi

# Use the required Java version
echo -e "${BLUE}Setting Java version to $REQUIRED_JAVA_VERSION${NC}"
sdk use java "$REQUIRED_JAVA_VERSION"

# Verify Java version
echo -e "${BLUE}Verifying Java installation...${NC}"
java -version

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo -e "${YELLOW}⚠️  Maven is not installed${NC}"
    echo -e "${YELLOW}Installing Maven...${NC}"
    sdk install maven
else
    echo -e "${GREEN}✅ Maven is available${NC}"
fi

# Create .sdkmanrc file for automatic Java version switching
echo -e "${BLUE}Creating .sdkmanrc file...${NC}"
echo "java=$REQUIRED_JAVA_VERSION" > "$PROJECT_DIR/.sdkmanrc"

# Set Maven options for better performance
export MAVEN_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"

echo -e "${GREEN}✅ Development environment setup complete!${NC}"
echo
echo -e "${BLUE}Next steps:${NC}"
echo "1. Run ${GREEN}./scripts/build.sh${NC} to build the project"
echo "2. Run ${GREEN}./scripts/test.sh${NC} to run tests"
echo "3. Run ${GREEN}./scripts/run.sh --help${NC} to see usage options"
echo
echo -e "${YELLOW}Note: In new terminal sessions, run ${GREEN}sdk env${NC} to automatically use the correct Java version${NC}"
