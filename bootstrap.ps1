$ErrorActionPreference='Stop'
$projectRoot=$PSScriptRoot
$toolRoot=Join-Path $projectRoot '.tools'
New-Item -ItemType Directory -Path $toolRoot -Force | Out-Null
if (!(Test-Path "$toolRoot\jdk-21\bin\javac.exe")) {
  $release=Invoke-RestMethod 'https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse'
  Invoke-WebRequest $release[0].binary.package.link -OutFile "$toolRoot\jdk.zip"
  Expand-Archive -LiteralPath "$toolRoot\jdk.zip" -DestinationPath "$toolRoot\jdk-unpack" -Force
  $jdkFolder=Get-ChildItem "$toolRoot\jdk-unpack" -Directory | Select-Object -First 1
  Move-Item -LiteralPath $jdkFolder.FullName -Destination "$toolRoot\jdk-21"
}
New-Item -ItemType Directory -Path "$projectRoot\gradle\wrapper" -Force | Out-Null
if (!(Test-Path "$projectRoot\gradle\wrapper\gradle-wrapper.jar")) {
  Invoke-WebRequest 'https://raw.githubusercontent.com/FabricMC/fabric-example-mod/1.21/gradle/wrapper/gradle-wrapper.jar' -OutFile "$projectRoot\gradle\wrapper\gradle-wrapper.jar"
}
Invoke-WebRequest 'https://raw.githubusercontent.com/FabricMC/fabric-example-mod/1.21/gradlew.bat' -OutFile "$projectRoot\gradlew.bat"
Write-Output 'Isolated JDK and Gradle wrapper ready.'
