#!/usr/bin/env pwsh
# SMS AI Conversation Simulation Script
# This script simulates SMS conversations with the SafeBirth Connect backend

$baseUrl = "http://localhost:8080"

# Colors for output
function Write-SMS-In { param($phone, $msg) Write-Host "📱 [$phone] → " -ForegroundColor Yellow -NoNewline; Write-Host $msg -ForegroundColor White }
function Write-SMS-Out { param($msg) Write-Host "💬 SafeBirth → " -ForegroundColor Green -NoNewline; Write-Host $msg -ForegroundColor White }
function Write-Section { param($title) Write-Host "`n═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan; Write-Host "  $title" -ForegroundColor Cyan; Write-Host "═══════════════════════════════════════════════════════════════`n" -ForegroundColor Cyan }

# Function to send SMS webhook (simulates Twilio)
function Send-SMS {
    param($From, $Body)
    
    $payload = @{
        from = $From
        body = $Body
        to = "+962700000000"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/sms/webhook" -Method POST -ContentType "application/json" -Body $payload
        return $response.message
    } catch {
        Write-Host "❌ Error: $_" -ForegroundColor Red
        return $null
    }
}

# Wait for server to be ready
function Wait-ForServer {
    Write-Host "⏳ Waiting for server at $baseUrl ..." -ForegroundColor Yellow
    $maxAttempts = 30
    for ($i = 0; $i -lt $maxAttempts; $i++) {
        try {
            $response = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method GET -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.status -eq "UP") {
                Write-Host "✅ Server is ready!" -ForegroundColor Green
                return $true
            }
        } catch {
            # Try API docs endpoint as fallback
            try {
                $null = Invoke-WebRequest -Uri "$baseUrl/swagger-ui/index.html" -Method GET -TimeoutSec 2 -ErrorAction SilentlyContinue
                Write-Host "✅ Server is ready!" -ForegroundColor Green
                return $true
            } catch {}
        }
        Start-Sleep -Seconds 2
    }
    Write-Host "❌ Server did not start in time" -ForegroundColor Red
    return $false
}

# ============================================
# SCENARIO 1: New Mother Registration (English)
# ============================================
function Demo-MotherRegistration {
    Write-Section "SCENARIO 1: Mother Registration (English)"
    
    $phone = "+962790000001"
    
    Write-Host "👤 A new mother texts for help..."
    Start-Sleep -Seconds 1
    
    # First message - emergency
    Write-SMS-In $phone "Help! I'm pregnant and having contractions"
    $response = Send-SMS -From $phone -Body "Help! I'm pregnant and having contractions"
    if ($response) { Write-SMS-Out $response }
    Start-Sleep -Seconds 2
    
    # Mother provides information
    Write-SMS-In $phone "I'm 28 years old, due in March, no previous complications, I'm in Zaatari camp zone 5"
    $response = Send-SMS -From $phone -Body "I'm 28 years old, due in March, no previous complications, I'm in Zaatari camp zone 5"
    if ($response) { Write-SMS-Out $response }
    Start-Sleep -Seconds 2
    
    Write-Host "`n✅ Mother registration complete!" -ForegroundColor Green
}

# ============================================
# SCENARIO 2: New Mother Registration (Arabic)
# ============================================
function Demo-MotherRegistrationArabic {
    Write-Section "SCENARIO 2: Mother Registration (Arabic)"
    
    $phone = "+962790000002"
    
    Write-Host "👤 أم جديدة ترسل رسالة بالعربي..."
    Start-Sleep -Seconds 1
    
    # Arabic message
    Write-SMS-In $phone "مساعدة! أنا حامل وعندي طلق"
    $response = Send-SMS -From $phone -Body "مساعدة! أنا حامل وعندي طلق"
    if ($response) { Write-SMS-Out $response }
    Start-Sleep -Seconds 2
    
    # Mother provides info in Arabic
    Write-SMS-In $phone "عمري 25 سنة، موعدي شهر 4، لا يوجد مضاعفات سابقة، مخيم الزعتري منطقة 3"
    $response = Send-SMS -From $phone -Body "عمري 25 سنة، موعدي شهر 4، لا يوجد مضاعفات سابقة، مخيم الزعتري منطقة 3"
    if ($response) { Write-SMS-Out $response }
    Start-Sleep -Seconds 2
    
    Write-Host "`n✅ تم تسجيل الأم بنجاح!" -ForegroundColor Green
}

# ============================================
# SCENARIO 3: Volunteer Registration
# ============================================
function Demo-VolunteerRegistration {
    Write-Section "SCENARIO 3: Volunteer Registration"
    
    $phone = "+962790000003"
    
    Write-Host "🏥 A new volunteer texts to register..."
    Start-Sleep -Seconds 1
    
    # First message
    Write-SMS-In $phone "Hi, I'm a nurse and want to volunteer"
    $response = Send-SMS -From $phone -Body "Hi, I'm a nurse and want to volunteer"
    if ($response) { Write-SMS-Out $response }
    Start-Sleep -Seconds 2
    
    # Volunteer provides skills
    Write-SMS-In $phone "I can help with labor, bleeding, and giving advice. I'm in Zaatari"
    $response = Send-SMS -From $phone -Body "I can help with labor, bleeding, and giving advice. I'm in Zaatari"
    if ($response) { Write-SMS-Out $response }
    Start-Sleep -Seconds 2
    
    Write-Host "`n✅ Volunteer registration complete!" -ForegroundColor Green
}

# ============================================
# SCENARIO 4: Returning Mother Help Request
# ============================================
function Demo-ReturningMotherHelp {
    Write-Section "SCENARIO 4: Returning Mother Requests Help"
    
    # Use a pre-registered mother
    $phone = "+962791111001"
    
    Write-Host "🆘 A registered mother sends an emergency message..."
    Start-Sleep -Seconds 1
    
    Write-SMS-In $phone "I'm bleeding heavily, please help!"
    $response = Send-SMS -From $phone -Body "I'm bleeding heavily, please help!"
    if ($response) { Write-SMS-Out $response }
    Start-Sleep -Seconds 2
    
    Write-Host "`n✅ Help request created, volunteers being notified!" -ForegroundColor Green
}

# ============================================
# SCENARIO 5: Volunteer Direct Commands
# ============================================
function Demo-VolunteerCommands {
    Write-Section "SCENARIO 5: Volunteer Direct Commands"
    
    # Use a pre-registered volunteer
    $phone = "+962792222001"
    
    Write-Host "📋 A volunteer uses direct commands..."
    Start-Sleep -Seconds 1
    
    # Set busy
    Write-SMS-In $phone "busy"
    $response = Send-SMS -From $phone -Body "busy"
    if ($response) { Write-SMS-Out $response }
    Start-Sleep -Seconds 2
    
    # Set available
    Write-SMS-In $phone "available"
    $response = Send-SMS -From $phone -Body "available"
    if ($response) { Write-SMS-Out $response }
    Start-Sleep -Seconds 2
    
    # ETA response
    Write-SMS-In $phone "10"
    $response = Send-SMS -From $phone -Body "10"
    if ($response) { Write-SMS-Out $response }
    Start-Sleep -Seconds 2
    
    Write-Host "`n✅ Volunteer commands demonstrated!" -ForegroundColor Green
}

# ============================================
# Main Execution
# ============================================
Write-Host @"

╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║          🏥 SafeBirth Connect SMS Simulation 🏥               ║
║                                                               ║
║     AI-Powered Conversational SMS for Refugee Camps          ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝

"@ -ForegroundColor Magenta

# Check if server is running
if (-not (Wait-ForServer)) {
    Write-Host "`n⚠️  Please start the server first with:" -ForegroundColor Yellow
    Write-Host "   mvn spring-boot:run" -ForegroundColor Cyan
    exit 1
}

# Run demos
Demo-MotherRegistration
Demo-MotherRegistrationArabic
Demo-VolunteerRegistration
Demo-ReturningMotherHelp
Demo-VolunteerCommands

Write-Host @"

╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║               ✅ All Scenarios Complete!                       ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝

"@ -ForegroundColor Green
