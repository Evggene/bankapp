# BankApp

Микросервисное банковское приложение: аккаунты, переводы, кэш, обмен валют, уведомления, gateway и UI.

## ⚙️ Локальный запуск

### Требования
- **Java 17+**
- **Maven 3.8+**
- **PostgreSQL 13+**
- **Consul** (для сервис-дискавери)
- **Keycloak** (для аутентификации)

### Сборка
```bash
mvn clean install
```

### Запуск сервисов
Каждый сервис можно поднять из своей директории:
```bash
cd accounts
mvn spring-boot:run
```

По умолчанию:
- Accounts: http://localhost:8010
- Blocker: http://localhost:8020
- Cash: http://localhost:8030
- Exchange: http://localhost:8040
- Exchange Generator: http://localhost:8050
- Notifications: http://localhost:8060
- Transfer: http://localhost:8070
- Gateway: http://localhost:8100
- Front-UI: http://localhost:8080

---

## 🐳 Запуск в Docker Compose

### 1. Сборка и запуск
```bash
docker compose up --build
```

### 2. Доступные сервисы
- **Consul UI** → [http://localhost:8500](http://localhost:8500)
- **Postgres** → порт 5431, БД bankapp, логин/пароль: user/pass
- **Keycloak** → [http://localhost:8101](http://localhost:8101), логин/пароль: admin/admin
- **Gateway** → [http://localhost:8100](http://localhost:8100)
- **Front-UI** → [http://localhost:8080](http://localhost:8080)

### 3. Перезапуск
```bash
docker compose down -v
docker compose up --build -d
```

---

## 🧩 Структура сервисов

- `accounts` — управление аккаунтами
- `blocker` — блокировка операций
- `cash` — операции со счетом
- `exchange` — конвертация валют
- `exchange-generator` — генерация курсов валют
- `notifications` — уведомления
- `transfer` — переводы
- `gateway` — API Gateway
- `front-ui` — фронтенд
