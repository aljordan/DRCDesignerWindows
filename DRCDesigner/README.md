# DRCDesigner Build and Packaging Guide

This document describes the required tools and commands to:
- build the Java project
- run the application locally
- package a standalone Windows MSI installer

## 1) Required Software

### Operating system
- Windows 10 or Windows 11

### Java
- JDK 21 or newer (required for compile and jpackage)
- `javac`, `jar`, and `jpackage` must be available (on PATH or via `JAVA_HOME`)

Notes:
- JRE alone is not enough to build or package.
- End users do not need to install Java when using the packaged MSI, because jpackage bundles a runtime.

### WiX Toolset (for MSI/exe installer types)
- WiX v3.x (for example 3.14.x)
- `candle.exe` and `light.exe` must be on PATH

Notes:
- `app-image` packaging does not require WiX.
- `msi` and `exe` packaging with current jpackage flow require WiX tools.

## 2) Repository Layout (key files)

- `build.ps1`: compiles all Java files with lint checks
- `package.ps1`: stages files, validates optional layout rules, and calls jpackage
- `files`: arg file list used by local compile workflows
- `support-files.manifest.example.json`: example source-to-target staging manifest
- `support-layout.requirements.example.json`: optional staged layout validation manifest

## 3) Build the Project

From repository root:

```powershell
.\build.ps1
```

What it does:
- Compiles sources in `src\org\alanjordan\drcdesigner`
- Emits class files under project root package path (`org\...`)
- Enables lint checks (`-Xlint:unchecked -Xlint:deprecation`)

## 4) Run the App Locally (from classes)

After a successful build:

```powershell
java -cp . org.alanjordan.drcdesigner.DRCDesigner
```

## 5) Create Packaged Outputs with package.ps1

### Build MSI (default)

```powershell
.\package.ps1 -PackageType msi -AppVersion 1.0.0
```

### Build EXE installer

```powershell
.\package.ps1 -PackageType exe -AppVersion 1.0.0
```

### Build app-image only (no installer)

```powershell
.\package.ps1 -PackageType app-image -AppVersion 1.0.0
```

Output locations:
- Installer/app-image output: `out\installer`
- Temporary staging input: `out\package-input`
- Generated app JAR: `out\dist\DRCDesigner.jar`

## 6) Include Supporting Files During Packaging

If support files are outside compiled Java resources, provide a manifest:

```powershell
.\package.ps1 -PackageType msi -SupportManifest .\support-files.manifest.json
```

Manifest format is an array of objects:

```json
[
  { "source": "path/in/repo", "target": "path/in/package" }
]
```

## 7) Validate Staged Runtime Layout Before Packaging

To enforce expected runtime paths in staged output:

```powershell
.\package.ps1 -ValidateOnly -RequiredLayoutManifest .\support-layout.requirements.example.json
```

Or with full packaging:

```powershell
.\package.ps1 -PackageType msi -RequiredLayoutManifest .\support-layout.requirements.example.json
```

## 8) Common Troubleshooting

### jpackage cannot find WiX tools
Error mentions `candle.exe` / `light.exe` not found.

Fix:
- Install WiX v3.x
- Add WiX `bin` folder to PATH
- Open a new terminal and verify:

```powershell
candle -?
light -?
```

### Java tools not found
If `javac`, `jar`, or `jpackage` is missing:
- Install JDK 21+
- Set `JAVA_HOME`
- Ensure `%JAVA_HOME%\bin` is on PATH

### PowerShell argfile behavior
For direct manual javac usage, quote argfiles if needed to avoid PowerShell parsing issues.

## 9) Quick End-to-End Example

```powershell
# 1) Compile
.\build.ps1

# 2) Run locally
java -cp . org.alanjordan.drcdesigner.DRCDesigner

# 3) Package MSI
.\package.ps1 -PackageType msi -AppVersion 1.0.0
```

## 10) Repeatable MSI Build Verification (copy/paste)

Use this when preparing a new release to compile, package, and confirm the MSI exists:

```powershell
$version = "1.0.13"

# 1) Compile all sources with lint checks
.\build.ps1

# 2) Build/update the runnable JAR used by jpackage
$jar = (Get-Command jar -ErrorAction SilentlyContinue).Source
if (-not $jar -and $env:JAVA_HOME) {
  $candidate = Join-Path $env:JAVA_HOME "bin\jar.exe"
  if (Test-Path $candidate) { $jar = $candidate }
}
if (-not $jar) { throw "jar tool not found." }
if (-not (Test-Path "out\dist")) { New-Item -Path "out\dist" -ItemType Directory | Out-Null }
& $jar --create --file "out\dist\DRCDesigner.jar" --main-class "org.alanjordan.drcdesigner.DRCDesigner" -C . org

# 3) Refresh jpackage input JAR
if (-not (Test-Path "out\package-input")) { New-Item -Path "out\package-input" -ItemType Directory | Out-Null }
Copy-Item -Path "out\dist\DRCDesigner.jar" -Destination "out\package-input\DRCDesigner.jar" -Force

# 4) Package MSI to out\installer-fixed
$jpackage = (Get-Command jpackage -ErrorAction SilentlyContinue).Source
if (-not $jpackage -and $env:JAVA_HOME) {
  $candidate2 = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
  if (Test-Path $candidate2) { $jpackage = $candidate2 }
}
if (-not $jpackage) { throw "jpackage not found." }
$dest = Join-Path (Get-Location) "out\installer-fixed"
if (-not (Test-Path $dest)) { New-Item -Path $dest -ItemType Directory | Out-Null }

& $jpackage --name DRCDesigner --app-version $version --vendor "Alan Jordan" --input "out\package-input" --main-jar "DRCDesigner.jar" --main-class "org.alanjordan.drcdesigner.DRCDesigner" --dest $dest --type msi --win-dir-chooser --win-menu --win-shortcut --install-dir DRCDesigner
if ($LASTEXITCODE -ne 0) { throw "jpackage MSI build failed with exit code $LASTEXITCODE" }

# 5) Verify expected artifact exists
$msi = Join-Path $dest "DRCDesigner-$version.msi"
if (Test-Path $msi) {
  Write-Host "MSI ready: $msi"
} else {
  throw "Expected MSI not found: $msi"
}
```

Optional quick check of all packaged MSI files:

```powershell
Get-ChildItem .\out\installer-fixed\DRCDesigner-*.msi | Select-Object Name, LastWriteTime, Length
```
