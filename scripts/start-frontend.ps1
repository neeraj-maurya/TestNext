# Start frontend: install deps (if necessary) and run Vite dev server
# Run from PowerShell: .\scripts\start-frontend.ps1

Set-StrictMode -Version Latest
Push-Location (Resolve-Path "$PSScriptRoot\..\ui")
try {
    if (-Not (Test-Path "node_modules")) {
        Write-Host "Installing npm dependencies..." -ForegroundColor Green
        npm install
    }
    Write-Host "Starting Vite dev server (npm run dev)..." -ForegroundColor Green
    npm run dev
} finally {
    Pop-Location
}
