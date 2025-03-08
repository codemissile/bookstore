package com.bookstore.cart.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cart_items") // Explicit table name (preferred over @Entity(name))
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false) // Maps to session_id, required
    private String sessionId;

    @Column(name = "book_id", nullable = false) // Maps to book_id, required
    private Long bookId;

    @Column(nullable = false) // Title can be nullable in DB, but let’s keep it required for consistency
    private String title;

    @Column(nullable = false) // Matches double precision, required
    private double price;

    @Column(nullable = false) // Matches integer, required
    private int quantity;

    // Constructors
    public CartItem() {}

    public CartItem(String sessionId, Long bookId, String title, double price, int quantity) {
        this.sessionId = sessionId;
        this.bookId = bookId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}