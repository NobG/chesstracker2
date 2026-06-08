package com.nobg.chesstracker2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "motivation_quotes")
public class MotivationQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String quoteText;

    private String author;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public void setQuoteText(String quoteText) {
        this.quoteText = quoteText;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
