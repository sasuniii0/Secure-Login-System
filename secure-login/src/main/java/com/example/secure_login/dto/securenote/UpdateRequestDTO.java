package com.example.secure_login.dto.securenote;

import com.example.secure_login.entity.SecureNote.NoteCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequestDTO {
    private String title;
    private String content;
    private NoteCategory category;
}
