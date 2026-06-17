# Start backend: builds jar and runs with dev profile (H2 file DB)
# Run from PowerShell: .\scripts\start-backend.ps1

Set-StrictMode -Version Latest
Push-Location (Resolve-Path "$PSScriptRoot\..")
try {
    Write-Host "Building project (skip tests)..." -ForegroundColor Green
    .\mvnw.cmd -DskipTests package

    Write-Host "Starting backend JAR with dev profile..." -ForegroundColor Green
    java -jar .\target\testnext-0.1.0-SNAPSHOT.jar --spring.profiles.active=dev
} finally {
    Pop-Location
}
