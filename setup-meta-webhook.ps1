# Webhook Setup Script for Meta WhatsApp
# This script helps you set up ngrok and configure Meta webhook

Write-Host "`n*** WEBHOOK SETUP FOR META WHATSAPP ***`n" -ForegroundColor Cyan

# Check if app is running
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8082/api/meta/test/health" -Method GET -ErrorAction Stop
    Write-Host "[OK] Application is running on port 8082" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Application is NOT running!" -ForegroundColor Red
    Write-Host "        Please start it first: mvn spring-boot:run`n" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n*** WEBHOOK CONFIGURATION DETAILS ***" -ForegroundColor Cyan
Write-Host "  Webhook URL Path: /meta/webhook" -ForegroundColor White
Write-Host "  Verify Token: tutormate_secure_verify_token_2026" -ForegroundColor White
Write-Host "  Subscribe to: messages field" -ForegroundColor White

Write-Host "`n*** NEXT STEPS ***" -ForegroundColor Cyan
Write-Host "`n[STEP 1] START NGROK (in a NEW PowerShell window):" -ForegroundColor Yellow
Write-Host "         ngrok http 8082" -ForegroundColor White

Write-Host "`n[STEP 2] COPY YOUR NGROK URL:" -ForegroundColor Yellow
Write-Host "         Look for the 'Forwarding' line in ngrok output" -ForegroundColor White
Write-Host "         Copy the HTTPS URL (e.g., https://abc123.ngrok-free.app)" -ForegroundColor White

Write-Host "`n[STEP 3] CONFIGURE WEBHOOK IN META:" -ForegroundColor Yellow
Write-Host "         a) Go to: https://developers.facebook.com/apps" -ForegroundColor White
Write-Host "         b) Select your app -> WhatsApp -> Configuration" -ForegroundColor White
Write-Host "         c) Click 'Edit' next to Webhook" -ForegroundColor White
Write-Host "         d) Enter:" -ForegroundColor White
Write-Host "            Callback URL: https://YOUR_NGROK_URL/meta/webhook" -ForegroundColor Cyan
Write-Host "            Verify Token: tutormate_secure_verify_token_2026" -ForegroundColor Cyan
Write-Host "         e) Click 'Verify and Save'" -ForegroundColor White
Write-Host "         f) Subscribe to 'messages' webhook field" -ForegroundColor White

Write-Host "`n[STEP 4] TEST IT:" -ForegroundColor Yellow
Write-Host "         Send a WhatsApp message from your phone:" -ForegroundColor White
Write-Host "         TO: +1 555 142 4698 (Meta's test number)" -ForegroundColor Cyan
Write-Host "         MESSAGE: hi" -ForegroundColor Cyan

Write-Host "`n[STEP 5] CHECK LOGS:" -ForegroundColor Yellow
Write-Host "         Watch your application logs for:" -ForegroundColor White
Write-Host "         'Received webhook from Meta: ...'" -ForegroundColor Cyan
Write-Host "         'Processing message from 27785312360: hi'" -ForegroundColor Cyan

Write-Host "`n*** QUICK REFERENCE ***" -ForegroundColor Cyan
Write-Host "  Your Phone: +27 78 531 2360" -ForegroundColor White
Write-Host "  Meta Test Number: +1 555 142 4698" -ForegroundColor White
Write-Host "  Verify Token: tutormate_secure_verify_token_2026" -ForegroundColor White
Write-Host "  Webhook Path: /meta/webhook" -ForegroundColor White

Write-Host "`n*** IMPORTANT ***" -ForegroundColor Yellow
Write-Host "  - Keep ngrok running while testing" -ForegroundColor White
Write-Host "  - Keep your app running (mvn spring-boot:run)" -ForegroundColor White
Write-Host "  - ngrok URL changes each restart (free version)" -ForegroundColor White
Write-Host "  - Update webhook URL in Meta if ngrok restarts" -ForegroundColor White

Write-Host "`nReady to start ngrok? Open a NEW terminal and run: ngrok http 8082`n" -ForegroundColor Green

