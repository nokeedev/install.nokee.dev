#!/usr/bin/env pwsh
#Requires -version 5

# Nokee installer
# usage: (in powershell)
#  Invoke-Expression (Invoke-Webrequest <my location> -UseBasicParsing).Content

param (
    [string]$version = "main",
    [string]$installdir = ""
)

& {
    $ErrorActionPreference = 'Stop'

    function writeErrorTip($msg) {
        Write-Host $msg -BackgroundColor Red -ForegroundColor White
    }

    if (-not $env:CI) {
        $logo = @(
            '                 _                               '
            '     _ __   ___ | | _____  ___                   '
            '    | ''_ \ / _ \| |/ / _ \/ _ \                 '
            '    | | | | (_) |   <  __/  __/                  '
            '    |_| |_|\___/|_|\_\___|\___| installer        '
            '         Painless native development with Gradle '
            '                                                 '
            '   >  Manual: https://nokee.dev/getting-started  '
            '                                                 ')
        Write-Host $([string]::Join("`n", $logo)) -ForegroundColor Green
    }

    if ($IsLinux -or $IsMacOS) {
        writeErrorTip 'Install on *nix is not supported, try '
        writeErrorTip '(Use curl) "bash <(curl -fsSL https://nokee.dev/install.sh)"'
        writeErrorTip 'or'
        writeErrorTip '(Use wget) "bash <(wget https://nokee.dev/install.sh -O -)"'
        throw 'Unsupported platform'
    }

    # $temppath = ($env:TMP, $env:TEMP, "$(Get-Location)" -ne $null)[0]
    [Net.ServicePointManager]::SecurityProtocol = "tls12, tls11, tls"

    if ($null -eq $installdir -or $installdir -match '^\s*$') {
        $installdir = & {
            if ($HOME) {
                return Join-Path $HOME '.gradle\init.d'
            }
            return 'gradle'
        }
    }
    $installdir = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($installdir)

    function nokeeInstall {
        $outfile = Join-Path $installdir "nokee.init.gradle"
		New-Item "$installdir" -ItemType Directory
		$url = "https://raw.githubusercontent.com/nokeedev/init.nokee.dev/main/nokee.init.gradle"
        Write-Host "Start downloading $url .."
        try {
            Invoke-Webrequest $url -OutFile $outfile -UseBasicParsing
        } catch {
            writeErrorTip 'Download failed!'
            writeErrorTip 'Check your network'
            throw
        }
    }

    nokeeInstall
}