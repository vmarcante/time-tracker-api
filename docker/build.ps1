#!/usr/bin/env pwsh
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("local", "dev", "prod")]
    [string]$Environment,

    [switch]$OnlyBuild
)

$ErrorActionPreference = "Stop"

$envFile = ".env.$Environment"
$composeFile = "docker-compose.$Environment.yml"

if (-not (Test-Path $envFile)) {
    Write-Error "Environment file not found: $envFile`nCreate it from the example: Copy-Item \"$envFile.example\" \"$envFile\""
}

if (-not (Test-Path $composeFile)) {
    Write-Error "Compose file not found: $composeFile"
}

Write-Host "Using environment: $Environment"
Write-Host "Compose file: $composeFile"
Write-Host "Environment file: $envFile"

if ($OnlyBuild) {
    Write-Host "Building Docker image..."
    docker-compose -f $composeFile build
} else {
    Write-Host "Building and starting container..."
    docker-compose -f $composeFile up --build
}
