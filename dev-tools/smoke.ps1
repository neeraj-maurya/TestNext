# dev-tools/smoke.ps1
# Safe smoke script: does NOT call exit; logs errors but keeps the shell alive.

$results = @()
$now = Get-Date -Format "yyyyMMddHHmmss"

function record($name, $ok, $msg='') {
  $results += [pscustomobject]@{ Step = $name; OK = $ok; Message = $msg }
  if (-not $ok) { Write-Error "STEP FAILED: $name - $msg" } else { Write-Output "STEP OK: $name" }
}

# Wait for health (non-fatal)
Write-Output "Waiting for backend health (up to 60s)..."
$max = 60; $ready = $false
for ($i=0; $i -lt $max; $i++) {
  try {
    $h = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -ErrorAction Stop
    if ($h.status -eq 'UP') { $ready = $true; break }
  } catch {}
  Start-Sleep -Seconds 1
}
$msg = if ($ready) { "UP" } else { "NOT UP after 60s" }
record "backend-health" $ready $msg

# Helper to POST JSON and return parsed object or $null
function safePost($uri, $json) {
  try {
    return Invoke-RestMethod -Method Post -Uri $uri -Body ($json | ConvertTo-Json -Depth 5) -ContentType "application/json" -ErrorAction Stop
  } catch {
    return $null
  }
}

# TEST SUITES: GET
try {
  $suites = Invoke-RestMethod -Uri "http://localhost:8080/api/test-suites" -ErrorAction Stop
  record "test-suites-get" $true ""
} catch {
  record "test-suites-get" $false $_.Exception.Message
}

# TEST SUITES: CREATE -> DELETE
$body = @{ name = "smoke-suite-$now"; projectId = 1; description = "smoke $now" }
$created = safePost "http://localhost:8080/api/test-suites" $body
if ($created -ne $null) {
  record "test-suites-create" $true ("id="+$created.id)
  try {
    Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/test-suites/$($created.id)" -ErrorAction Stop
    record "test-suites-delete" $true ""
  } catch {
    record "test-suites-delete" $false $_.Exception.Message
  }
} else {
  record "test-suites-create" $false "create returned null"
}

# TENANTS: GET
try {
  $tenants = Invoke-RestMethod -Uri "http://localhost:8080/api/tenants" -ErrorAction Stop
  record "tenants-get" $true ""
} catch {
  record "tenants-get" $false $_.Exception.Message
}

# TENANTS: CREATE -> UPDATE -> DELETE
$tenantBody = @{ name = "smoke-tenant-$now"; schemaName = "smoke_schema_$now" }
$tenant = safePost "http://localhost:8080/api/tenants" $tenantBody
if ($tenant -ne $null -and $tenant.id) {
  record "tenants-create" $true ("id="+$tenant.id)
  try {
    $upd = @{ name = "smoke-tenant-updated-$now"; schemaName = $tenant.schemaName } 
    $updated = Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/tenants/$($tenant.id)" -Body ($upd | ConvertTo-Json) -ContentType "application/json" -ErrorAction Stop
    record "tenants-update" $true ""
  } catch {
    record "tenants-update" $false $_.Exception.Message
  }
  try {
    Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/tenants/$($tenant.id)" -ErrorAction Stop
    record "tenants-delete" $true ""
  } catch {
    record "tenants-delete" $false $_.Exception.Message
  }
} else {
  record "tenants-create" $false "create returned null or no id"
}

# SYSTEM USERS: GET -> CREATE -> DELETE (if /api/system-users exists)
try {
  $users = Invoke-RestMethod -Uri "http://localhost:8080/api/system-users" -ErrorAction Stop
  record "system-users-get" $true ""
  $userBody = @{ username = "smokeuser_$now"; email = "smoke_$now@example.test"; displayName = "Smoke User $now"; role = "SYSTEM_ADMIN"; hashedPassword = "pass" }
  $user = safePost "http://localhost:8080/api/system-users" $userBody
  if ($user -ne $null -and $user.id) {
    record "system-users-create" $true ("id="+$user.id)
    try {
      Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/system-users/$($user.id)" -ErrorAction Stop
      record "system-users-delete" $true ""
    } catch {
      record "system-users-delete" $false $_.Exception.Message
    }
  } else {
    record "system-users-create" $false "create returned null or no id"
  }
} catch {
  record "system-users-get" $false $_.Exception.Message
}

# SUMMARY (no exit)
Write-Output "`n--- SMOKE SUMMARY ---"
$results | Format-Table -AutoSize

$failed = $results | Where-Object { -not $_.OK }
if ($failed) {
  Write-Output "`nSome steps failed. Inspect above errors."
} else {
  Write-Output "`nAll smoke steps succeeded."
}
