# Meta WhatsApp Credentials Setup Script
# Run this before starting your application

Write-Host "`n🔐 Setting up Meta WhatsApp credentials...`n" -ForegroundColor Cyan

# Your Meta WhatsApp credentials from the dashboard
$env:META_WHATSAPP_ACCESS_TOKEN="EAATAmHycwZC8BQ3Ftyc5GhUpvbHlPZBnoCu95PDyksYku0GaZB1GhMcKtIZBBScCqSpqJUGbO8KnsAxHAb9yZBmPHTF3CTNESr53xsEZAhOVGKevjpAUu1COxZBbjevZABSN8Q8P3xVPBQFx9ukxFZASze6U6CYd3tZAqpjRen1ZBLOg5p6JJfcVa9wWCjTEe0Xugh9y3XEgcJRA0BkN1vZCq7FWXPU5KYWXTFWB9lv5YZCugv1MrZCW3H6uGpZBCeLFFnZCJvn4O7LH7Ed4pROyBfYjeeRotgZDZD"
$env:META_WHATSAPP_PHONE_NUMBER_ID="959729383897901"
$env:META_WHATSAPP_BUSINESS_ACCOUNT_ID="941501588391536"
$env:META_WEBHOOK_VERIFY_TOKEN="tutormate_secure_verify_token_2026"

# Your test phone number (for testing)
$TEST_PHONE="27785312360"

Write-Host "✅ Meta Access Token set" -ForegroundColor Green
Write-Host "✅ Phone Number ID: 959729383897901" -ForegroundColor Green
Write-Host "✅ Business Account ID: 941501588391536" -ForegroundColor Green
Write-Host "✅ Webhook Verify Token: tutormate_secure_verify_token_2026" -ForegroundColor Green
Write-Host "✅ Test Phone Number: +27 78 531 2360" -ForegroundColor Green

Write-Host "`n📝 IMPORTANT NOTES:" -ForegroundColor Yellow
Write-Host "  • This access token is TEMPORARY (valid for 24 hours)" -ForegroundColor White
Write-Host "  • For production, create a System User token in Meta Business Suite" -ForegroundColor White
Write-Host "  • Test number expires in 90 days (until ~May 24, 2026)" -ForegroundColor White
Write-Host "  • You can add up to 5 test phone numbers" -ForegroundColor White

Write-Host "`n🚀 Ready to start! Run one of these commands:" -ForegroundColor Cyan
Write-Host "  mvn spring-boot:run" -ForegroundColor White
Write-Host "  OR" -ForegroundColor Gray
Write-Host "  mvn clean package; java -jar target/twilio-whatsapp-springboot-1.0.0.jar" -ForegroundColor White

Write-Host "`n🧪 To test sending a message:" -ForegroundColor Cyan
Write-Host '  Invoke-RestMethod -Uri "http://localhost:8082/api/meta/test/send?to=27785312360&message=Hello from TutorMate!" -Method GET' -ForegroundColor White

Write-Host ""
