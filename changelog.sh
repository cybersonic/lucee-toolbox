#!/bin/bash

# Changelog generator for Lucee Toolbox
# This script generates a changelog from git commits

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to get the last tag
get_last_tag() {
    git describe --tags --abbrev=0 2>/dev/null || echo ""
}

# Function to get commits since last tag
get_commits_since_tag() {
    local last_tag=$1
    if [[ -n "$last_tag" ]]; then
        git log ${last_tag}..HEAD --pretty=format:"%s" --no-merges
    else
        git log --pretty=format:"%s" --no-merges -n 20
    fi
}

# Function to categorize commits
categorize_commits() {
    local commits="$1"
    
    echo "## What's New"
    echo ""
    
    # Features
    local features=$(echo "$commits" | grep -i "^feat\|^add\|^implement\|^new" || true)
    if [[ -n "$features" ]]; then
        echo "### ✨ Features"
        while IFS= read -r line; do
            if [[ -n "$line" ]]; then
                echo "- $line"
            fi
        done <<< "$features"
        echo ""
    fi
    
    # Bug fixes
    local fixes=$(echo "$commits" | grep -i "^fix\|^bug\|^patch\|^resolve" || true)
    if [[ -n "$fixes" ]]; then
        echo "### 🐛 Bug Fixes"
        while IFS= read -r line; do
            if [[ -n "$line" ]]; then
                echo "- $line"
            fi
        done <<< "$fixes"
        echo ""
    fi
    
    # Improvements
    local improvements=$(echo "$commits" | grep -i "^improve\|^enhance\|^optimize\|^update\|^refactor" || true)
    if [[ -n "$improvements" ]]; then
        echo "### 🔧 Improvements"
        while IFS= read -r line; do
            if [[ -n "$line" ]]; then
                echo "- $line"
            fi
        done <<< "$improvements"
        echo ""
    fi
    
    # Documentation
    local docs=$(echo "$commits" | grep -i "^doc\|^readme\|^documentation" || true)
    if [[ -n "$docs" ]]; then
        echo "### 📚 Documentation"
        while IFS= read -r line; do
            if [[ -n "$line" ]]; then
                echo "- $line"
            fi
        done <<< "$docs"
        echo ""
    fi
    
    # CI/CD and build
    local ci=$(echo "$commits" | grep -i "^ci\|^build\|^workflow\|^action" || true)
    if [[ -n "$ci" ]]; then
        echo "### 🚀 CI/CD & Build"
        while IFS= read -r line; do
            if [[ -n "$line" ]]; then
                echo "- $line"
            fi
        done <<< "$ci"
        echo ""
    fi
    
    # Other changes
    local others=$(echo "$commits" | grep -v -i "^feat\|^add\|^implement\|^new\|^fix\|^bug\|^patch\|^resolve\|^improve\|^enhance\|^optimize\|^update\|^refactor\|^doc\|^readme\|^documentation\|^ci\|^build\|^workflow\|^action" || true)
    if [[ -n "$others" ]]; then
        echo "### 🔄 Other Changes"
        while IFS= read -r line; do
            if [[ -n "$line" ]]; then
                echo "- $line"
            fi
        done <<< "$others"
        echo ""
    fi
}

# Function to generate full changelog
generate_changelog() {
    local since_tag=$1
    local target_version=$2
    
    print_status "Generating changelog..."
    
    if [[ -n "$since_tag" ]]; then
        echo "# Changelog for $target_version"
        echo ""
        echo "*Generated from commits since $since_tag*"
        echo ""
    else
        echo "# Changelog for $target_version"
        echo ""
        echo "*Generated from recent commits*"
        echo ""
    fi
    
    local commits=$(get_commits_since_tag "$since_tag")
    
    if [[ -z "$commits" ]]; then
        echo "No commits found since last tag."
        return
    fi
    
    categorize_commits "$commits"
    
    echo "## Installation"
    echo ""
    echo "Download the native binary for your platform from the [releases page](https://github.com/cybersonic/lucee-toolbox/releases):"
    echo ""
    echo "- **Linux**: \`lucee-toolbox-linux-x64\`"
    echo "- **macOS**: \`lucee-toolbox-macos-arm64\`"
    echo "- **Windows**: \`lucee-toolbox-windows-x64.exe\`"
    echo ""
    echo "### Usage Example"
    echo "\`\`\`bash"
    echo "# Make executable (Linux/macOS)"
    echo "chmod +x lucee-toolbox-*"
    echo ""
    echo "# Test the binary"
    echo "./lucee-toolbox-linux-x64 --version"
    echo ""
    echo "# Lint CFML files"
    echo "./lucee-toolbox-linux-x64 -i src/"
    echo ""
    echo "# Format CFML files"
    echo "./lucee-toolbox-linux-x64 -i src/ --format"
    echo "\`\`\`"
}

# Main function
main() {
    case "$1" in
        --since)
            if [[ -z "$2" ]]; then
                print_error "Please provide a tag name with --since"
                exit 1
            fi
            generate_changelog "$2" "${3:-Next Version}"
            ;;
        --version)
            if [[ -z "$2" ]]; then
                print_error "Please provide a version with --version"
                exit 1
            fi
            local last_tag=$(get_last_tag)
            generate_changelog "$last_tag" "$2"
            ;;
        --help|-h)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  --since <tag>     Generate changelog since specific tag"
            echo "  --version <ver>   Generate changelog for specific version (uses last tag)"
            echo "  --help, -h        Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 --version 1.0.2              # Generate changelog for version 1.0.2"
            echo "  $0 --since v1.0.0               # Generate changelog since tag v1.0.0"
            echo "  $0 --version 1.0.2 > CHANGELOG.md  # Save to file"
            ;;
        *)
            # Default: generate changelog for next version
            local last_tag=$(get_last_tag)
            local current_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null || echo "Next Version")
            generate_changelog "$last_tag" "$current_version"
            ;;
    esac
}

main "$@"
