#!/bin/bash

# Lucee Toolbox Minimal Build Script
# This creates a minimal linting-only version without heavy dependencies

echo "🔥 Lucee Toolbox Minimal Build Script"
echo "======================================"

# Make sure the script is executable
chmod +x "$0"

# Clean build
echo "🧹 Cleaning previous build..."
mvn clean

# Build with exclusions for heavy dependencies
echo "🔨 Building minimal version..."
mvn package -DskipTests \
  -Dexclude-lucee=true \
  -Dexclude-boxlang=true \
  -Dexclude-httpcomponents=true \
  -Dexclude-jsoup=true \
  -Dexclude-fastutil=true

# Create dist directory if it doesn't exist
mkdir -p dist

# Copy JAR to dist
echo "📦 Copying JAR to dist..."
cp target/lucee-toolbox-1.0.1.jar dist/lucee-toolbox-1.0.1-minimal.jar

# Display results
echo ""
echo "✅ Minimal build completed!"
echo "📋 JAR size: $(ls -lh dist/lucee-toolbox-1.0.1-minimal.jar | awk '{print $5}')"
echo "📋 JAR location: dist/lucee-toolbox-1.0.1-minimal.jar"
echo ""
echo "🧪 Testing minimal version..."
java -jar dist/lucee-toolbox-1.0.1-minimal.jar --version
