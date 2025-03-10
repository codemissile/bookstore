import os
from flask import Flask, jsonify, request
import psycopg2
from flask_cors import CORS
import logging
from psycopg2.extras import RealDictCursor

app = Flask(__name__)
CORS(app)
logging.basicConfig(level=logging.DEBUG)

# Obtener valores de las variables de entorno
DB_HOST = os.getenv("DB_HOST", "postgres")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME", "bookstore")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASSWORD = os.getenv("DB_PASSWORD", "postgres")

def get_db_connection():
    return psycopg2.connect(dbname=DB_NAME, user=DB_USER, password=DB_PASSWORD, host=DB_HOST, port=DB_PORT)

@app.route('/catalog', methods=['GET'])
def get_catalog():
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute("SELECT id, title, author, price, stock_quantity, image_url FROM books")
    books = [{"id": row[0], "title": row[1], "author": row[2], "price": row[3], "stock_quantity": row[4], "image_url": row[5]} for row in cur.fetchall()]
    cur.close()
    conn.close()
    return jsonify(books)

@app.route('/inventory/<int:book_id>', methods=['GET'])
def get_book(book_id):
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute("SELECT id, title, author, price, stock_quantity, image_url FROM books WHERE id = %s", (book_id,))
    row = cur.fetchone()
    cur.close()
    conn.close()
    if row:
        return jsonify({"id": row[0], "title": row[1], "author": row[2], "price": row[3], "stock_quantity": row[4], "image_url": row[5]})
    return jsonify({"error": "Book not found"}), 404

@app.route('/inventory/update', methods=['POST'])
def update_inventory():
    data = request.get_json()
    book_id = data['bookId']
    quantity = data['quantity']
    logging.debug(f"Updating stock for book_id {book_id} by reducing {quantity}")
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute("UPDATE books SET stock_quantity = stock_quantity - %s WHERE id = %s", (quantity, book_id))
    conn.commit()
    cur.close()
    conn.close()
    return "Stock updated", 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)