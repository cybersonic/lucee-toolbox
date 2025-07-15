#!/bin/bash

# Release automation script for Lucee Toolbox
# This script automates the entire release process including version bumping,
# tagging, and GitHub release creation

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required tools are available
check_requirements() {
    print_status "Checking requirements..."
    
    if ! command -v git &> /dev/null; then
        print_error "git is required but not installed."
        exit 1
    fi
    
    if ! command -v gh &> /dev/null; then
        print_error "GitHub CLI (gh) is required but not installed."
        print_error "Install with: brew install gh"
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is required but not installed."
        exit 1
    fi
    
    # Check if gh is authenticated
    if ! gh auth status &> /dev/null; then
        print_error "GitHub CLI is not authenticated. Run: gh auth login"
        exit 1
    fi
    
    print_success "All requirements met"
}

# Function to validate semantic version
validate_version() {
    local version=$1
    if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        print_error "Invalid version format: $version"
        print_error "Version must be in format: X.Y.Z (e.g., 1.0.0)"
        exit 1
    fi
}

# Function to get the current version
get_current_version() {
    mvn help:evaluate -Dexpression=project.version -q -DforceStdout
}

# Function to suggest next version
suggest_next_version() {
    local current_version=$1
    local major=$(echo $current_version | cut -d. -f1)
    local minor=$(echo $current_version | cut -d. -f2)
    local patch=$(echo $current_version | cut -d. -f3)
    
    local next_patch=$((patch + 1))
    local next_minor=$((minor + 1))
    local next_major=$((major + 1))
    
    echo "Current version: $current_version"
    echo ""
    echo "Suggested versions:"
    echo "  Patch (bug fixes): $major.$minor.$next_patch"
    echo "  Minor (new features): $major.$next_minor.0"
    echo "  Major (breaking changes): $next_major.0.0"
}

# Function to check if working directory is clean
check_git_status() {
    if [[ -n $(git status --porcelain) ]]; then
        print_error "Working directory is not clean. Please commit or stash changes first."
        git status --short
        exit 1
    fi
    
    # Check if we're on main branch
    local current_branch=$(git branch --show-current)
    if [[ "$current_branch" != "main" ]]; then
        print_warning "You are not on the main branch (current: $current_branch)"
        read -p "Do you want to continue? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_error "Release cancelled"
            exit 1
        fi
    fi
}

# Function to update version
update_version() {
    local new_version=$1
    print_status "Updating version to $new_version..."
    
    # Use the existing version.sh script
    ./version.sh "$new_version"
    
    print_success "Version updated to $new_version"
}

# Function to run tests
run_tests() {
    print_status "Running tests..."
    mvn clean test
    print_success "Tests passed"
}

# Function to create git tag and commit
create_git_tag() {
    local version=$1
    local tag_name="v$version"
    
    print_status "Creating git commit and tag..."
    
    # Add all changed files
    git add .
    
    # Create commit
    git commit -m "Bump version to $version"
    
    # Create tag
    git tag -a "$tag_name" -m "Release $version"
    
    print_success "Created commit and tag $tag_name"
}

# Function to push to GitHub
push_to_github() {
    local version=$1
    local tag_name="v$version"
    
    print_status "Pushing to GitHub..."
    
    # Push commit
    git push origin main
    
    # Push tag
    git push origin "$tag_name"
    
    print_success "Pushed to GitHub"
}

# Function to create GitHub release
create_github_release() {
    local version=$1
    local tag_name="v$version"
    
    print_status "Creating GitHub release..."
    
    # Generate changelog from git commits since last tag
    local last_tag=$(git describe --tags --abbrev=0 HEAD^ 2>/dev/null || echo "")
    local changelog=""
    
    if [[ -n "$last_tag" ]]; then
        changelog=$(git log ${last_tag}..HEAD --pretty=format:"- %s" --no-merges)
    else
        changelog=$(git log --pretty=format:"- %s" --no-merges -n 10)
    fi
    
    # Create release notes
    local release_notes="## Changes in this release

$changelog

## Installation

### Native Binaries
Download the appropriate binary for your platform from the assets below:

- **Linux**: \`lucee-toolbox-linux-x64\`
- **macOS Intel**: \`lucee-toolbox-macos-x64\`
- **macOS Apple Silicon**: \`lucee-toolbox-macos-arm64\`
- **Windows**: \`lucee-toolbox-windows-x64.exe\`

### Usage
\`\`\`bash
# Make executable (Linux/macOS)
chmod +x lucee-toolbox-*

# Test the binary
./lucee-toolbox-linux-x64 --version

# Lint CFML files
./lucee-toolbox-linux-x64 -i src/

# Format CFML files
./lucee-toolbox-linux-x64 -i src/ --format

# Process from stdin
echo 'dump(server);' | ./lucee-toolbox-linux-x64 -
\`\`\`

## Build Information
- Built with GraalVM native-image
- Java 17 compatible
- No external dependencies required"

    # Create the release
    gh release create "$tag_name" \
        --title "Lucee Toolbox $version" \
        --notes "$release_notes" \
        --latest
    
    print_success "Created GitHub release $tag_name"
    print_status "GitHub Actions will automatically build and upload binaries"
}

# Function to wait for GitHub Actions to complete
wait_for_build() {
    local tag_name=$1
    print_status "Waiting for GitHub Actions to build binaries..."
    
    # Wait a bit for the workflow to start
    sleep 10
    
    # Check for workflow run
    local max_attempts=30
    local attempt=0
    
    while [[ $attempt -lt $max_attempts ]]; do
        local run_id=$(gh run list --event=release --limit=1 --json id --jq '.[0].id' 2>/dev/null || echo "")
        
        if [[ -n "$run_id" ]]; then
            print_status "Found workflow run: $run_id"
            gh run watch $run_id
            break
        fi
        
        attempt=$((attempt + 1))
        print_status "Waiting for workflow to start... ($attempt/$max_attempts)"
        sleep 5
    done
    
    if [[ $attempt -eq $max_attempts ]]; then
        print_warning "Workflow monitoring timed out. Check GitHub Actions manually."
    fi
}

# Main release function
main() {
    print_status "Starting release process for Lucee Toolbox"
    
    # Check requirements
    check_requirements
    
    # Get current version
    local current_version=$(get_current_version)
    
    # If no version argument provided, show suggestions
    if [[ -z "$1" ]]; then
        suggest_next_version "$current_version"
        echo ""
        read -p "Enter new version: " new_version
        
        if [[ -z "$new_version" ]]; then
            print_error "No version provided"
            exit 1
        fi
    else
        local new_version=$1
    fi
    
    # Validate version format
    validate_version "$new_version"
    
    # Check if version is actually newer
    if [[ "$new_version" == "$current_version" ]]; then
        print_error "New version ($new_version) is the same as current version ($current_version)"
        exit 1
    fi
    
    # Check git status
    check_git_status
    
    # Confirm release
    echo ""
    print_status "Release Summary:"
    echo "  Current Version: $current_version"
    echo "  New Version: $new_version"
    echo "  Branch: $(git branch --show-current)"
    echo "  Repository: $(git remote get-url origin)"
    echo ""
    
    read -p "Proceed with release? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_error "Release cancelled"
        exit 1
    fi
    
    # Execute release steps
    update_version "$new_version"
    run_tests
    create_git_tag "$new_version"
    push_to_github "$new_version"
    create_github_release "$new_version"
    
    print_success "Release $new_version completed successfully!"
    print_status "GitHub Actions is building the native binaries..."
    print_status "Monitor progress at: https://github.com/cybersonic/lucee-toolbox/actions"
    
    # Optional: Wait for build to complete
    read -p "Wait for build to complete? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        wait_for_build "v$new_version"
    fi
    
    print_success "Release process completed!"
    print_status "Release URL: https://github.com/cybersonic/lucee-toolbox/releases/tag/v$new_version"
}

# Handle script arguments
case "$1" in
    --help|-h)
        echo "Usage: $0 [version]"
        echo ""
        echo "Examples:"
        echo "  $0           # Interactive mode with version suggestions"
        echo "  $0 1.0.2     # Release version 1.0.2"
        echo "  $0 --help    # Show this help"
        echo ""
        echo "This script will:"
        echo "  1. Update version in all files"
        echo "  2. Run tests"
        echo "  3. Create git commit and tag"
        echo "  4. Push to GitHub"
        echo "  5. Create GitHub release"
        echo "  6. GitHub Actions will build and upload binaries"
        exit 0
        ;;
    *)
        main "$@"
        ;;
esac
