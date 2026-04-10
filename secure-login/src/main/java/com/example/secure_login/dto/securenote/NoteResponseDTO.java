package com.example.secure_login.dto.securenote;

import com.example.secure_login.entity.SecureNote.NoteCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponseDTO {
    private Long id;
    private String title;
    private String content;
    private NoteCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
