
# Library Management API

A simple RESTful API to manage users, books, checkouts, and notifications for a library system.
- [Flow Diagram](#flow-diagram)
- [Postman Testing](#postman-testing)
---
## 🔗 Base URL

http://localhost:9000/ (Runs Locally)
---
## 📦 Endpoints

### 👤 User Routes

| Method | Endpoint     | Description       |
|--------|--------------|-------------------|
| POST   | `/users`     | Create a new user |
| GET    | `/users`     | List all users    |

---

### 📘 Book Routes

| Method | Endpoint     | Description         |
|--------|--------------|---------------------|
| POST   | `/books`     | Create a new book   |
| GET    | `/books`     | List all books      |

---

### 🔄 Checkout Routes

| Method | Endpoint                              | Description                          |
|--------|----------------------------------------|--------------------------------------|
| POST   | `/checkouts`                          | Create a new book checkout           |
| POST   | `/checkouts/:checkoutId/return`       | Return a book using checkout ID      |

---

### 🔔 Notification Routes

| Method | Endpoint           | Description                |
|--------|--------------------|----------------------------|
| GET    | `/notifications`   | Get all notification logs  |

---

## 🧪 Example Payloads

### Create User

```json
POST /users
{
  "name": "Alice Smith",
  "email": "alice@example.com"
}
```
### Create Book
```json
POST /books
{
  "title": "1984",
  "author": "George Orwell"
}
```

### Create Checkout
```json
POST /checkouts
{
  "userId": 1,
  "bookId": 2
}
```
# 🛠️ Tech Stack

- Scala (Play Framework)
- Pekko (Akka) Scheduler
- Async / Futures
 - Postgres DB

🚧 Notes
Overdue checkouts are checked automatically every 40 seconds.

Users with overdue books will receive notifications via the NotificationService.

Fines are calculated automatically for overdue checkouts.

### Flow Diagram
![Flow diagram](https://github.com/user-attachments/assets/8ec153cc-8859-46eb-abbd-57c67d828a15)

### Postman Testing
<img width="315" height="410" alt="image" src="https://github.com/user-attachments/assets/0b409743-5593-449a-b485-59cb67d3b14c" />

