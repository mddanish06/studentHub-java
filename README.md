# StudentHub

This document provides full instructions to run the Spring Boot + MySQL, including environment setup, build steps, API usage, and troubleshooting.

---

## 🚀 Project Overview

A StudentHub built using:

* **Spring Boot** (REST API)
* **MySQL Database**
* **JWT Authentication**
* **HTML/JS Frontend (Local static files)**

Swagger may not work due to security filters, so the README includes **manual API documentation**.

---

## 🛠 Requirements

Before running the project, install:

* **Java 17** or later
* **Maven 3.8+**
* **MySQL 8+**
* Any REST client (Postman/ThunderClient)

---

## 🗄️ Database Setup

1. Create database in MySQL:

```sql
CREATE DATABASE student_app;
```

2. Update `application.properties` if needed:

```
spring.datasource.url=jdbc:mysql://localhost:3306/student_app?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
```

3. Spring Boot will auto-create tables using JPA.

---

## ▶️ Run the Backend

Inside the project folder:

```bash
mvn spring-boot:run
```

The backend runs at:

```
http://localhost:8080
```

---

## 🌐 Run the Frontend (HTML Files)

Open the HTML files using **Live Server (VS Code extension)** or any static server.

Example URL:

```
http://127.0.0.1:5500/index.html
```

---

## 🔑 JWT Authentication Flow

Before calling any protected API:

### 1️⃣ Login

```
POST /api/auth/login
```

**Request Body:**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**

```json
{
  "token": "<JWT_TOKEN>"
}
```

Include this token in all further requests:

```
Authorization: Bearer <JWT_TOKEN>
```

---

# 📘 API Documentation (Manual since Swagger is not working)

## 👦 Student APIs

### ➕ Create Student

```
POST /api/students
```

**Body:**

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "9876543210",
  "dob": "2002-01-01",
  "subjects": ["Math", "Science"]
}
```

---

### 📄 Get All Students

```
GET /api/students
```

### 🔍 Get Student by ID

```
GET /api/students/{id}
```

### ✏️ Update Student

```
PUT /api/students/{id}
```

### ❌ Delete Student

```
DELETE /api/students/{id}
```

---

## 📚 Subject APIs

### ➕ Add Subject to Student

```
POST /api/students/{id}/subjects
```

**Body:**

```json
{
  "subjectName": "Mathematics"
}
```

### ❌ Delete Subject

```
DELETE /api/students/{studentId}/subjects/{subjectId}
```

---

## 🖼️ Photo Upload APIs

### 📤 Upload Photo

```
POST /api/students/{id}/photo
```

*Multipart File*

### 📥 View Photo

```
GET /api/students/photo/{filename}
```

---

# ⚙️ Troubleshooting

### ❗ Swagger shows 403 Forbidden

This is caused by the JWT filter blocking Swagger paths.
Even though the paths were added, some versions resolve resources differently, so Swagger may still not load.

This README contains full manual API docs as a fallback.

---

### ❗ CORS Error in Browser

Add this bean in `CorsConfig`:

```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(false);
        }
    };
}
```

---

# 🎯 Conclusion

This README provides:

* Full environment setup
* How to run backend/frontend
* JWT login usage
* API Endpoints
* Fixes for common issues (CORS + Swagger)
