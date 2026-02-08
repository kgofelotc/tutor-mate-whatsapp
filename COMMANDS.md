# 🚀 Quick Command Reference

## Start Application
```powershell
cd c:\repository\personal\twilio-whatsapp-springboot
mvn spring-boot:run
```

## Test Messages (Run in NEW PowerShell window while server is running)

### Quick Test
```powershell
Invoke-RestMethod -Uri "http://localhost:8082/api/whatsapp/send-test" -Method POST
```

### Interactive Test Script
```powershell
.\test-whatsapp.ps1
```

### Send Custom Text Message
```powershell
$body = '{"to":"27662035457","message":"Hello from Infobip!"}' 
Invoke-RestMethod -Uri "http://localhost:8082/api/whatsapp/send-text" `
    -Method POST -Body $body -ContentType "application/json"
```

### Send Template Message
```powershell
$body = '{"to":"27662035457","templateName":"test_whatsapp_template_en","language":"en","placeholders":["Tebogo"]}'
Invoke-RestMethod -Uri "http://localhost:8082/api/whatsapp/send-template" `
    -Method POST -Body $body -ContentType "application/json"
```

## Check Status

### Health Check
```powershell
Invoke-RestMethod -Uri "http://localhost:8082/actuator/health"
```

### Open Swagger UI
```powershell
Start-Process "http://localhost:8082/swagger-ui/index.html"
```

### Open H2 Console
```powershell
Start-Process "http://localhost:8082/h2-console"
```
(JDBC URL: `jdbc:h2:mem:testdb`, Username: `sa`, Password: `password`)

## Build Commands

### Clean & Compile
```powershell
mvn clean compile
```

### Package Application
```powershell
mvn clean package
```

### Run Tests
```powershell
mvn test
```

## Troubleshooting

### Check Port 8082
```powershell
netstat -ano | findstr :8082
```

### Kill Process on Port 8082
```powershell
$port = Get-NetTCPConnection -LocalPort 8082 -ErrorAction SilentlyContinue
if ($port) { Stop-Process -Id $port.OwningProcess -Force }
```

## Important URLs

| Service | URL |
|---------|-----|
| Application | http://localhost:8082 |
| Swagger UI | http://localhost:8082/swagger-ui/index.html |
| H2 Console | http://localhost:8082/h2-console |
| Health Check | http://localhost:8082/actuator/health |
| Test Endpoint | http://localhost:8082/api/whatsapp/send-test |

## Configuration

- **API Key**: `05e2528d857d11fc3bb326170342ded4-4098c481-33a1-43e3-9d0a-d195776ce9aa`
- **Base URL**: `https://e5v55q.api.infobip.com`
- **Sender**: `447860088970`
- **Test Recipient**: `27662035457`
- **Template**: `test_whatsapp_template_en`
