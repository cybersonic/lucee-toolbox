#!/bin/bash

# Lucee Toolbox - Clean Script
# This script cleans build artifacts and caches

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
DEEP_CLEAN=false
CACHE_CLEAN=false

# Function to show help
show_help() {
    echo "Lucee Toolbox Clean Script"
    echo
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "Options:"
    echo "  -d, --deep          Deep clean (including Maven cache)"
    echo "  -c, --cache         Clean only caches"
    echo "  -h, --help          Show this help message"
    echo
    echo "Examples:"
    echo "  $0                  Standard clean"
    echo "  $0 -d               Deep clean with Maven cache"
    echo "  $0 -c               Clean caches only"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -d|--deep)
            DEEP_CLEAN=true
            shift
            ;;
        -c|--cache)
            CACHE_CLEAN=true
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
echo -e "${BLUE}Lucee Toolbox Clean${NC}"
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
fi

# Change to project directory
cd "$PROJECT_DIR"

if [ "$CACHE_CLEAN" = true ]; then
    echo -e "${YELLOW}🧹 Cleaning caches only...${NC}"
    
    # Clean Maven cache for this project
    if [ -d "$HOME/.m2/repository/com/lucee/toolbox" ]; then
        echo -e "${BLUE}Removing Maven cache for lucee-toolbox...${NC}"
        rm -rf "$HOME/.m2/repository/com/lucee/toolbox"
    fi
    
    # Clean IDE files
    if [ -d ".idea" ]; then
        echo -e "${BLUE}Removing IntelliJ IDEA files...${NC}"
        rm -rf .idea
    fi
    
    if [ -f "*.iml" ]; then
        echo -e "${BLUE}Removing IntelliJ IDEA module files...${NC}"
        rm -f *.iml
    fi
    
    if [ -d ".vscode" ]; then
        echo -e "${BLUE}Removing VS Code files...${NC}"
        rm -rf .vscode
    fi
    
    if [ -d ".settings" ]; then
        echo -e "${BLUE}Removing Eclipse settings...${NC}"
        rm -rf .settings
    fi
    
    if [ -f ".project" ] || [ -f ".classpath" ]; then
        echo -e "${BLUE}Removing Eclipse project files...${NC}"
        rm -f .project .classpath
    fi
    
elif [ "$DEEP_CLEAN" = true ]; then
    echo -e "${YELLOW}🧹 Performing deep clean...${NC}"
    
    # Maven clean
    echo -e "${BLUE}Running Maven clean...${NC}"
    mvn clean
    
    # Clean Maven cache
    echo -e "${BLUE}Cleaning Maven cache...${NC}"
    if [ -d "$HOME/.m2/repository/com/lucee/toolbox" ]; then
        rm -rf "$HOME/.m2/repository/com/lucee/toolbox"
    fi
    
    # Clean temporary files
    echo -e "${BLUE}Cleaning temporary files...${NC}"
    find . -name "*.tmp" -type f -delete 2>/dev/null || true
    find . -name "*.temp" -type f -delete 2>/dev/null || true
    find . -name ".DS_Store" -type f -delete 2>/dev/null || true
    
    # Clean IDE files
    echo -e "${BLUE}Cleaning IDE files...${NC}"
    rm -rf .idea *.iml .vscode .settings .project .classpath
    
    # Clean logs
    if [ -d "logs" ]; then
        echo -e "${BLUE}Cleaning logs...${NC}"
        rm -rf logs
    fi
    
else
    echo -e "${YELLOW}🧹 Performing standard clean...${NC}"
    
    # Standard Maven clean
    echo -e "${BLUE}Running Maven clean...${NC}"
    mvn clean
    
    # Clean common temporary files
    echo -e "${BLUE}Cleaning temporary files...${NC}"
    find . -name "*.tmp" -type f -delete 2>/dev/null || true
    find . -name ".DS_Store" -type f -delete 2>/dev/null || true
fi

echo
echo -e "${GREEN}✅ Clean completed successfully!${NC}"

# Show what was cleaned
echo -e "${BLUE}Cleaned:${NC}"
echo "  - Maven target directory"
echo "  - Temporary files"

if [ "$CACHE_CLEAN" = true ] || [ "$DEEP_CLEAN" = true ]; then
    echo "  - IDE configuration files"
fi

if [ "$DEEP_CLEAN" = true ]; then
    echo "  - Maven local repository cache"
    echo "  - Log files"
fi

echo
echo -e "${YELLOW}Ready for fresh build: ${GREEN}./scripts/build.sh${NC}"
