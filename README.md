# Bookstore

An online bookstore application with a React frontend, Spring Boot cart service, Flask catalog service, and PostgreSQL database. Supports browsing, cart management, and checkout with Docker and Kubernetes deployment options.

## Project Structure
- **`frontend/`**: React frontend (UI).
- **`backend/cart-service/`**: Spring Boot backend for cart and orders.
- **`backend/catalog-service/`**: Flask backend for book catalog.
- **`database/`**: PostgreSQL setup.
- **`k8s/`**: Kubernetes deployment files.
- **`ci-cd/`**: CI/CD automation (to be implemented).
- **`docker-compose.yml`**: Local development with Docker Compose.

## Prerequisites
Ensure you have the following installed with these versions:

| Software          | Version       | Download Link                                  |
|-------------------|---------------|------------------------------------------------|
| Node.js           | 18.17.0       | [nodejs.org](https://nodejs.org/dist/v18.17.0/) |
| Java JDK          | 11            | [adoptium.net](https://adoptium.net/temurin/releases/?version=11) |
| Maven             | 3.9.6         | [maven.apache.org](https://maven.apache.org/download.cgi) |
| Python            | 3.11.5        | [python.org](https://www.python.org/downloads/release/python-3115/) |
| PostgreSQL        | 15.4          | [postgresql.org](https://www.postgresql.org/download/) |
| Git               | 2.43.0        | [git-scm.com](https://git-scm.com/downloads) |

- **Operating System**: Tested on Windows 10/11 (should work on macOS/Linux with minor adjustments).

## Setup Instructions

### 1. Clone the Repository
`git clone https://github.com/codemissile/bookstore.git
cd bookstore`

### 2. Set Up PostgreSQL
user:postgres
password:postgres

open psql cmd:

CREATE DATABASE bookstore;
```\c bookstore```

Database Initialization:
The cart-service uses schema.sql and data.sql to initialize the database on startup.

### 3. Set Up Flask Catalog Service

**Catalog Service**
```sh
cd backend/catalog-service
pip install flask flask-cors psycopg2-binary
python app.py
```
Access: http://localhost:5000/catalog

### 4. Set Up Spring Boot Cart Service

**Cart Service**
   ```sh
   cd backend/cart-service
   mvn clean install
   mvn spring-boot:run
   ```

Access: http://localhost:8081/cart

### 5. Set Up React Frontend

```sh
cd C:\Users\user\bookstore\frontend
npm install
npm start
```

Access: http://localhost:3000
