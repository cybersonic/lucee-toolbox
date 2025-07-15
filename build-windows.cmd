@echo off
REM Windows native build script for Lucee Toolbox
REM This script creates a native Windows executable using GraalVM

echo.
echo 🔥 Lucee Toolbox Windows Native Build Script
echo =============================================
echo.

REM Check if native-image is available
where native-image >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ native-image not found!
    echo Please install GraalVM and native-image:
    echo   1. Download GraalVM from: https://github.com/graalvm/graalvm-ce-builds/releases
    echo   2. Set JAVA_HOME to GraalVM directory
    echo   3. Add %%JAVA_HOME%%\bin to PATH
    echo   4. Run: gu install native-image
    exit /b 1
)

REM Ensure JAR is built
if not exist "target\lucee-toolbox-1.0.0.jar" (
    echo 📦 Building JAR first...
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo ❌ Maven build failed!
        exit /b 1
    )
)

echo 🎯 Building for platform: windows-x64
echo 🔨 Creating native executable: lucee-toolbox-windows-x64.exe

REM Build native executable
native-image ^
    -jar target\lucee-toolbox-1.0.0.jar ^
    -o lucee-toolbox-windows-x64

if %errorlevel% neq 0 (
    echo ❌ Native executable build failed!
    exit /b 1
)

echo.
echo ✅ Native executable created successfully!
echo 📋 Executable: lucee-toolbox-windows-x64.exe
echo.

REM Test the executable
echo 🧪 Testing native executable...
lucee-toolbox-windows-x64.exe --version

echo.
echo Usage:
echo   lucee-toolbox-windows-x64.exe -i src\
echo.

REM Create a generic copy for convenience
if exist "lucee-toolbox.exe" del "lucee-toolbox.exe"
copy "lucee-toolbox-windows-x64.exe" "lucee-toolbox.exe"
echo 📋 Generic executable created: lucee-toolbox.exe
echo.
