param(
 [Parameter(Mandatory=$true)][string]$Source,
 [Parameter(Mandatory=$true)][string]$Jdk,
 [ValidateSet('baseline','corpus','mutants','restored')][string]$Stage='baseline'
)
$ErrorActionPreference='Stop'
$Here=$PSScriptRoot
$Source=(Resolve-Path -LiteralPath $Source).Path
$env:JAVA_HOME=$Jdk
$env:PATH="$Jdk/bin;$env:PATH"
$Results=Join-Path $Here 'results'
$Scratch=Join-Path $Source 'logs/campaign-revision'
New-Item -ItemType Directory -Force $Results,$Scratch | Out-Null
$env:CAMPAIGN1_RESULTS_DIR=Join-Path $Results 'corpus-ledgers'
$Baseline='cb1afadaf683f915a31b09df2fd732da10515df4'
if ((git -C $Source rev-parse HEAD) -ne $Baseline) {throw 'Wrong source baseline'}

function Manifest($pattern) {
 $items=[ordered]@{}
 Get-ChildItem -LiteralPath "$Source/product" -Recurse -File -Filter $pattern | Sort-Object FullName | ForEach-Object {
  $rel=[IO.Path]::GetRelativePath($Source,$_.FullName).Replace('\','/')
  $items[$rel]=(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
 }
 return $items
}
function Capture($name,$selection) {
 $record=Join-Path $Results $name
 New-Item -ItemType Directory -Force $record | Out-Null
 # Remove only prior XML results under the explicitly checked scratch root. Never use old XML.
 Get-ChildItem -LiteralPath "$Source/product" -Recurse -File -Filter 'TEST-*.xml' | Where-Object {
  $_.FullName.Contains([IO.Path]::DirectorySeparatorChar+'test-results'+[IO.Path]::DirectorySeparatorChar)
 } | ForEach-Object {
  if (-not $_.FullName.StartsWith($Source+[IO.Path]::DirectorySeparatorChar)) {throw 'Outside scratch'}
  Remove-Item -LiteralPath $_.FullName
 }
 $command=@('--console=plain','--continue','--no-build-cache','--rerun-tasks')+$selection
 $started=[DateTime]::UtcNow.ToString('o')
 Push-Location "$Source/product"
 try { & ./gradlew.bat @command *> "$Scratch/$name.log"; $code=$LASTEXITCODE }
 finally {Pop-Location}
 $cases=@(); $suites=@()
 Get-ChildItem -LiteralPath "$Source/product" -Recurse -File -Filter 'TEST-*.xml' | Where-Object {$_.FullName.Contains('test-results')} | Sort-Object FullName | ForEach-Object {
  Copy-Item -LiteralPath $_.FullName -Destination $record
  [xml]$xml=Get-Content -LiteralPath $_.FullName -Raw
  $suites+=@{name=$xml.testsuite.name;tests=$xml.testsuite.tests;errors=$xml.testsuite.errors;failures=$xml.testsuite.failures}
  foreach($case in $xml.testsuite.testcase) {
   $cases+=@{class=$case.classname;name=$case.name;failure=@($case.failure | Where-Object {$_} | ForEach-Object {@{type=$_.type;message=$_.message;text=$_.InnerText}});error=@($case.error | Where-Object {$_} | ForEach-Object {@{type=$_.type;message=$_.message;text=$_.InnerText}});skipped=($null -ne $case.skipped)}
  }
 }
 $data=@{name=$name;sourceCommit=$Baseline;startedUtc=$started;finishedUtc=[DateTime]::UtcNow.ToString('o');command=@('gradlew.bat')+$command;exitCode=$code;suites=$suites;cases=$cases;sourceHashes=(Manifest '*.java');classfileHashes=(Manifest '*.class');consoleSha256=(Get-FileHash "$Scratch/$name.log").Hash.ToLowerInvariant()}
 $data | ConvertTo-Json -Depth 12 | Set-Content -Encoding utf8 "$record/run.json"
 Write-Host "$name exit=$code tests=$($cases.Count) failures=$(@($cases | Where-Object {$_.failure.Count -gt 0 -or $_.error.Count -gt 0}).Count)"
 return $data
}
$Selection=@(':types:test','--tests','com.arcogine.types.Campaign1*',':factory:test','--tests','com.arcogine.factory.model.Campaign1*','--tests','com.arcogine.factory.model.IndependentFactoryChallengeTest',':storage:test','--tests','com.arcogine.governance.change.Campaign1*','--tests','com.arcogine.governance.change.IndependentStorageChallengeTest')
if ($Stage -eq 'baseline') {
 (& "$Jdk/bin/java.exe" -version 2>&1 | Out-String) | Set-Content "$Results/java-version.txt"
 Push-Location "$Source/product"
 try { & ./gradlew.bat --version *> "$Results/gradle-version.txt" } finally {Pop-Location}
 $r=Capture 'clean-retained' @('clean',':types:test',':factory:test','--tests','com.arcogine.factory.model.*','--tests','com.arcogine.factory.change.*',':storage:test',':governance:test')
 if ($r.exitCode -ne 0 -or $r.cases.Count -eq 0) {throw 'Clean retained baseline failed'}
 exit
}
foreach($module in @('types','factory','storage')) {
 $dest=if($module -eq 'factory'){'domains/factory'}else{$module}
 Copy-Item -Recurse -Force "$Here/experiment-sources/$module/src/*" "$Source/product/$dest/src"
}
if ($Stage -in @('corpus','restored')) {
 $r=Capture $Stage $Selection
 if ($r.exitCode -ne 0 -or $r.cases.Count -ne 43) {throw 'Corpus baseline failed or discovery changed'}
 exit
}
$clean=Get-Content "$Results/corpus/run.json" -Raw | ConvertFrom-Json
if ($clean.exitCode -ne 0 -or $clean.cases.Count -ne 43) {throw 'Missing clean corpus baseline'}
$definitions=Get-Content "$Here/mutation/mutants.txt" -Raw
$mutants=@()
foreach($block in ($definitions -split '(?m)^=== ')[1..44]) {
 $match=[regex]::Match($block,'(?s)^([^\r\n]+)\r?\nfile: ([^\r\n]+).*?--- old\r?\n(.*?)\r?\n--- new\r?\n(.*)$')
 if (-not $match.Success) {throw 'Malformed mutant'}
 $mutants+=@{id=$match.Groups[1].Value;file=$match.Groups[2].Value;old=$match.Groups[3].Value.Replace("`r`n","`n").TrimEnd("`n");new=$match.Groups[4].Value.Replace("`r`n","`n").TrimEnd("`n")}
}
$mutants+=@{id='CH1-MESSAGE';file='product/domains/factory/src/main/java/com/arcogine/factory/model/validation/FactoryModelValidator.java';old='"must contain only valid Unicode scalar values"';new='field.startsWith("resources[") ? "must contain only valid Unicode scalar values" : "CHALLENGE changed text"';multiple=$true}
$mutants+=@{id='CH2-CATEGORY';file='product/storage/src/main/java/com/arcogine/storage/FileArcogineStorage.java';old='storedArtifact ? STORAGE_INTEGRITY : FINGERPRINT_MISMATCH;';new='storedArtifact ? STORAGE_INTEGRITY : STORAGE_INTEGRITY;'}
$mutants+=@{id='CH3-UNREFERENCED';file='product/domains/factory/src/main/java/com/arcogine/factory/model/FactoryRuntimeAssembler.java';old='for (OperationDefinition operation : model.operations()) {';new='for (OperationDefinition operation : model.operations()) { if (model.products().stream().noneMatch(p -> p.operationId() == operation.id())) { continue; }'}
foreach($mutant in $mutants) {
 $path=Join-Path $Source $mutant.file
 $original=[IO.File]::ReadAllBytes($path)
 try {
  $text=[Text.Encoding]::UTF8.GetString($original).Replace("`r`n","`n")
  $count=([regex]::Matches($text,[regex]::Escape($mutant.old))).Count
  if ($count -eq 0 -or ($count -ne 1 -and -not $mutant.multiple)) {throw "Mutation is not unique: $($mutant.id) $count"}
  [IO.File]::WriteAllText($path,$text.Replace($mutant.old,$mutant.new),[Text.UTF8Encoding]::new($false))
  $null=Capture $mutant.id $Selection
 } finally { [IO.File]::WriteAllBytes($path,$original) }
}
