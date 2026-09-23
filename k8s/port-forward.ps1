#Requires -Version 5.1

<#
.SYNOPSIS
    Port-forward dos servicos Kubernetes do Time Tracker para localhost.

.DESCRIPTION
    Encaminha o service time-tracker-api (e opcionalmente o redis) para portas
    locais, sem buildar nem aplicar manifests. Util quando o deploy ja foi feito
    e so e preciso expor o cluster localmente.

.PARAMETER Port
    Porta local para a API. Default: 8080

.PARAMETER Namespace
    Kubernetes namespace. Default: time-tracker

.PARAMETER Redis
    Tambem faz port-forward do service redis.

.PARAMETER RedisPort
    Porta local para o Redis (somente com -Redis). Default: 6379

.EXAMPLE
    .\k8s\port-forward.ps1

.EXAMPLE
    .\k8s\port-forward.ps1 -Port 9090

.EXAMPLE
    .\k8s\port-forward.ps1 -Redis -RedisPort 6380
#>

param (
    [int] $Port = 8080,

    [string] $Namespace = "time-tracker",

    [switch] $Redis,

    [int] $RedisPort = 6379
)

$ErrorActionPreference = "Stop"
$ApiServiceName = "time-tracker-api"
$RedisServiceName = "redis"

function Test-CommandAvailable {
    param ([string] $Name)
    return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

# Testa se a porta local esta livre antes de iniciar o port-forward
function Test-PortAvailable {
    param ([int] $PortNumber)
    $listener = $null
    try {
        $listener = [System.Net.Sockets.TcpListener]::new(
            [System.Net.IPAddress]::Loopback, $PortNumber)
        $listener.Start()
        return $true
    }
    catch {
        return $false
    }
    finally {
        if ($listener) { $listener.Stop() }
    }
}

Write-Host "== Kubernetes Port-Forward ==" -ForegroundColor Cyan

# --- Validate tools ---
if (-not (Test-CommandAvailable "kubectl")) {
    throw "kubectl nao encontrado. Instale o kubectl."
}

# --- Validate local ports ---
if (-not (Test-PortAvailable -PortNumber $Port)) {
    throw "Porta local $Port ja esta em uso. Escolha outra com -Port (ex.: -Port 9090)."
}
if ($Redis -and -not (Test-PortAvailable -PortNumber $RedisPort)) {
    throw "Porta local $RedisPort ja esta em uso. Escolha outra com -RedisPort."
}

# --- Check namespace/services exist ---
$apiService = kubectl get service $ApiServiceName -n $Namespace --ignore-not-found 2>$null
if (-not $apiService) {
    throw "Service '$ApiServiceName' nao encontrado no namespace '$Namespace'. Rode o deploy primeiro."
}

if ($Redis) {
    $redisService = kubectl get service $RedisServiceName -n $Namespace --ignore-not-found 2>$null
    if (-not $redisService) {
        throw "Service '$RedisServiceName' nao encontrado no namespace '$Namespace'."
    }
}

$redisJob = $null
try {
    if ($Redis) {
        Write-Host "`nRedis -> http://localhost:$RedisPort" -ForegroundColor Cyan
        $redisJob = Start-Job -Name "pf-redis" -ScriptBlock {
            param($ns, $port)
            kubectl port-forward -n $ns service/redis "${port}:6379"
        } -ArgumentList $Namespace, $RedisPort
    }

    Write-Host "API   -> http://localhost:$Port/api" -ForegroundColor Cyan
    Write-Host "`nPressione Ctrl+C para parar.`n" -ForegroundColor Yellow

    kubectl port-forward -n $Namespace service/$ApiServiceName "${Port}:80"
}
finally {
    if ($redisJob) {
        Stop-Job $redisJob -ErrorAction SilentlyContinue
        Remove-Job $redisJob -Force -ErrorAction SilentlyContinue
    }
}
