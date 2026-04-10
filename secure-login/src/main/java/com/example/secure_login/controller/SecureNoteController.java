package com.example.secure_login.controller;

import com.example.secure_login.dto.securenote.CreateRequestDTO;
import com.example.secure_login.dto.securenote.NoteResponseDTO;
import com.example.secure_login.dto.securenote.UpdateRequestDTO;
import com.example.secure_login.service.SecureNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vault/notes")
public class SecureNoteController {
    private final SecureNoteService noteService;

    /**
     * Create a new encrypted note.
     * POST /api/vault/notes
     * Body: { "title": "...", "content": "...", "category": "ACCOUNT_NUMBER" }
     * Header: Authorization: Bearer <jwt>
     */
    @PostMapping
    public ResponseEntity<NoteResponseDTO> createNote(
            @Valid @RequestBody CreateRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(noteService.createNote(request));
    }

    /**
     * Get all notes for the authenticated user (decrypted).
     * GET /api/vault/notes
     */
    @GetMapping
    public ResponseEntity<List<NoteResponseDTO>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }

    /**
     * Get a single note by ID (decrypted).
     * GET /api/vault/notes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponseDTO> getNoteById(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    /**
     * Update an existing note.
     * PUT /api/vault/notes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponseDTO> updateNote(
            @PathVariable Long id,
            @RequestBody UpdateRequestDTO request) {
        return ResponseEntity.ok(noteService.updateNote(id, request));
    }

    /**
     * Delete a note.
     * DELETE /api/vault/notes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}
