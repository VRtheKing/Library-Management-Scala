# 📚 Library Management API

A RESTful API for managing users, books, checkouts, and notifications in a library system.

----------

## 📍 Base URL

`http://localhost:9000/` 

----------

## 📌 Table of Contents

- [Routes](#%EF%B8%8F-routes)
  - [User Routes](#-user-routes)
  - [Book Routes](#-book-routes)
  - [Checkout Routes](#-checkout-routes)
  - [Notification Routes](#-notification-route)
- [📦 Example Payloads](#-example-payloads)
- [📊 Flow Diagram](#-flow-diagram)
- [🧪 Postman Testing](#-postman-testing)
- [🛠️ Tech Stack](#%EF%B8%8F-tech-stack)
- [📡 gRPC Notification Service](#-grpc-notification-service)
- [🚧 Notes](#-notes)


----------

## 🛤️ Routes

### 👤 User Routes


| Method | Endpoint         | Description                    |
|--------|------------------|--------------------------------|
| POST   | `/users`         | Create a new user              |
| GET    | `/users`         | List all users                 |
| PATCH  | `/users`         | Update a user by ID            |
| GET    | `/borrowedBooks` | List books borrowed by a user  |

----------

### 📘 Book Routes


| Method | Endpoint   | Description         |
|--------|------------|---------------------|
| POST   | `/books`   | Create a new book   |
| GET    | `/books`   | List all books      |
| PATCH  | `/books`   | Update book details |


----------

### 🔄 Checkout Routes


| Method | Endpoint                          | Description                    |
|--------|-----------------------------------|--------------------------------|
| GET    | `/checkouts`                      | Get checkouts by status        |
| POST   | `/checkouts`                      | Create a new book checkout     |
| PATCH  | `/checkout`                       | Update checkout details        |
| POST   | `/checkouts/:checkoutId/return`   | Return a book by checkout ID   

----------

### 🔔 Notification Route


| Method | Endpoint         | Description                |
|--------|------------------|----------------------------|
| GET    | `/notifications` | Get all notification logs  |


----------

## 📦 Example Payloads

### ➕ Create User

```
POST /users 
{  
	"name":  "Jhonny Cage",
	"email":  "jcage@mk.com"  
}
``` 

### ✏️ Update User

```
PATCH /users 
{  
	"id":  1,
	"name":  "Jaqqci Briggs"
}
```

----------

### ➕ Create Book

```
POST /books 
{  
	"title":  "1984 - The War that Never Started",
	"author":  "George Orwell",
	"isbn":  "784-1-234-87562-7",
	"stock":  3  
}
```

### ✏️ Update Book

```
PATCH /books 
{  
	"id":  2,
	"stock":  5  
}
``` 

----------

### ➕ Create Checkout

```
POST /checkouts 
{  
	"userId":  1,
	"bookId":  2  
}
```

### 🔁 Return Book

`POST /checkouts/1/return` 

----------

### ✏️ Update Checkout

```
PATCH /checkout 
{  
	"id":  1,  
	"returned":  true,
	"fine":  5.00  
}
``` 

----------
## 🗄️ Database Table Schemas

### User Table

| Column     | Type             | Constraints                  |
|------------|------------------|------------------------------|
| id         | Long             | Primary Key, Auto Increment   |
| name       | String           |                              |
| email      | String           | Unique                       |
| created_at | LocalDateTime    |                              |

---

### Book Table

| Column     | Type             | Constraints                  |
|------------|------------------|------------------------------|
| id         | Long             | Primary Key, Auto Increment   |
| title      | String           |                              |
| author     | String           |                              |
| isbn       | String           |                              |
| stock      | Int              |                              |
| fine       | Int              |                              |
| updated_at | LocalDateTime    |                              |

---

### Checkout Table

| Column      | Type           | Constraints                         |
|-------------|----------------|-----------------------------------|
| id          | Long           | Primary Key, Auto Increment         |
| user_id     | Long           | Foreign Key → users(id), Cascade   |
| book_id     | Long           | Foreign Key → books(id), Cascade   |
| due_date    | LocalDate      |                                   |
| return_date | LocalDate?     | Nullable                          |
| fine        | BigDecimal?    | Nullable                          |
| returned    | Boolean        |                                   |

---

### Notification Table

| Column      | Type           | Constraints                      |
|-------------|----------------|--------------------------------|
| id          | Long           | Primary Key, Auto Increment      |
| message     | String         |                                |
| created_at  | LocalDateTime  |                                |


----------

## 📊 Flow Diagram

![Flow diagram](https://github.com/user-attachments/assets/8ec153cc-8859-46eb-abbd-57c67d828a15)

----------

## 🧪 Postman Testing

<img width="350" height="705" alt="image" src="https://github.com/user-attachments/assets/e590f514-ecbd-4ca5-8940-54809f37e58d" />

----------

## 🛠️ Tech Stack

-   **Language**: Scala
    
-   **Framework**: Play Framework
    
-   **Scheduler**: Pekko (Akka)
    
-   **Database**: PostgreSQL (via Slick ORM)
    
-   **Async**: Futures-based concurrency
    
-   **Notifications**: gRPC-based streaming service
    

----------

## 📡 gRPC Notification Service

**Proto definition:**

```proto
syntax = "proto3";

package service.notification;

message OverdueBook {
    int64 bookId = 1;
    string dueDate = 2;
    int32 fine = 3;
}

message Notification {
    string username = 1;
    int64 userId = 2;
    repeated OverdueBook overdueBooks = 3;
}

message SubscribeRequest {}

service NotificationService {
    rpc SubscribeNotifications(SubscribeRequest) returns (stream Notification);
}
```

-   **gRPC Server**: `localhost:50051`
    

----------

## 🚧 Notes

-   Overdue checkouts are **automatically checked every 5 minutes** via scheduled tasks.
    
-   Users with overdue books will receive real-time notifications via the **gRPC NotificationService**.
    
-   **Fines are auto-calculated** and updated in the checkout record.
    
-   Notifications are also **persisted in the database** and can be queried via `/notifications`.
