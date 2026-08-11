#Requires -Version 5.1

<#
.SYNOPSIS
    Build, push, deploy and expose the Time Tracker API on Kubernetes.

.DESCRIPTION
    Automates Docker image build/push, applies Kubernetes manifests,
    waits for rollout and optionally starts port-forward.

.PARAMETER Image
    Full Docker image name, including registry and tag.
    Example: vmarcante/time-tracker-api:v1.0.0

.PARAMETER Registry
    Docker registry or user. Default: vmarcante

.PARAMETER Repository
    Image repository name. Default: time-tracker-api

.PARAMETER Version
    Image tag. If omitted, uses the latest Git tag or short commit hash.

.PARAMETER Namespace
    Kubernetes namespace. Default: time-tracker

.PARAMETER Port
    Local port for port-forward. Default: 8080

.PARAMETER SkipBuild
    Skip Docker build and push. Useful when the image is already in the registry.

.PARAMETER SkipPortForward
    Skip port-forward after deployment.

.EXAMPLE
    .\k8s\deploy.ps1

.EXAMPLE
    .\k8s\deploy.ps1 -Registry "meu-registry" -Version "v1.2.3"

.EXAMPLE
    .\k8s\deploy.ps1 -Image "vmarcante/time-tracker-api:v1.2.3" -SkipPortForward
#>

param (
    [string] $Image,

    [string] $Registry = "vmarcante",

    [string] $Repository = "time-tracker-api",

    [string] $Version,

    [string] $Namespace = "time-tracker",

    [int] $Port = 8080,

    [switch] $SkipBuild,

    [switch] $SkipPortForward
)

$ErrorActionPreference = "Stop"
$DeploymentName = "time-tracker-api"

function Test-CommandAvailable {
    param ([string] $Name)
    return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

function Get-GitVersion {
    try {
        $tag = git describe --tags --always 2>$null
        if ($LASTEXITCODE -eq 0 -and $tag) {
            return $tag
        }
    }
    catch { }

    try {
        $sha = git rev-parse --short HEAD 2>$null
        if ($LASTEXITCODE -eq 0 -and $sha) {
            return $sha
        }
    }
    catch { }

    return "latest"
}

Write-Host "== Kubernetes Deploy Script ==" -ForegroundColor Cyan

# --- Resolve image name ---
if (-not $Image) {
    if (-not $Version) {
        $Version = Get-GitVersion
        Write-Host "Versao detectada pelo Git: $Version" -ForegroundColor Yellow
    }
    $Image = "$Registry/$Repository`:$Version"
}

Write-Host "Imagem: $Image" -ForegroundColor Cyan

# --- Validate tools ---
if (-not (Test-CommandAvailable "docker")) {
    throw "Docker nao encontrado. Instale o Docker Desktop."
}
if (-not (Test-CommandAvailable "kubectl")) {
    throw "kubectl nao encontrado. Instale o kubectl."
}

# --- Build & Push ---
if (-not $SkipBuild) {
    Write-Host "`n[1/5] Buildando imagem: $Image" -ForegroundColor Cyan
    docker build -t $Image -f "$PSScriptRoot/../docker/Dockerfile" "$PSScriptRoot/.."

    Write-Host "`n[2/5] Enviando imagem para o registry" -ForegroundColor Cyan
    docker push $Image
}
else {
    Write-Host "`n[1/5] Build pulado. Usando imagem: $Image" -ForegroundColor Yellow
}

# --- Apply manifests ---
Write-Host "`n[3/5] Aplicando manifests Kubernetes" -ForegroundColor Cyan
kubectl apply -k $PSScriptRoot

# --- Update image on deployment ---
Write-Host "`n[4/5] Atualizando imagem do deployment" -ForegroundColor Cyan
kubectl set image deployment/$DeploymentName api=$Image -n $Namespace

# --- Wait for rollout ---
Write-Host "`n[5/5] Aguardando pods ficarem prontos" -ForegroundColor Cyan
kubectl rollout status deployment/$DeploymentName -n $Namespace --timeout=180s

Write-Host "`nDeploy concluido com sucesso!" -ForegroundColor Green

# --- Port-forward ---
if (-not $SkipPortForward) {
    Write-Host "`nIniciando port-forward em http://localhost:$Port/api" -ForegroundColor Cyan
    Write-Host "Pressione Ctrl+C para parar.`n" -ForegroundColor Yellow
    kubectl port-forward -n $Namespace service/$DeploymentName ${Port}:80
}
else {
    Write-Host "`nPort-forward pulado. Para testar localmente, execute:" -ForegroundColor Yellow
    Write-Host "  kubectl port-forward -n $Namespace service/$DeploymentName ${Port}:80" -ForegroundColor White
}
