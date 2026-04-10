package com.example.secure_login.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "secure_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecureNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoteCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum NoteCategory {
        ACCOUNT_NUMBER,
        PASSWORD,
        ID_CARD,
        CREDIT_CARD,
        API_KEY,
        OTHER
    }
}
