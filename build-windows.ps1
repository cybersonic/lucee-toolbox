# Windows native build script for Lucee Toolbox (PowerShell)
# This script creates a native Windows executable using GraalVM

Write-Host ""
Write-Host "🔥 Lucee Toolbox Windows Native Build Script (PowerShell)" -ForegroundColor Yellow
Write-Host "=======================================================" -ForegroundColor Yellow
Write-Host ""

# Check if native-image is available
if (-not (Get-Command "native-image" -ErrorAction SilentlyContinue)) {
    Write-Host "❌ native-image not found!" -ForegroundColor Red
    Write-Host "Please install GraalVM and native-image:" -ForegroundColor Red
    Write-Host "  1. Download GraalVM from: https://github.com/graalvm/graalvm-ce-builds/releases" -ForegroundColor Red
    Write-Host "  2. Set JAVA_HOME to GraalVM directory" -ForegroundColor Red
    Write-Host "  3. Add `$env:JAVA_HOME\bin to PATH" -ForegroundColor Red
    Write-Host "  4. Run: gu install native-image" -ForegroundColor Red
    exit 1
}

# Get version from pom.xml
try {
    $VERSION = (mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    if ($LASTEXITCODE -ne 0) {
        throw "Maven version extraction failed"
    }
    $JAR_NAME = "lucee-toolbox-$VERSION.jar"
} catch {
    Write-Host "❌ Failed to get version from pom.xml!" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}

# Ensure JAR is built
if (-not (Test-Path "target\$JAR_NAME")) {
    Write-Host "📦 Building JAR first..." -ForegroundColor Cyan
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Maven build failed!" -ForegroundColor Red
        exit 1
    }
}

Write-Host "🎯 Building for platform: windows-x64" -ForegroundColor Green
Write-Host "🔨 Creating native executable: lucee-toolbox-windows-x64.exe" -ForegroundColor Green
Write-Host "📋 Using JAR: $JAR_NAME" -ForegroundColor Green

# Create dist directory
if (-not (Test-Path "dist")) {
    New-Item -ItemType Directory -Path "dist" | Out-Null
}

# Build native executable
Write-Host "🔧 Running native-image..." -ForegroundColor Cyan
native-image `
    -jar "target\$JAR_NAME" `
    -o "dist\lucee-toolbox-windows-x64"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Native executable build failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "✅ Native executable created successfully!" -ForegroundColor Green
Write-Host "📋 Executable: dist\lucee-toolbox-windows-x64.exe" -ForegroundColor Green
Write-Host ""

# Test the executable
Write-Host "🧪 Testing native executable..." -ForegroundColor Cyan
& "dist\lucee-toolbox-windows-x64.exe" --version

Write-Host ""
Write-Host "Usage:" -ForegroundColor Yellow
Write-Host "  dist\lucee-toolbox-windows-x64.exe -i src\" -ForegroundColor Yellow
Write-Host ""

# Create a generic copy for convenience
if (Test-Path "dist\lucee-toolbox.exe") {
    Remove-Item "dist\lucee-toolbox.exe"
}
Copy-Item "dist\lucee-toolbox-windows-x64.exe" "dist\lucee-toolbox.exe"
Write-Host "📋 Generic executable created: dist\lucee-toolbox.exe" -ForegroundColor Green
Write-Host ""
