# 🚀 Deploy to Render - Quick Setup Script

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "   TutorMate - Render Deployment Helper   " -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

# Check if git is initialized
if (!(Test-Path ".git")) {
    Write-Host "⚠️  Git repository not initialized!" -ForegroundColor Yellow
    Write-Host "   Run: git init" -ForegroundColor White
    exit 1
}

# Check for uncommitted changes
$gitStatus = git status --porcelain
if ($gitStatus) {
    Write-Host "📝 Uncommitted changes detected" -ForegroundColor Yellow
    Write-Host ""
    
    $commit = Read-Host "Commit changes? (y/n)"
    if ($commit -eq "y") {
        Write-Host ""
        $commitMsg = Read-Host "Enter commit message (or press Enter for default)"
        if (!$commitMsg) {
            $commitMsg = "Deploy to Render with enhanced features"
        }
        
        git add .
        git commit -m $commitMsg
        Write-Host "✅ Changes committed" -ForegroundColor Green
    }
}

# Check for GitHub remote
$remotes = git remote -v
if (!$remotes -or !($remotes -match "origin")) {
    Write-Host ""
    Write-Host "⚠️  No GitHub remote configured!" -ForegroundColor Yellow
    Write-Host "   1. Create a GitHub repository" -ForegroundColor White
    Write-Host "   2. Run: git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git" -ForegroundColor White
    Write-Host "   3. Run this script again" -ForegroundColor White
    exit 1
}

# Push to GitHub
Write-Host ""
Write-Host "📤 Pushing to GitHub..." -ForegroundColor Cyan
try {
    git push -u origin main 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        # Try master branch if main fails
        git push -u origin master 2>&1 | Out-Null
    }
    Write-Host "✅ Code pushed to GitHub" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to push. Check your GitHub credentials" -ForegroundColor Red
    exit 1
}

# Display next steps
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "   Next Steps: Render Setup                " -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "1️⃣  Go to: https://render.com" -ForegroundColor White
Write-Host "2️⃣  Sign in with GitHub" -ForegroundColor White
Write-Host "3️⃣  Click: New + → Web Service" -ForegroundColor White
Write-Host "4️⃣  Connect your repository" -ForegroundColor White
Write-Host "5️⃣  Configure:" -ForegroundColor White
Write-Host "     • Name: tutormate-whatsapp" -ForegroundColor Gray
Write-Host "     • Runtime: Docker" -ForegroundColor Gray
Write-Host "     • Plan: Free (for testing)" -ForegroundColor Gray
Write-Host ""
Write-Host "6️⃣  Set Environment Variables:" -ForegroundColor White
Write-Host ""

# Prompt for Twilio credentials
Write-Host "Let me help you prepare the environment variables..." -ForegroundColor Cyan
Write-Host ""

$accountSid = Read-Host "Enter TWILIO_ACCOUNT_SID (or press Enter to skip)"
$authToken = Read-Host "Enter TWILIO_AUTH_TOKEN (or press Enter to skip)"
$whatsappFrom = Read-Host "Enter TWILIO_WHATSAPP_FROM (default: +14155238886)"

if (!$whatsappFrom) {
    $whatsappFrom = "+14155238886"
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
Write-Host "   Copy These to Render Dashboard:        " -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
Write-Host ""

if ($accountSid) {
    Write-Host "TWILIO_ACCOUNT_SID=$accountSid" -ForegroundColor White
} else {
    Write-Host "TWILIO_ACCOUNT_SID=your_account_sid_here" -ForegroundColor Yellow
}

if ($authToken) {
    Write-Host "TWILIO_AUTH_TOKEN=$authToken" -ForegroundColor White
} else {
    Write-Host "TWILIO_AUTH_TOKEN=your_auth_token_here" -ForegroundColor Yellow
}

Write-Host "TWILIO_WHATSAPP_FROM=$whatsappFrom" -ForegroundColor White
Write-Host "SERVER_PORT=8082" -ForegroundColor White
Write-Host "SPRING_DATASOURCE_URL=jdbc:h2:mem:tutormate" -ForegroundColor White
Write-Host "SPRING_JPA_HIBERNATE_DDL_AUTO=update" -ForegroundColor White
Write-Host ""

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "7️⃣  Click 'Create Web Service'" -ForegroundColor White
Write-Host "8️⃣  Wait for deployment (~5-10 minutes)" -ForegroundColor White
Write-Host "9️⃣  Get your URL (e.g., https://tutormate-whatsapp.onrender.com)" -ForegroundColor White
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "   Twilio Webhook Setup                   " -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "🔟 Go to: Twilio Console → WhatsApp Sandbox Settings" -ForegroundColor White
Write-Host "1️⃣1️⃣  Set webhook URL:" -ForegroundColor White
Write-Host "     https://your-app-name.onrender.com/twilio/webhook" -ForegroundColor Gray
Write-Host "1️⃣2️⃣  Method: POST" -ForegroundColor White
Write-Host "1️⃣3️⃣  Save!" -ForegroundColor White
Write-Host ""

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
Write-Host "   Test Your Deployment                   " -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
Write-Host ""
Write-Host "1️⃣4️⃣  Send 'HI' to your Twilio WhatsApp number" -ForegroundColor White
Write-Host "1️⃣5️⃣  Follow the interactive prompts" -ForegroundColor White
Write-Host "1️⃣6️⃣  Test booking a session" -ForegroundColor White
Write-Host ""

Write-Host "📚 For detailed instructions, see RENDER_DEPLOYMENT.md" -ForegroundColor Cyan
Write-Host ""
Write-Host "✨ Good luck with your deployment!" -ForegroundColor Magenta
Write-Host ""
