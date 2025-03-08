# Bookstore

An online bookstore application with a React frontend, Spring Boot cart service, Flask catalog service, and PostgreSQL database. 
Allows users to browse a catalog, add items to a cart, checkout, and process payments.

## Project Structure
- **`bookstore/`**: React frontend (UI).
- **`cart-service/`**: Spring Boot backend for cart, orders, and payment.
- **`catalog-service/`**: Flask backend for book catalog and inventory.

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

CREATE TABLE books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    price NUMERIC(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL
);

CREATE TABLE cart_items (
    id SERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    quantity INTEGER NOT NULL
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    total_amount NUMERIC(10,2) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    book_id INTEGER,
    quantity INTEGER NOT NULL,
    price NUMERIC(10,2) NOT NULL
);

CREATE TABLE card (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    ccnum VARCHAR(255) NOT NULL,
    expdate DATE NOT NULL,
    seccode VARCHAR(4) NOT NULL
);

INSERT INTO books (title, author, price, stock_quantity) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', 12.99, 10),
('1984', 'George Orwell', 9.99, 15),
('To Kill a Mockingbird', 'Harper Lee', 15.50, 8);

### 3. Set Up Flask Catalog Service

```cd catalog-service```<br>
```pip install flask flask-cors psycopg2-binary```<br>
```python app.py```<br>
Access: http://localhost:5000/catalog

### 4. Set Up Spring Boot Cart Service

```cd cart-service```<br>
```mvn clean install```<br>
```mvn spring-boot:run```<br>

Access: http://localhost:8081/cart

### 5. Set Up React Frontend

```cd C:\Users\user\Desktop\bookstore```<br>
```npm install```<br>
```npm start```<br>

Access: http://localhost:3000
