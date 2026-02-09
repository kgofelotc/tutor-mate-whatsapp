# Script to configure Infobip WhatsApp Webhook

$apiKey = "05e2528d857d11fc3bb326170342ded4-4098c481-33a1-43e3-9d0a-d195776ce9aa"
$baseUrl = "https://e5v55q.api.infobip.com"
$webhookUrl = "https://tutormate-whatsapp.onrender.com/infobip/webhook"
$sender = "447860088970"

Write-Host "Configuring Infobip WhatsApp Webhook..." -ForegroundColor Cyan
Write-Host "Webhook URL: $webhookUrl" -ForegroundColor Yellow
Write-Host ""

# Try Method 1: Scenarios endpoint
Write-Host "Method 1: Configuring via Scenarios endpoint..." -ForegroundColor Green
try {
    $headers = @{
        "Authorization" = "App $apiKey"
        "Content-Type" = "application/json"
    }

    $body = @{
        url = $webhookUrl
        scenarioKey = "WHATSAPP_INBOUND_MESSAGES"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/notifications/1/scenarios" -Method POST -Headers $headers -Body $body -ErrorAction Stop
    Write-Host "✓ Webhook configured successfully via Scenarios!" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 5
}
catch {
    Write-Host "✗ Method 1 failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Try Method 2: WhatsApp specific endpoint
Write-Host "Method 2: Configuring via WhatsApp endpoint..." -ForegroundColor Green
try {
    $headers = @{
        "Authorization" = "App $apiKey"
        "Content-Type" = "application/json"
    }

    $body = @{
        forwardUrl = $webhookUrl
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/whatsapp/1/senders/$sender/inbound-messages" -Method PUT -Headers $headers -Body $body -ErrorAction Stop
    Write-Host "✓ Webhook configured successfully via WhatsApp endpoint!" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 5
}
catch {
    Write-Host "✗ Method 2 failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "If both methods failed, you may need to:" -ForegroundColor Yellow
Write-Host "1. Contact Infobip support to enable webhook configuration for trial accounts" -ForegroundColor Yellow
Write-Host "2. Or upgrade your account to access webhook features" -ForegroundColor Yellow
Write-Host ""
Write-Host "For now, you can still test SENDING messages from your app to WhatsApp!" -ForegroundColor Cyan
