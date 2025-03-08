package com.bookstore.cart.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Entity(name = "sessions")
@Table(name = "sessions")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Session {
    @Id
    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Type(type = "jsonb")
    @Column(name = "user_data", columnDefinition = "jsonb")
    private String userData;

    @Column(name = "created_at", updatable = false, insertable = false)
    private java.sql.Timestamp createdAt;

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUserData() { return userData; }
    public void setUserData(String userData) { this.userData = userData; }
    public java.sql.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
}