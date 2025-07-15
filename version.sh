#!/bin/bash

# Version management script for Lucee Toolbox
# Usage: ./version.sh [new_version]
# Example: ./version.sh 1.0.1

set -e

CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

if [ -z "$1" ]; then
    echo "Current version: $CURRENT_VERSION"
    echo ""
    echo "Usage: ./version.sh [new_version]"
    echo "Example: ./version.sh 1.0.1"
    echo ""
    echo "Version guidelines:"
    echo "  - Patch (1.0.1): Bug fixes, minor improvements"
    echo "  - Minor (1.1.0): New features, backward-compatible changes"
    echo "  - Major (2.0.0): Breaking changes, major rewrites"
    exit 0
fi

NEW_VERSION=$1

echo "Updating version from $CURRENT_VERSION to $NEW_VERSION..."

# Update pom.xml
mvn versions:set -DnewVersion=$NEW_VERSION -DgenerateBackupPoms=false

# Update files that reference the version
sed -i.bak "s/$CURRENT_VERSION/$NEW_VERSION/g" README.md && rm README.md.bak
sed -i.bak "s/$CURRENT_VERSION/$NEW_VERSION/g" DEVELOPMENT.md && rm DEVELOPMENT.md.bak
sed -i.bak "s/$CURRENT_VERSION/$NEW_VERSION/g" BUILD.md && rm BUILD.md.bak
sed -i.bak "s/$CURRENT_VERSION/$NEW_VERSION/g" src/main/resources/lucee-toolbox.json && rm src/main/resources/lucee-toolbox.json.bak
sed -i.bak "s/$CURRENT_VERSION/$NEW_VERSION/g" src/main/java/org/lucee/toolbox/LuceeToolbox.java && rm src/main/java/org/lucee/toolbox/LuceeToolbox.java.bak
sed -i.bak "s/$CURRENT_VERSION/$NEW_VERSION/g" src/main/java/org/lucee/toolbox/repl/CFMLRepl.java && rm src/main/java/org/lucee/toolbox/repl/CFMLRepl.java.bak
sed -i.bak "s/$CURRENT_VERSION/$NEW_VERSION/g" src/main/java/org/lucee/toolbox/cli/CommandLineInterface.java && rm src/main/java/org/lucee/toolbox/cli/CommandLineInterface.java.bak
sed -i.bak "s/$CURRENT_VERSION/$NEW_VERSION/g" build.sh && rm build.sh.bak
sed -i.bak "s/$CURRENT_VERSION/$NEW_VERSION/g" build-native.sh && rm build-native.sh.bak
sed -i.bak "s/$CURRENT_VERSION/$NEW_VERSION/g" build-windows.cmd && rm build-windows.cmd.bak
sed -i.bak "s/$CURRENT_VERSION/$NEW_VERSION/g" cfml-repl.sh && rm cfml-repl.sh.bak

echo "✅ Version updated successfully!"
echo ""
echo "Next steps:"
echo "1. Test your changes: mvn clean package"
echo "2. Commit changes: git add . && git commit -m 'Bump version to $NEW_VERSION'"
echo "3. Create a GitHub release with tag v$NEW_VERSION"
echo ""
echo "The GitHub Actions workflow will automatically build binaries for the new version."
