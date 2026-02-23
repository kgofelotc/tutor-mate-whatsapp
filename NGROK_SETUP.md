# ngrok Setup Guide

## Step 1: Create ngrok Account (FREE)

1. Go to: https://dashboard.ngrok.com/signup
2. Sign up with email or GitHub (it's free!)
3. Verify your email if required

## Step 2: Get Your Authtoken

1. After login, go to: https://dashboard.ngrok.com/get-started/your-authtoken
2. Copy your authtoken (looks like: `2abc123def456ghi789jkl0`)

## Step 3: Configure ngrok

Run this command in PowerShell (replace YOUR_TOKEN with actual token):

```powershell
ngrok config add-authtoken YOUR_TOKEN
```

Example:
```powershell
ngrok config add-authtoken 2abc123def456ghi789jkl0
```

You should see:
```
Authtoken saved to configuration file: C:\Users\YourName\.ngrok2\ngrok.yml
```

## Step 4: Start ngrok

Now run:
```powershell
ngrok http 8082
```

You should see:
```
Session Status                online
Account                       your.email@example.com
Version                       3.3.1
Region                        United States (us)
Forwarding                    https://xxxx-xx-xx.ngrok-free.app -> http://localhost:8082
```

## Step 5: Copy Your ngrok URL

Copy the HTTPS forwarding URL (e.g., `https://1234-56-78.ngrok-free.app`)

Then proceed to configure Meta webhook!

---

## Troubleshooting

**Error: "authentication failed"**
- Make sure you copied the complete authtoken
- Run the `ngrok config add-authtoken` command again

**Error: "account limit reached"**
- Free tier allows 1 online ngrok agent at a time
- Stop any other running ngrok processes

**ngrok URL changes every restart**
- This is normal for free tier
- For static URLs, upgrade to paid plan ($10/month)
- Just update Meta webhook URL when ngrok restarts
