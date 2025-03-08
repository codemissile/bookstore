-- Books table: Stores book catalog
CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) UNIQUE NOT NULL,
    author VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INT NOT NULL CHECK (stock_quantity >= 0),
    image_url VARCHAR(255)
);

-- Sessions table: Tracks user sessions
CREATE TABLE IF NOT EXISTS sessions (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) UNIQUE NOT NULL,
    user_data JSONB, -- Optional for future user data storage
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cart_items table: Holds items in active carts
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    book_id BIGINT NOT NULL,
    price DOUBLE PRECISION NOT NULL CHECK (price >= 0),
    quantity INT NOT NULL CHECK (quantity > 0),
    title VARCHAR(255),
    CONSTRAINT cart_items_session_id_fkey FOREIGN KEY (session_id) REFERENCES sessions(session_id) ON DELETE CASCADE,
    CONSTRAINT cart_items_book_id_fkey FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    UNIQUE (session_id, book_id) -- Prevents duplicate book entries per session
);

-- Orders table: Records completed orders
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    customer_name VARCHAR(255) DEFAULT 'Anonymous',
    total_amount NUMERIC(10, 2) NOT NULL CHECK (total_amount >= 0),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order_items table: Links orders to books
CREATE TABLE IF NOT EXISTS order_items (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT order_items_book_id_fkey FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- Drop redundant 'book' table if it exists
DROP TABLE IF EXISTS book;