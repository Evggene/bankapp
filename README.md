# BankApp

Микросервисное банковское приложение: аккаунты, переводы, кэш, обмен валют, уведомления, gateway и UI.  
Полностью контейнеризовано и может быть развёрнуто в Kubernetes через Helm umbrella-chart `bankapp-chart`.

## ⚙️ Требования для локального запуска

- **Java 21+**
- **Maven 3.8+**
- **Docker / Docker Compose**
- **Minikube** (для локального Kubernetes)
- **Helm 3+**
- **kubectl**
- **Optional:** DBeaver / pgAdmin для подключения к PostgreSQL

---

## 🚀 Сборка и запуск

### 📦 Сборка приложений
```bash
mvn clean install
```

### ☸️ Развёртывание в Kubernetes через Helm

```bash
helm dependency update bankapp-chart
helm upgrade --install bankapp .bankapp-chart -n bankapp --create-namespace -f .bankapp-chart/values.yaml -f .bankapp-chart/prometheus.values.yaml -f .bankapp-chart/grafana.values.yaml
```

---

## 🧩 Модули и сервисы

| Сервис               | Порт | Назначение                                  |
|-----------------------|------|----------------------------------------------|
| Accounts              | 8010 | Регистрация и аутентификация пользователей   |
| Blocker               | 8020 | Проверка операций на блокировки             |
| Cash                  | 8030 | Операции с наличными                        |
| Exchange              | 8040 | Конвертация валют                           |
| Exchange Generator    | 8050 | Публикация курсов валют                     |
| Notifications         | 8070 | Получение событий и UI уведомления          |
| Transfer              | 8090 | Денежные переводы                           |
| Front-UI              | 8060 | Веб-интерфейс                     |    
| PostgreSQL            | 5431 | Основная БД                                 |
| Keycloak              | 8101 | Авторизация и токены                        |
| Kafka                 | 9092 | Сообщения между сервисами                  |
| Zipkin                | 9411 | Трассировка запросов                       |
| Prometheus            | 9090 | Сбор метрик                                |
| Grafana               | 3000 | Дашборды и визуализация                    |
| OpenSearch            | 9200 | Хранилище логов                            |
| OpenSearch Dashboards | 5601 | Веб-интерфейс поиска и анализа логов       |
| Logstash              | 5044 | Приём логов от микросервисов              |

---

## 📡 Kafka — топики

| Топик                   | Назначение                                | Продюсер               | Консьюмеры            |
|--------------------------|--------------------------------------------|-------------------------|-----------------------|
| `exchange.rates`        | Публикация курсов валют                   | exchange-generator     | exchange             |
| `notifications.events`  | Уведомления о доменных событиях          | transfer, cash         | notifications        |
| `bankapp-logs`         | Отправка логов микросервисов → Logstash  | все микросервисы      | Logstash → OpenSearch |

---

## 🔥 Observability (Наблюдаемость)

### 📊 Prometheus + Grafana

- **Prometheus** собирает метрики со всех сервисов (`/actuator/prometheus`), включая HTTP-метрики, JVM, пользовательские счётчики (например, успешный/неуспешный вход).
- **Grafana** разворачивается как Helm сабчарт с автоматическим провайдером дашбордов.  
  Включены:
    - `BankApp - HTTP Service Overview` — RPS, 4xx/5xx, латентность, кастомные метрики Accounts (signup/login)
    - `BankApp - JVM Overview` — память, GC, CPU, потоки

URL: [http://grafana.192.168.49.2.nip.io](http://grafana.192.168.49.2.nip.io)

---

### 🧠 Zipkin

Включён для трассировки распределённых запросов между сервисами через Spring Cloud Sleuth / Micrometer Tracing.

URL: [http://zipkin.192.168.49.2.nip.io/zipkin](http://zipkin.192.168.49.2.nip.io/zipkin)

---

### 🪵 Логирование: Logstash + OpenSearch + OpenSearch Dashboards

Каждый микросервис настроен на отправку логов в Kafka топик `bankapp-logs` через Log4j2 Kafka Appender.  
Далее:
1. Logstash читает из топика `bankapp-logs`.
2. Logstash парсит сообщения в JSON и пишет в OpenSearch.
3. OpenSearch Dashboards отображает логи.

- Dashboards URL: [http://192.168.49.2.nip.io/dashboards]

---

## 🧪 Проверка метрик и логов

### Метрики
```bash
kubectl port-forward svc/bankapp-prometheus-server 9090:80 -n bankapp
# http://localhost:9090
```

Примеры PromQL:
```promql
sum by (result) (rate(accounts_signup_total[5m]))
sum by (result) (rate(accounts_login_total[5m]))
```

---

## 🧼 Удаление

```bash
helm uninstall bankapp -n bankapp
kubectl delete ns bankapp
```

---

## 📝 Примечания

- Все сервисы используют общий `values.yaml` с глобальными настройками IP и тегов образов.
- nip.io автоматически подставляет IP Minikube в DNS-имя, что упрощает доступ через Ingress.
- Grafana, Zipkin и OpenSearch можно выключать через `values.yaml`, если не нужны.
