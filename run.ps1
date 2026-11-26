Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "Starting Cloud ERP (Spring Boot + React Vite)" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

$root = $PSScriptRoot
$backendDir = Join-Path $root "cloud_erp\backend-java"
$frontendDir = Join-Path $root "cloud_erp\frontend"
$jarPath = Join-Path $backendDir "target\cloud-erp-backend-1.0.0.jar"

Write-Host "Starting Spring Boot Backend on http://127.0.0.1:8000 ..." -ForegroundColor Green
$backendProcess = Start-Process -FilePath "java" -ArgumentList "-jar", "`"$jarPath`"" -WorkingDirectory $backendDir -PassThru

Start-Sleep -Seconds 3

Write-Host "Starting Vite Frontend on http://localhost:5173 ..." -ForegroundColor Green
$frontendProcess = Start-Process -FilePath "npm.cmd" -ArgumentList "run dev" -WorkingDirectory $frontendDir -PassThru

Write-Host ""
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "Backend API : http://127.0.0.1:8000" -ForegroundColor Yellow
Write-Host "Frontend UI : http://localhost:5173" -ForegroundColor Yellow
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop both servers." -ForegroundColor Gray

try {
    while ($true) {
        if ($backendProcess.HasExited -or $frontendProcess.HasExited) {
            break
        }
        Start-Sleep -Seconds 1
    }
} finally {
    if (-not $backendProcess.HasExited) { Stop-Process -Id $backendProcess.Id -Force }
    if (-not $frontendProcess.HasExited) { Stop-Process -Id $frontendProcess.Id -Force }
}
