#!/bin/bash

# Lucee Toolbox - Run Script
# This script runs the application with the correct Java version

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

# Function to show help
show_help() {
    echo "Lucee Toolbox Run Script"
    echo
    echo "Usage: $0 [LUCEE_TOOLBOX_OPTIONS...]"
    echo
    echo "This script ensures the correct Java version is used and runs the Lucee Toolbox."
    echo "All arguments are passed through to the Lucee Toolbox application."
    echo
    echo "Examples:"
    echo "  $0 --help                     Show Lucee Toolbox help"
    echo "  $0 --version                  Show version"
    echo "  $0 -i src/ -m lint            Lint src/ directory"
    echo "  $0 -i file.cfc -m format      Format a single file"
    echo "  $0 -i src/ --dry-run          Show what would be changed"
}

# Check if help is requested
if [[ "$1" == "-h" || "$1" == "--help" ]]; then
    show_help
    exit 0
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Lucee Toolbox${NC}"
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

# Change to project directory
cd "$PROJECT_DIR"

# Check if JAR file exists
JAR_FILE="target/lucee-toolbox-1.0.1.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${YELLOW}⚠️  JAR file not found: $JAR_FILE${NC}"
    echo -e "${YELLOW}Building project first...${NC}"
    ./scripts/build.sh
    echo
fi

# Verify Java version
echo -e "${BLUE}Using Java version:${NC}"
java -version
echo

# Run the application
echo -e "${BLUE}Running: java -jar $JAR_FILE $@${NC}"
echo

# Set JVM options for better performance
export JAVA_OPTS="-Xmx1g -XX:+UseG1GC -XX:+UseStringDeduplication"

# Execute the application
exec java $JAVA_OPTS -jar "$JAR_FILE" "$@"
