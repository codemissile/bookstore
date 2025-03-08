package com.bookstore.cart.repository;

import com.bookstore.cart.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, String> {
}