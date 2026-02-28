# 🧾 Hisaab-Khata — Shopkeeper Ledger & Inventory Management

Hisaab-Khata is a Spring Boot–based ledger and inventory management system designed for small shopkeepers.  
It helps maintain suppliers, items, purchases, sales, units, staff management, authentication, and full business accounting.

---

## 🚀 Features

### ✅ Authentication & Authorization
- JWT-based authentication
- Role-based access (Admin / Staff)
- Secure endpoints

### 🛒 Inventory Management
- Item master
- Units and conversions
- Purchases & Sales
- Stock tracking

### 👥 Supplier Management
- Add, update, search suppliers
- Shop-wise filtering

### 🧾 Ledger & Payments
- Ledger entries
- Payment modes
- Credit/Debit tracking

### 👨‍💼 Staff Management
- Staff creation
- Role-based access control

---

## 🏗 Tech Stack

- **Java 17+**
- **Spring Boot 3+**
- **Spring Security 6**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **MapStruct (optional)**
- **Maven**

---

## 📁 Project Structure

```
hisaab-khata/
 ├── src/main/java/com/hisaab_khata/
 │   ├── controller
 │   ├── service
 │   ├── repository
 │   ├── entity
 │   ├── config
 │   └── util
 ├── src/main/resources/
 │   ├── application.properties
 │   └── schema.sql / data.sql (optional)
 ├── pom.xml
 └── README.md
```

---

## ⚙️ Setup Instructions

### 1️⃣ Clone Repo
```bash
git clone https://github.com/sorov0/Hisaab-Khata.git
cd Hisaab-Khata
```

### 2️⃣ Configure Database
Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hisaabkhata
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

### 3️⃣ Run Project
```bash
mvn spring-boot:run
```

---

## 🧪 API Testing (Postman)

The project includes:
- Login API
- Supplier API
- Item API
- Ledger API
- Staff API

JWT token required for protected endpoints.

---

## 🤝 Contributing

Feel free to fork this repository and submit pull requests.

---

## 📜 License

This project is licensed under the MIT License.

---

## 👨‍💻 Author

**Saurav Kumar**  
GitHub: [https://github.com/sorov0](https://github.com/sorov0)

