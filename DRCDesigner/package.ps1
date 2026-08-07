param(
    [string]$AppName = 'DRCDesigner',
    [string]$AppVersion = '2.0.0',
    [string]$Vendor = 'Alan Jordan',
    [string]$MainClass = 'org.alanjordan.drcdesigner.DRCDesigner',
    [ValidateSet('app-image', 'exe', 'msi')]
    [string]$PackageType = 'msi',
    [string]$SupportManifest = '',
    [string]$RequiredLayoutManifest = '',
    [string]$InstallDirName = 'DRCDesigner',
    [switch]$SkipBuild,
    [switch]$ValidateOnly
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$outRoot = Join-Path $projectRoot 'out'
$distDir = Join-Path $outRoot 'dist'
$stageDir = Join-Path $outRoot 'package-input'
$installerDir = Join-Path $outRoot 'installer'

function Ensure-Directory {
    param([string]$Path)
    if (-not (Test-Path $Path)) {
        New-Item -Path $Path -ItemType Directory | Out-Null
    }
}

function Resolve-JavaTool {
    param([string]$ToolName)

    $tool = Get-Command $ToolName -ErrorAction SilentlyContinue
    if ($tool) {
        return $tool.Source
    }

    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME "bin\$ToolName.exe"
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    throw "$ToolName was not found on PATH and JAVA_HOME was not set correctly."
}

function Stage-SupportFiles {
    param(
        [string]$ManifestPath,
        [string]$DestinationRoot
    )

    if ([string]::IsNullOrWhiteSpace($ManifestPath)) {
        Write-Host 'No support manifest provided. Continuing without extra support files.'
        return
    }

    $resolvedManifest = Resolve-Path $ManifestPath -ErrorAction Stop
    $entries = Get-Content $resolvedManifest -Raw | ConvertFrom-Json

    foreach ($entry in $entries) {
        if (-not $entry.source) {
            throw "Manifest entry missing 'source': $($entry | ConvertTo-Json -Compress)"
        }

        $sourcePath = $entry.source.ToString()
        if (-not [System.IO.Path]::IsPathRooted($sourcePath)) {
            $sourcePath = Join-Path $projectRoot $sourcePath
        }

        $resolvedSource = Resolve-Path $sourcePath -ErrorAction Stop
        $targetRel = ''
        if ($entry.target) {
            $targetRel = $entry.target.TrimStart([char[]]@('\', '/'))
        }

        $targetDir = if ($targetRel) {
            Join-Path $DestinationRoot $targetRel
        } else {
            $DestinationRoot
        }

        Ensure-Directory $targetDir

        if ((Get-Item $resolvedSource).PSIsContainer) {
            Copy-Item -Path (Join-Path $resolvedSource '*') -Destination $targetDir -Recurse -Force
            Write-Host "Staged directory $($entry.source) -> $targetRel"
        } else {
            Copy-Item -Path $resolvedSource -Destination $targetDir -Force
            Write-Host "Staged file $($entry.source) -> $targetRel"
        }
    }
}

function Test-PathRequirement {
    param(
        [string]$RootPath,
        [object]$Entry
    )

    if (-not $Entry.path) {
        throw "Layout manifest entry missing 'path': $($Entry | ConvertTo-Json -Compress)"
    }

    $entryType = 'any'
    if ($Entry.type) {
        $entryType = $Entry.type.ToString().ToLowerInvariant()
    }

    if ($entryType -notin @('any', 'file', 'directory')) {
        throw "Unsupported entry type '$($Entry.type)' for path '$($Entry.path)'."
    }

    $required = $true
    if ($null -ne $Entry.required) {
        $required = [bool]$Entry.required
    }

    $relativePath = $Entry.path.ToString().TrimStart([char[]]@('\', '/'))
    $resolvedPath = Join-Path $RootPath $relativePath
    $exists = Test-Path -Path $resolvedPath

    if (-not $exists) {
        return @{
            Valid = (-not $required)
            Message = "Missing path: $relativePath"
        }
    }

    $isLeaf = Test-Path -Path $resolvedPath -PathType Leaf
    $isContainer = Test-Path -Path $resolvedPath -PathType Container

    if ($entryType -eq 'file' -and -not $isLeaf) {
        return @{
            Valid = $false
            Message = "Expected file but found directory: $relativePath"
        }
    }

    if ($entryType -eq 'directory' -and -not $isContainer) {
        return @{
            Valid = $false
            Message = "Expected directory but found file: $relativePath"
        }
    }

    return @{
        Valid = $true
        Message = "OK: $relativePath"
    }
}

function Validate-SupportLayout {
    param(
        [string]$LayoutManifestPath,
        [string]$LayoutRoot
    )

    if ([string]::IsNullOrWhiteSpace($LayoutManifestPath)) {
        Write-Host 'No required layout manifest provided. Skipping support layout validation.'
        return
    }

    $resolvedManifest = Resolve-Path $LayoutManifestPath -ErrorAction Stop
    $entries = Get-Content $resolvedManifest -Raw | ConvertFrom-Json

    $failed = New-Object System.Collections.Generic.List[string]
    foreach ($entry in $entries) {
        $result = Test-PathRequirement -RootPath $LayoutRoot -Entry $entry
        if (-not $result.Valid) {
            $failed.Add($result.Message)
        } else {
            Write-Host $result.Message
        }
    }

    if ($failed.Count -gt 0) {
        $lines = @('Support layout validation failed:')
        foreach ($item in $failed) {
            $lines += " - $item"
        }
        $message = $lines -join [Environment]::NewLine
        throw $message
    }

    Write-Host 'Support layout validation passed.'
}

Ensure-Directory $outRoot
Ensure-Directory $distDir
Ensure-Directory $installerDir

if (Test-Path $stageDir) {
    Remove-Item -Path $stageDir -Recurse -Force
}
Ensure-Directory $stageDir

if (-not $SkipBuild) {
    Write-Host 'Running build.ps1...'
    & (Join-Path $projectRoot 'build.ps1')
}

$jarTool = Resolve-JavaTool -ToolName 'jar'
$jpackageTool = Resolve-JavaTool -ToolName 'jpackage'

$jarPath = Join-Path $distDir "$AppName.jar"
if (Test-Path $jarPath) {
    Remove-Item -Path $jarPath -Force
}

# Package the compiled classes and bundled HTML/image resources under org/.
& $jarTool --create --file $jarPath --main-class $MainClass -C $projectRoot org

Copy-Item -Path $jarPath -Destination $stageDir -Force
Stage-SupportFiles -ManifestPath $SupportManifest -DestinationRoot $stageDir
Validate-SupportLayout -LayoutManifestPath $RequiredLayoutManifest -LayoutRoot $stageDir

if ($ValidateOnly) {
    Write-Host ''
    Write-Host 'Validation-only run complete. Skipping jpackage.'
    Write-Host "Stage directory: $stageDir"
    exit 0
}

$jpackageArgs = @(
    '--name', $AppName,
    '--app-version', $AppVersion,
    '--vendor', $Vendor,
    '--input', $stageDir,
    '--main-jar', (Split-Path -Leaf $jarPath),
    '--main-class', $MainClass,
    '--dest', $installerDir,
    '--type', $PackageType
)

Write-Host "Running jpackage ($PackageType)..."

if ($PackageType -ne 'app-image') {
    $jpackageArgs += @(
        '--win-dir-chooser',
        '--win-menu',
        '--win-shortcut',
        '--install-dir', $InstallDirName
    )
}

& $jpackageTool @jpackageArgs
if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed with exit code $LASTEXITCODE"
}

Write-Host ''
Write-Host 'Packaging complete.'
Write-Host "Output directory: $installerDir"
Write-Host "Stage directory: $stageDir"
Write-Host 'Tip: pass -SupportManifest support-files.manifest.json when you are ready to include support files.'
Write-Host 'Tip: pass -RequiredLayoutManifest support-layout.requirements.json to enforce expected runtime paths before packaging.'
