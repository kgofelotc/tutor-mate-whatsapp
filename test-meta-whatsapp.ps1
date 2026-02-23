# Meta WhatsApp Quick Test Commands

# ============================================
# STEP 1: Set Your Environment Variables
# ============================================

# Replace these with your actual Meta credentials
$env:META_WHATSAPP_ACCESS_TOKEN="your_access_token_here"
$env:META_WHATSAPP_PHONE_NUMBER_ID="your_phone_number_id_here"
$env:META_WHATSAPP_BUSINESS_ACCOUNT_ID="your_waba_id_here"
$env:META_WEBHOOK_VERIFY_TOKEN="your_custom_verify_token_here"

# Replace with your test phone number (country code + number, no + or spaces)
$TEST_PHONE = "27821234567"

# ============================================
# STEP 2: Start Your Application
# ============================================

# Start the Spring Boot app
mvn spring-boot:run

# Wait for: "Started DemoApplication in X.XXX seconds"

# ============================================
# STEP 3: Test the Integration
# ============================================

# Test 1: Health Check
Invoke-RestMethod -Uri "http://localhost:8082/api/meta/test/health" -Method GET

# Test 2: Send Simple Text Message
Invoke-RestMethod -Uri "http://localhost:8082/api/meta/test/send?to=$TEST_PHONE&message=Hello from Meta!" -Method GET

# Test 3: Send Interactive Buttons
Invoke-RestMethod -Uri "http://localhost:8082/api/meta/test/buttons?to=$TEST_PHONE" -Method GET

# Test 4: Send Interactive List
Invoke-RestMethod -Uri "http://localhost:8082/api/meta/test/list?to=$TEST_PHONE" -Method GET

# ============================================
# STEP 4: Test Full Conversation Flow
# ============================================

# Just send "hi" or "menu" from your WhatsApp to start the conversation
# The bot will respond automatically via the webhook

# ============================================
# TROUBLESHOOTING
# ============================================

# Check application logs
# Look for:
# - "Meta WhatsApp API initialized with Phone Number ID: ..."
# - "Message sent successfully to ..."
# - "Received webhook from Meta: ..."

# Common errors:
# 1. "Invalid access token" - Check your META_WHATSAPP_ACCESS_TOKEN
# 2. "Recipient not found" - Add test number in Meta App Dashboard
# 3. "Connection refused" - Make sure app is running on port 8082

# ============================================
# WEBHOOK TESTING (After ngrok setup)
# ============================================

# 1. Start ngrok in a separate terminal:
#    ngrok http 8082

# 2. Copy the HTTPS URL (e.g., https://abc123.ngrok.io)

# 3. Configure webhook in Meta Dashboard:
#    URL: https://abc123.ngrok.io/meta/webhook
#    Verify Token: (use the same token from META_WEBHOOK_VERIFY_TOKEN)

# 4. Send a message from WhatsApp to your business number

# 5. Check logs - you should see:
#    "Received webhook from Meta: ..."
#    "Processing message from ..."

# ============================================
# USEFUL CURL EQUIVALENTS (if needed)
# ============================================

# Health check
# curl http://localhost:8082/api/meta/test/health

# Send test message
# curl "http://localhost:8082/api/meta/test/send?to=27821234567&message=Hello"

# Send buttons
# curl http://localhost:8082/api/meta/test/buttons?to=27821234567

# Send list
# curl http://localhost:8082/api/meta/test/list?to=27821234567
