# Test sending WhatsApp message via TutorMate app

$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    to = "27662035457"  # Replace with your WhatsApp number (with country code)
    message = "Hello! This is a test message from TutorMate app running on Render! 🎉"
} | ConvertTo-Json

Write-Host "Sending test message via TutorMate app..." -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "https://tutormate-whatsapp.onrender.com/api/whatsapp/send-text" -Method POST -Headers $headers -Body $body
    Write-Host "✓ Message sent successfully!" -ForegroundColor Green
    $response
}
catch {
    Write-Host "✗ Failed to send: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
}
