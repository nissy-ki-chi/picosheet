# JPicosheet のビルドとテスト実行スクリプト
#
# 使い方:
#   .\build-test.cmd               ビルドして AllTests を実行
#   .\build-test.cmd -BuildOnly    ビルドのみ
#   .\build-test.cmd -Test test.CellTest   指定したテストクラスのみ実行
#
# JDK は JAVA_HOME を優先し、なければ VS Code の Java 拡張機能 (redhat.java) に付属する JDK を使います。
# JUnit も同じ拡張機能に付属する JUnit 4 / Hamcrest の jar を使います。
# クラスファイルは build\classes に作られます。
# テストはプロジェクトのフォルダで実行します (XMLReaderTest などが src\test のファイルを相対パスで読み書きするため)。

param(
	[switch]$BuildOnly,
	[string]$Test = "test.AllTests"
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$buildDir = Join-Path $root "build"
$classesDir = Join-Path $buildDir "classes"

# 最新バージョンの redhat.java 拡張機能を探す
$extDir = Get-ChildItem "$env:USERPROFILE\.vscode\extensions" -Directory -Filter "redhat.java-*" -ErrorAction SilentlyContinue |
	Sort-Object { [version]($_.Name -replace '^redhat\.java-(\d+\.\d+\.\d+).*$', '$1') } -Descending |
	Select-Object -First 1

# JDK を探す
$jdkBin = $null
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\javac.exe"))) {
	$jdkBin = Join-Path $env:JAVA_HOME "bin"
} elseif ($extDir) {
	$javac = Get-ChildItem (Join-Path $extDir.FullName "jre") -Recurse -Filter "javac.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
	if ($javac) { $jdkBin = $javac.DirectoryName }
}
if (-not $jdkBin) {
	Write-Error "JDK が見つかりません。JAVA_HOME を設定するか、VS Code に Extension Pack for Java を入れてください。"
}

# JUnit 4 と Hamcrest の jar を探す
if (-not $extDir) {
	Write-Error "VS Code の Java 拡張機能が見つからないため、JUnit の jar を用意できません。"
}
$plugins = Join-Path $extDir.FullName "server\plugins"
$junitJar = Get-ChildItem $plugins -Filter "org.junit_4*.jar" | Sort-Object Name -Descending | Select-Object -First 1
$hamcrestJar = Get-ChildItem $plugins -Filter "org.hamcrest*.jar" | Where-Object { $_.Name -match '^org\.hamcrest_[\d.]+\.jar$' } | Sort-Object Name -Descending | Select-Object -First 1
if (-not $junitJar -or -not $hamcrestJar) {
	Write-Error "JUnit 4 または Hamcrest の jar が $plugins に見つかりません。"
}
$libCp = "$($junitJar.FullName);$($hamcrestJar.FullName)"

Write-Host "JDK   : $jdkBin"
Write-Host "JUnit : $($junitJar.Name), $($hamcrestJar.Name)"

# ビルド
if (Test-Path $classesDir) { Remove-Item -Recurse -Force $classesDir }
New-Item -ItemType Directory -Force $classesDir | Out-Null

$sources = Get-ChildItem (Join-Path $root "src") -Recurse -Filter "*.java" | ForEach-Object { '"' + ($_.FullName -replace '\\', '/') + '"' }
$srcList = Join-Path $buildDir "sources.txt"
[System.IO.File]::WriteAllLines($srcList, $sources)

Write-Host "Compiling $($sources.Count) files..."
& (Join-Path $jdkBin "javac.exe") "-J-Duser.language=en" -encoding UTF-8 -nowarn -d $classesDir -cp $libCp "@$srcList"
if ($LASTEXITCODE -ne 0) {
	Write-Host "ビルドに失敗しました。" -ForegroundColor Red
	exit $LASTEXITCODE
}
Write-Host "ビルド成功" -ForegroundColor Green

if ($BuildOnly) { exit 0 }

# テスト実行
Push-Location $root
try {
	& (Join-Path $jdkBin "java.exe") "-Duser.language=en" "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" -cp "$classesDir;$libCp" junit.textui.TestRunner $Test
	$testExit = $LASTEXITCODE
} finally {
	Pop-Location
}
if ($testExit -eq 0) {
	Write-Host "テスト成功" -ForegroundColor Green
} else {
	Write-Host "テストに失敗があります。" -ForegroundColor Red
}
exit $testExit
