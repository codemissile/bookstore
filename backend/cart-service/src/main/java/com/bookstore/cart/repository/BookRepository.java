package com.bookstore.cart.repository;

import com.bookstore.cart.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}