#!/bin/bash

# CFML REPL Script for Lucee Toolbox
# This script provides an easy way to launch the CFML REPL

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="$SCRIPT_DIR/target/lucee-toolbox-1.0.0.jar"

# Check if the JAR file exists
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR file not found at $JAR_PATH"
    echo "Please run 'mvn clean package' first to build the toolbox"
    exit 1
fi

# Run the REPL
echo "Starting Lucee Toolbox CFML REPL..."
java -jar "$JAR_PATH" --mode repl
