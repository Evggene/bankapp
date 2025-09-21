# BankApp

Микросервисное банковское приложение: аккаунты, переводы, кэш, обмен валют, уведомления, gateway и UI.

## ⚙️ Локальный запуск

### Требования
- **Java 17+**
- **Maven 3.8+**
- **PostgreSQL 13+**
- **Keycloak** (для аутентификации)
- **Kafka**

## Что нового
В проект добавлена **Apache Kafka** и реализовано межсервисное взаимодействие через события:
- **exchange-generator** публикует курсы валют в топик `exchange.rates`
- **transfer** и **cash** публикуют доменные события в топик `notifications.events`
- **notifications** подписывается на `notifications.events` и отображает эти события на фронте

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
- Front-UI: http://localhost:8080

---

## 🐳 Запуск в Docker Compose

### 1. Сборка и запуск
```bash
docker compose up --build
```

### 2. Доступные сервисы
- **Postgres** → порт 5431, БД bankapp, логин/пароль: user/pass
- **Keycloak** → [http://localhost:8101](http://localhost:8101), логин/пароль: admin/admin
- **Front-UI** → [http://loca…

</details>
