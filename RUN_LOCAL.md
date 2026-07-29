# Chạy dự án local

Chỉ Redis chạy bằng Docker. Backend, admin, storefront và AI service chạy trực tiếp trong các cửa sổ PowerShell riêng.

## Chuẩn bị lần đầu

Mở Docker Desktop, sau đó chạy:

```powershell
.\setup-local-tools.cmd
```

Cấu hình local được đọc từ:

- `backend/.env`
- `frontend/admin/.env.local`
- `frontend/storefront/.env.local`

Các file này chứa thông tin riêng và không được commit. Các file `*.example` là cấu hình mẫu an toàn để đưa lên Git.

## Chạy từng tác vụ

### 1. Redis

```powershell
docker compose up -d redis
```

Dừng Redis:

```powershell
docker compose stop redis
```

### 2. Backend

Mở một cửa sổ PowerShell riêng:

```powershell
.\start-backend.cmd
```

### 3. Admin

Mở một cửa sổ PowerShell riêng:

```powershell
.\start-admin.cmd
```

### 4. Storefront

Mở một cửa sổ PowerShell riêng:

```powershell
.\start-storefront.cmd
```

### 5. AI forecasting (khi cần)

```powershell
.\start-ai-service.ps1
```

Dừng backend, admin, storefront hoặc AI service bằng `Ctrl+C` trong đúng cửa sổ đang chạy tác vụ đó.

## Địa chỉ local

- Storefront: <http://localhost:3000>
- Admin: <http://localhost:3001>
- AI forecasting: <http://localhost:8081>
- Backend: <http://localhost:8082>
- Backend health: <http://localhost:8082/actuator/health>
- Redis: `localhost:6379`

Dữ liệu chính nằm trên Supabase. Docker Compose không chạy PostgreSQL, backend hoặc frontend.
