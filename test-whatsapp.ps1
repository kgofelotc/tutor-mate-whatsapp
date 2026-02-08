# WhatsApp Message Testing Script
# Make sure the application is running on http://localhost:8082

Write-Host "=== WhatsApp Infobip Testing Script ===" -ForegroundColor Cyan
Write-Host ""

# Base URL
$baseUrl = "http://localhost:8082"

# Test if server is running
try {
    Write-Host "Checking if server is running..." -ForegroundColor Yellow
    $null = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -ErrorAction SilentlyContinue
    Write-Host "✓ Server is running!" -ForegroundColor Green
} catch {
    Write-Host "✗ Server is not running. Please start it first with: mvn spring-boot:run" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Select a test option:" -ForegroundColor Cyan
Write-Host "1. Send quick test template message to Tebogo (27662035457)"
Write-Host "2. Send custom text message"
Write-Host "3. Send custom template message"
Write-Host "4. Exit"
Write-Host ""

$choice = Read-Host "Enter your choice (1-4)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "Sending quick test template message..." -ForegroundColor Yellow
        try {
            $response = Invoke-RestMethod -Uri "$baseUrl/api/whatsapp/send-test" -Method POST -ContentType "application/json"
            Write-Host "✓ Success!" -ForegroundColor Green
            Write-Host "Response:" -ForegroundColor Cyan
            $response | ConvertTo-Json -Depth 10
        } catch {
            Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
            if ($_.ErrorDetails.Message) {
                Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
            }
        }
    }
    
    "2" {
        Write-Host ""
        $to = Read-Host "Enter recipient phone number (e.g., 27662035457)"
        $message = Read-Host "Enter message text"
        
        $body = @{
            to = $to
            message = $message
        } | ConvertTo-Json
        
        Write-Host ""
        Write-Host "Sending text message..." -ForegroundColor Yellow
        try {
            $response = Invoke-RestMethod -Uri "$baseUrl/api/whatsapp/send-text" `
                -Method POST `
                -Body $body `
                -ContentType "application/json"
            
            Write-Host "✓ Success!" -ForegroundColor Green
            Write-Host "Response:" -ForegroundColor Cyan
            $response | ConvertTo-Json -Depth 10
        } catch {
            Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
            if ($_.ErrorDetails.Message) {
                Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
            }
        }
    }
    
    "3" {
        Write-Host ""
        $to = Read-Host "Enter recipient phone number (e.g., 27662035457)"
        $templateName = Read-Host "Enter template name (default: test_whatsapp_template_en)"
        if ([string]::IsNullOrWhiteSpace($templateName)) {
            $templateName = "test_whatsapp_template_en"
        }
        
        $language = Read-Host "Enter language code (default: en)"
        if ([string]::IsNullOrWhiteSpace($language)) {
            $language = "en"
        }
        
        $placeholders = Read-Host "Enter placeholder value (e.g., Tebogo)"
        
        $body = @{
            to = $to
            templateName = $templateName
            language = $language
            placeholders = @($placeholders)
        } | ConvertTo-Json
        
        Write-Host ""
        Write-Host "Sending template message..." -ForegroundColor Yellow
        try {
            $response = Invoke-RestMethod -Uri "$baseUrl/api/whatsapp/send-template" `
                -Method POST `
                -Body $body `
                -ContentType "application/json"
            
            Write-Host "✓ Success!" -ForegroundColor Green
            Write-Host "Response:" -ForegroundColor Cyan
            $response | ConvertTo-Json -Depth 10
        } catch {
            Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
            if ($_.ErrorDetails.Message) {
                Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
            }
        }
    }
    
    "4" {
        Write-Host "Goodbye!" -ForegroundColor Cyan
        exit 0
    }
    
    default {
        Write-Host "Invalid choice. Please run the script again." -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
