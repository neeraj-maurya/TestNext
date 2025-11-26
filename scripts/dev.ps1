<#
Simple PowerShell helper for common dev tasks.
Usage:
  .\dev.ps1 -Task build
  .\dev.ps1 -Task run-dev
  .\dev.ps1 -Task compose-up
#>

param(
    [string]$Task = 'help'
)

switch ($Task.ToLower()) {
    'help' {
        Write-Host "Tasks: build, run-dev, compose-up, compose-down" -ForegroundColor Cyan
        break
    }
    'build' {
        mvn -DskipTests package
        break
    }
    'run-dev' {
        mvn -Dspring-boot.run.profiles=dev spring-boot:run
        break
    }
    'compose-up' {
        docker-compose up --build
        break
    }
    'compose-down' {
        docker-compose down
        break
    }
    default {
        Write-Host "Unknown task: $Task" -ForegroundColor Yellow
        break
    }
}
