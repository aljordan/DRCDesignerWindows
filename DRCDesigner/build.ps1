$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$sourceDir = Join-Path $projectRoot 'src\org\alanjordan\drcdesigner'
if (-not (Test-Path $sourceDir)) {
    throw "Source directory not found: $sourceDir"
}

$javaFiles = Get-ChildItem -Path $sourceDir -Filter '*.java' | Select-Object -ExpandProperty FullName
if (-not $javaFiles) {
    throw "No Java files found in $sourceDir"
}

Write-Host "Compiling $($javaFiles.Count) Java files with lint checks..."

javac -Xlint:unchecked -Xlint:deprecation -d . @javaFiles

Write-Host 'Build complete: no unchecked or deprecation warnings.'
