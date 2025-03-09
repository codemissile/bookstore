import os
from flask import Flask, jsonify, request
from flask_cors import CORS
import psycopg2
from psycopg2.extras import RealDictCursor

app = Flask(__name__)
CORS(app)

# Obtener valores de las variables de entorno
DB_HOST = os.getenv("DB_HOST", "postgres")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME", "bookstore")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASSWORD = os.getenv("DB_PASSWORD", "postgres")

def get_db_connection():
    conn = psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        database=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD
    )
    return conn

@app.route("/catalog", methods=["GET"])
def get_catalog():
    try:
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute("SELECT id, title, author, price, stock_quantity FROM books")
        books = cur.fetchall()
        cur.close()
        conn.close()
        return jsonify(books)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/inventory/<int:book_id>", methods=["GET"])
def get_book_stock(book_id):
    try:
        conn = get_db_connection()
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute("SELECT title, price, stock_quantity FROM books WHERE id = %s", (book_id,))
        book = cur.fetchone()
        cur.close()
        conn.close()
        if book:
            return jsonify({"title": book["title"], "price": book["price"], "stock": book["stock_quantity"]})
        return jsonify({"error": "Book not found"}), 404
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/inventory/update", methods=["POST"])
def update_stock():
    data = request.json
    book_id = data.get("bookId")
    quantity = data.get("quantity")
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT stock_quantity FROM books WHERE id = %s", (book_id,))
        stock = cur.fetchone()
        if not stock:
            cur.close()
            conn.close()
            return jsonify({"error": "Book not found"}), 404
        if stock[0] < quantity:
            cur.close()
            conn.close()
            return jsonify({"error": "Insufficient stock"}), 400
        cur.execute("UPDATE books SET stock_quantity = stock_quantity - %s WHERE id = %s", (quantity, book_id))
        conn.commit()
        cur.close()
        conn.close()
        return jsonify({"message": "Stock updated"})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
