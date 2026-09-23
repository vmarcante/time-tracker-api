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
    Nome/tag da imagem definido manualmente (prioridade 1).

.PARAMETER VersionStrategy
    Estrategia de resolucao automatica do nome quando -Version nao e informado.
    Valores: Auto (padrao), Tag, Timestamp, Sha, Latest.
    Auto aplica a prioridade: Tag > Timestamp > Sha > Latest.

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
    .\k8s\deploy.ps1 -Version "v1.2.3"

.EXAMPLE
    .\k8s\deploy.ps1 -VersionStrategy Timestamp

.EXAMPLE
    .\k8s\deploy.ps1 -Image "vmarcante/time-tracker-api:v1.2.3" -SkipPortForward
#>

param (
    [string] $Image,

    [string] $Registry = "vmarcante",

    [string] $Repository = "time-tracker-api",

    # Prioridade 1: nome/tag manual
    [string] $Version,

    # Estrategia de auto-deteccao (usado quando -Version nao e informado)
    [ValidateSet("Auto", "Tag", "Timestamp", "Sha", "Latest")]
    [string] $VersionStrategy = "Auto",

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

# Retorna a tag Git exata do HEAD (nao usa git describe, que pode retornar v1.0-3-gabc)
function Get-GitTag {
    try {
        [string]$tag = git tag --points-at HEAD 2>$null | Select-Object -First 1
        if ($LASTEXITCODE -eq 0 -and $tag) { return $tag.Trim() }
    }
    catch { }
    return $null
}

# Retorna o SHA curto do commit atual
function Get-GitSha {
    try {
        [string]$sha = git rev-parse --short HEAD 2>$null
        if ($LASTEXITCODE -eq 0 -and $sha) { return $sha.Trim() }
    }
    catch { }
    return $null
}

# Resolve o nome da versao conforme a estrategia escolhida.
# Prioridade no modo Auto: 2.Tag > 3.Horario > 4.Sha > 5.Latest
function Resolve-ImageVersion {
    param (
        [ValidateSet("Auto", "Tag", "Timestamp", "Sha", "Latest")]
        [string]$Strategy
    )

    switch ($Strategy) {

        "Tag" {
            [string]$tag = Get-GitTag
            if ($tag) {
                Write-Host "Versao: tag '$tag'" -ForegroundColor Green
                return $tag
            }
            throw "Nenhuma tag Git encontrada no HEAD. Crie uma tag ou use -VersionStrategy Auto."
        }

        "Timestamp" {
            [string]$ts = Get-Date -Format "yyyyMMdd-HHmm"
            Write-Host "Versao: horario '$ts'" -ForegroundColor Cyan
            return $ts
        }

        "Sha" {
            [string]$sha = Get-GitSha
            if ($sha) {
                Write-Host "Versao: SHA Git '$sha'" -ForegroundColor Cyan
                return $sha
            }
            throw "SHA Git nao disponivel. Verifique se o repositorio Git esta inicializado."
        }

        "Latest" {
            Write-Host "Versao: 'latest'" -ForegroundColor Yellow
            return "latest"
        }

        default { # Auto — aplica prioridade completa
            # 2. Tag Git exata no HEAD
            [string]$tag = Get-GitTag
            if ($tag) {
                Write-Host "Versao (auto): tag '$tag'" -ForegroundColor Green
                return $tag
            }

            # 3. Horario
            try {
                [string]$ts = Get-Date -Format "yyyyMMdd-HHmm"
                Write-Host "Versao (auto): horario '$ts' (sem tag)" -ForegroundColor Yellow
                return $ts
            }
            catch { }

            # 4. SHA Git
            [string]$sha = Get-GitSha
            if ($sha) {
                Write-Host "Versao (auto): SHA '$sha' (sem tag, sem horario)" -ForegroundColor Yellow
                return $sha
            }

            # 5. Latest
            Write-Host "Versao (auto): 'latest' (fallback final)" -ForegroundColor Red
            return "latest"
        }
    }
}

Write-Host "== Kubernetes Deploy Script ==" -ForegroundColor Cyan

# --- Resolve image name ---
if (-not $Image) {
    if ($Version) {
        # Prioridade 1: nome manual via -Version
        Write-Host "Versao manual: '$Version'" -ForegroundColor Cyan
    }
    else {
        $Version = Resolve-ImageVersion -Strategy $VersionStrategy
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
