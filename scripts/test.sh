#!/bin/bash

# Lucee Toolbox - Test Script
# This script runs tests with the correct Java version

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
COVERAGE=false
VERBOSE=false
SPECIFIC_TEST=""
INTEGRATION_ONLY=false
UNIT_ONLY=false

# Required Java version
REQUIRED_JAVA_VERSION="17.0.12-graal"

# Function to show help
show_help() {
    echo "Lucee Toolbox Test Script"
    echo
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "Options:"
    echo "  -c, --coverage      Generate test coverage report"
    echo "  -v, --verbose       Verbose test output"
    echo "  -t, --test NAME     Run specific test class"
    echo "  -i, --integration   Run integration tests only"
    echo "  -u, --unit          Run unit tests only"
    echo "  -h, --help          Show this help message"
    echo
    echo "Examples:"
    echo "  $0                  Run all tests"
    echo "  $0 -c               Run tests with coverage"
    echo "  $0 -t BadUserServiceTest  Run specific test"
    echo "  $0 -i               Run integration tests only"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -c|--coverage)
            COVERAGE=true
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -t|--test)
            SPECIFIC_TEST="$2"
            shift 2
            ;;
        -i|--integration)
            INTEGRATION_ONLY=true
            shift
            ;;
        -u|--unit)
            UNIT_ONLY=true
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
echo -e "${BLUE}Lucee Toolbox Tests${NC}"
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
MVN_OPTIONS=""

if [ "$VERBOSE" = true ]; then
    MVN_OPTIONS="$MVN_OPTIONS -X"
fi

# Determine test goal
if [ "$COVERAGE" = true ]; then
    GOAL="clean test jacoco:report"
    echo -e "${BLUE}🧪 Running tests with coverage...${NC}"
else
    GOAL="test"
    echo -e "${BLUE}🧪 Running tests...${NC}"
fi

# Specific test selection
if [ -n "$SPECIFIC_TEST" ]; then
    MVN_OPTIONS="$MVN_OPTIONS -Dtest=$SPECIFIC_TEST"
    echo -e "${YELLOW}🎯 Running specific test: $SPECIFIC_TEST${NC}"
elif [ "$INTEGRATION_ONLY" = true ]; then
    MVN_OPTIONS="$MVN_OPTIONS -Dtest=**/*IntegrationTest"
    echo -e "${YELLOW}🔗 Running integration tests only${NC}"
elif [ "$UNIT_ONLY" = true ]; then
    MVN_OPTIONS="$MVN_OPTIONS -Dtest=**/*Test,!**/*IntegrationTest"
    echo -e "${YELLOW}🔧 Running unit tests only${NC}"
fi

# Execute Maven test
echo -e "${BLUE}Running: mvn $GOAL $MVN_OPTIONS${NC}"
echo

if ! mvn $GOAL $MVN_OPTIONS; then
    echo -e "${RED}❌ Tests failed!${NC}"
    exit 1
fi

echo
echo -e "${GREEN}✅ Tests completed successfully!${NC}"

# Show coverage report if it was generated
if [ "$COVERAGE" = true ] && [ -f "target/site/jacoco/index.html" ]; then
    echo -e "${BLUE}📊 Coverage report generated:${NC}"
    echo "open target/site/jacoco/index.html"
    echo
    
    # Show coverage summary if jacoco CSV exists
    if [ -f "target/site/jacoco/jacoco.csv" ]; then
        echo -e "${BLUE}Coverage Summary:${NC}"
        # Parse the CSV to show a simple summary
        awk -F, 'NR>1 {
            instruction_covered += $4
            instruction_total += $3 + $4
            branch_covered += $6
            branch_total += $5 + $6
            line_covered += $8
            line_total += $7 + $8
        } END {
            if (instruction_total > 0) printf "Instructions: %.1f%% (%d/%d)\n", (instruction_covered/instruction_total)*100, instruction_covered, instruction_total
            if (branch_total > 0) printf "Branches: %.1f%% (%d/%d)\n", (branch_covered/branch_total)*100, branch_covered, branch_total
            if (line_total > 0) printf "Lines: %.1f%% (%d/%d)\n", (line_covered/line_total)*100, line_covered, line_total
        }' target/site/jacoco/jacoco.csv
    fi
fi

# Show test results summary
if [ -f "target/surefire-reports/TEST-*.xml" ]; then
    echo -e "${BLUE}Test Results:${NC}"
    # Count tests from surefire reports
    TOTAL_TESTS=$(grep -h "tests=" target/surefire-reports/TEST-*.xml | sed 's/.*tests="\([0-9]*\)".*/\1/' | awk '{sum += $1} END {print sum}')
    FAILED_TESTS=$(grep -h "failures=" target/surefire-reports/TEST-*.xml | sed 's/.*failures="\([0-9]*\)".*/\1/' | awk '{sum += $1} END {print sum}')
    ERROR_TESTS=$(grep -h "errors=" target/surefire-reports/TEST-*.xml | sed 's/.*errors="\([0-9]*\)".*/\1/' | awk '{sum += $1} END {print sum}')
    
    echo "Total tests: $TOTAL_TESTS"
    echo "Failures: $FAILED_TESTS"
    echo "Errors: $ERROR_TESTS"
    echo "Success rate: $(((TOTAL_TESTS - FAILED_TESTS - ERROR_TESTS) * 100 / TOTAL_TESTS))%"
fi
