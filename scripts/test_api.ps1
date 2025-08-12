param(
  [string]$BaseUrl = 'http://localhost:8000'
)

$ErrorActionPreference = 'Stop'
$BASE = $BaseUrl

Write-Host "===\u003e Usando BASE: $BASE" -ForegroundColor Cyan

function New-RandomIdent {
  $suffix = Get-Random -Maximum 99999999
  return ('1{0:D8}' -f $suffix)
}

function Call-Api {
  param(
    [Parameter(Mandatory)] [ValidateSet('GET','POST','PUT','DELETE')] [string]$Method,
    [Parameter(Mandatory)] [string]$Path,
    [Parameter()] $Body
  )
  $url = ($BASE.TrimEnd('/')) + $Path
  $params = @{ Uri = $url; Method = $Method; Headers = @{ 'Content-Type' = 'application/json' } }
  if ($PSBoundParameters.ContainsKey('Body')) {
    $json = $null
    if ($Body -is [string]) {
      $json = $Body
    } else {
      $json = $Body | ConvertTo-Json -Depth 10
    }
    $params.Body = $json
  }
  $t0 = Get-Date
  try {
    $r = Invoke-WebRequest @params
    $elapsed = (Get-Date) - $t0
    $parsedBody = $null
    try { $parsedBody = $r.Content | ConvertFrom-Json } catch { $parsedBody = $null }
    $out = [pscustomobject]@{
      Status = [int]$r.StatusCode
      BodyRaw = $r.Content
      Body = $parsedBody
      Ms = [math]::Round($elapsed.TotalMilliseconds, 1)
      Url = $url
    }
  } catch {
    $resp = $_.Exception.Response
    $elapsed = (Get-Date) - $t0
    if ($resp -ne $null) {
      $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
      $content = $reader.ReadToEnd()
      $parsedErr = $null
      try { $parsedErr = $content | ConvertFrom-Json } catch { $parsedErr = $null }
      $out = [pscustomobject]@{
        Status = [int]$resp.StatusCode
        BodyRaw = $content
        Body = $parsedErr
        Ms = [math]::Round($elapsed.TotalMilliseconds, 1)
        Url = $url
      }
    } else {
      $out = [pscustomobject]@{ Status = -1; BodyRaw = $_.ToString(); Body = $null; Ms = [math]::Round($elapsed.TotalMilliseconds, 1); Url = $url }
    }
  }
  $badge = 'ERR'
  if ($out.Status -ge 200 -and $out.Status -lt 300) { $badge = 'OK' }
  $color = 'Red'
  if ($badge -eq 'OK') { $color = 'Green' }
  Write-Host ("[$badge] {0} {1} -> HTTP {2} ({3} ms)" -f $Method, $Path, $out.Status, $out.Ms) -ForegroundColor $color
  if ($null -ne $out.Body) { $out.Body | ConvertTo-Json -Depth 10 | Write-Host } else { Write-Host $out.BodyRaw }
  return $out
}

# ============ 1) CLIENTES ============
Write-Host "\n=== 1) Crear cliente OK ===" -ForegroundColor Yellow
$attempts = 0; $CLIENTE1_ID=$null
while ($attempts -lt 3 -and -not $CLIENTE1_ID) {
  $nid = New-RandomIdent
  $r = Call-Api -Method POST -Path '/api/clientes' -Body @{ tipoIdentificacion='CC'; numIdentificacion=$nid; nombres='Juan'; apellidos='Pérez'; email='juan.perez@example.com'; fechaNacimiento='1990-05-10' }
  if ($r.Status -ge 200 -and $r.Status -lt 300) { $CLIENTE1_ID = $r.Body.id; break }
  $attempts++
}
if (-not $CLIENTE1_ID) { Write-Host "No se pudo crear CLIENTE1, algunas pruebas se omitirán." -ForegroundColor Red }

Write-Host "\n=== 1b) Menor de edad (400 esperado) ===" -ForegroundColor Yellow
Call-Api -Method POST -Path '/api/clientes' -Body @{ tipoIdentificacion='CC'; numIdentificacion='100000002'; nombres='Ana'; apellidos='Jaramillo'; email='ana.jara@example.com'; fechaNacimiento='2010-01-01' } | Out-Null

Write-Host "\n=== 1c) Email inválido (400 esperado) ===" -ForegroundColor Yellow
Call-Api -Method POST -Path '/api/clientes' -Body @{ tipoIdentificacion='CC'; numIdentificacion='100000003'; nombres='Pedro'; apellidos='López'; email='correo-invalido'; fechaNacimiento='1992-07-20' } | Out-Null

Write-Host "\n=== 1d) Nombre/apellido cortos (400 si aplica) ===" -ForegroundColor Yellow
Call-Api -Method POST -Path '/api/clientes' -Body @{ tipoIdentificacion='CC'; numIdentificacion='100000004'; nombres='A'; apellidos='B'; email='ab@example.com'; fechaNacimiento='1995-03-15' } | Out-Null

Write-Host "\n=== 1e) Duplicado (409 esperado) ===" -ForegroundColor Yellow
Call-Api -Method POST -Path '/api/clientes' -Body @{ tipoIdentificacion='CC'; numIdentificacion='100000001'; nombres='Juan'; apellidos='Pérez'; email='otro@example.com'; fechaNacimiento='1990-05-10' } | Out-Null

Write-Host "\n=== 1f) Actualizar cliente (200) ===" -ForegroundColor Yellow
if ($CLIENTE1_ID) {
  Call-Api -Method PUT -Path ("/api/clientes/$CLIENTE1_ID") -Body @{ nombres='Juan Carlos'; apellidos='Pérez Rojas'; email='juan.carlos@example.com' } | Out-Null
} else { Write-Host "Saltando actualización: CLIENTE1_ID no disponible" -ForegroundColor DarkYellow }

# ============ 2) CUENTAS ============
Write-Host "\n=== 2) Crear cuenta AHORROS para CLIENTE1 (201) ===" -ForegroundColor Yellow
$CTA1_ID=$null; $CTA1_NUM=$null
if ($CLIENTE1_ID) {
  $r = Call-Api -Method POST -Path '/api/cuentas' -Body @{ clienteId=$CLIENTE1_ID; tipoCuenta='AHORROS'; exentaGmf=$false; usuarioPropietario='empleado.demo' }
  if ($r.Status -ge 200 -and $r.Status -lt 300) { $CTA1_ID  = $r.Body.id; $CTA1_NUM = $r.Body.numeroCuenta } else { Write-Host "No se creó CTA1" -ForegroundColor DarkYellow }
} else { Write-Host "Saltando creación de CTA1: CLIENTE1_ID no disponible" -ForegroundColor DarkYellow }

Write-Host "\n=== 2b) Crear CLIENTE2 y cuenta CORRIENTE (201) ===" -ForegroundColor Yellow
$attempts=0; $CLIENTE2_ID=$null
while ($attempts -lt 3 -and -not $CLIENTE2_ID) {
  $nid2 = New-RandomIdent
  $r = Call-Api -Method POST -Path '/api/clientes' -Body @{ tipoIdentificacion='CC'; numIdentificacion=$nid2; nombres='María'; apellidos='Gómez'; email='maria.gomez@example.com'; fechaNacimiento='1989-08-08' }
  if ($r.Status -ge 200 -and $r.Status -lt 300) { $CLIENTE2_ID = $r.Body.id; break }
  $attempts++
}
if (-not $CLIENTE2_ID) { Write-Host "No se pudo crear CLIENTE2, se omiten pruebas relacionadas" -ForegroundColor Red }
if ($CLIENTE2_ID) {
  $r = Call-Api -Method POST -Path '/api/cuentas' -Body @{ clienteId=$CLIENTE2_ID; tipoCuenta='CORRIENTE'; exentaGmf=$true; usuarioPropietario='empleado.demo' }
  if ($r.Status -ge 200 -and $r.Status -lt 300) { $CTA2_ID  = $r.Body.id; $CTA2_NUM = $r.Body.numeroCuenta } else { Write-Host "No se creó CTA2" -ForegroundColor DarkYellow }
}

Write-Host "\n=== 2c) Cliente inexistente (404 esperado) ===" -ForegroundColor Yellow
Call-Api -Method POST -Path '/api/cuentas' -Body @{ clienteId='00000000-0000-0000-0000-000000000000'; tipoCuenta='AHORROS'; exentaGmf=$false; usuarioPropietario='empleado.demo' } | Out-Null

Write-Host "\n=== 2d) Tipo inválido (400 esperado) ===" -ForegroundColor Yellow
Call-Api -Method POST -Path '/api/cuentas' -Body @{ clienteId=$CLIENTE1_ID; tipoCuenta='INVERSION'; exentaGmf=$false; usuarioPropietario='empleado.demo' } | Out-Null

Write-Host "\n=== 2e) Cambiar estado a INACTIVA (200) ===" -ForegroundColor Yellow
if ($CTA1_ID) { Call-Api -Method PUT -Path ("/api/cuentas/$CTA1_ID") -Body @{ estado='INACTIVA' } | Out-Null } else { Write-Host "Saltando cambio de estado: CTA1_ID no disponible" -ForegroundColor DarkYellow }

Write-Host "\n=== 2f) Intentar cancelación con saldo > 0 (400/409 esperado) ===" -ForegroundColor Yellow
if ($CTA1_ID) { Call-Api -Method PUT -Path ("/api/cuentas/$CTA1_ID") -Body @{ estado='CANCELADA' } | Out-Null } else { Write-Host "Saltando cancelación: CTA1_ID no disponible" -ForegroundColor DarkYellow }

# ============ 3) TRANSACCIONES ============
Write-Host "\n=== 3) Consignación CTA1 (201) ===" -ForegroundColor Yellow
if ($CTA1_ID) { Call-Api -Method POST -Path '/api/transacciones/consignacion' -Body @{ cuentaDestinoId=$CTA1_ID; monto=150000; descripcion='Consignación inicial'; referencia='REF-CONS-001' } | Out-Null } else { Write-Host "Saltando consignación: CTA1_ID no disponible" -ForegroundColor DarkYellow }

Write-Host "\n=== 3b) Retiro CTA1 con fondos suficientes (201) ===" -ForegroundColor Yellow
if ($CTA1_ID) { Call-Api -Method POST -Path '/api/transacciones/retiro' -Body @{ cuentaOrigenId=$CTA1_ID; monto=50000; descripcion='Retiro efectivo'; referencia='REF-RET-001' } | Out-Null } else { Write-Host "Saltando retiro: CTA1_ID no disponible" -ForegroundColor DarkYellow }

Write-Host "\n=== 3c) Retiro con fondos insuficientes (400 esperado) ===" -ForegroundColor Yellow
if ($CTA1_ID) { Call-Api -Method POST -Path '/api/transacciones/retiro' -Body @{ cuentaOrigenId=$CTA1_ID; monto=999999999; descripcion='Retiro sin fondos'; referencia='REF-RET-002' } | Out-Null } else { Write-Host "Saltando retiro insuficiente: CTA1_ID no disponible" -ForegroundColor DarkYellow }

Write-Host "\n=== 3d) Transferencia CTA1 -\u003e CTA2 (201) ===" -ForegroundColor Yellow
if ($CTA1_ID -and $CTA2_ID) { Call-Api -Method POST -Path '/api/transacciones/transferencia' -Body @{ cuentaOrigenId=$CTA1_ID; cuentaDestinoId=$CTA2_ID; monto=30000; descripcion='Transferencia prueba'; referencia='REF-TRX-001' } | Out-Null } else { Write-Host "Saltando transferencia: faltan IDs" -ForegroundColor DarkYellow }

Write-Host "\n=== 3e) Transferencia origen=destino (400 esperado) ===" -ForegroundColor Yellow
if ($CTA1_ID) { Call-Api -Method POST -Path '/api/transacciones/transferencia' -Body @{ cuentaOrigenId=$CTA1_ID; cuentaDestinoId=$CTA1_ID; monto=10000; descripcion='Transferencia inválida'; referencia='REF-TRX-002' } | Out-Null } else { Write-Host "Saltando transferencia inválida: CTA1_ID no disponible" -ForegroundColor DarkYellow }

Write-Host "\n=== 3f) Transferencia fondos insuficientes (400 esperado) ===" -ForegroundColor Yellow
if ($CTA1_ID -and $CTA2_ID) { Call-Api -Method POST -Path '/api/transacciones/transferencia' -Body @{ cuentaOrigenId=$CTA1_ID; cuentaDestinoId=$CTA2_ID; monto=999999999; descripcion='Transferencia sin fondos'; referencia='REF-TRX-003' } | Out-Null } else { Write-Host "Saltando transferencia insuficiente: faltan IDs" -ForegroundColor DarkYellow }

# ============ 4) Cancelación con saldo 0 (opcional) ============
Write-Host "\n=== 4) (Opcional) Cancelar CTA1 si saldo=0 ===" -ForegroundColor Yellow
Write-Host "Si necesitas cancelar CTA1, asegúrate de dejar saldo en 0 y luego ejecuta:" -ForegroundColor DarkGray
if ($CTA1_ID) {
  Write-Host "  Call-Api -Method PUT -Path '/api/cuentas/$CTA1_ID' -Body @{ estado='CANCELADA' }" -ForegroundColor DarkGray
} else {
  Write-Host "  (CTA1_ID no disponible)" -ForegroundColor DarkGray
}

Write-Host "\n=== FIN de pruebas ===" -ForegroundColor Cyan

