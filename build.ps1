$env:JAVA_HOME=Join-Path $PSScriptRoot '.tools\jdk-21'
$env:GRADLE_USER_HOME=Join-Path $PSScriptRoot '.gradle-home'
& "$PSScriptRoot\gradlew.bat" -p "$PSScriptRoot" @args
exit $LASTEXITCODE
