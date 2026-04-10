package com.example.secure_login.service;

import com.example.secure_login.dto.securenote.CreateRequestDTO;
import com.example.secure_login.dto.securenote.NoteResponseDTO;
import com.example.secure_login.dto.securenote.UpdateRequestDTO;
import com.example.secure_login.entity.SecureNote;
import com.example.secure_login.entity.User;
import com.example.secure_login.repository.SecureNoteRepository;
import com.example.secure_login.repository.UserRepository;
import com.example.secure_login.util.AesEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecureNoteService {
    private final SecureNoteRepository noteRepository;
    private final UserRepository userRepository;
    private final AesEncryptionUtil aesEncryption;

    @Transactional
    public NoteResponseDTO createNote(CreateRequestDTO request) {
        User user = getCurrentUser();

        SecureNote note = SecureNote.builder()
                .title(request.getTitle())
                .encryptedContent(aesEncryption.encrypt(request.getContent()))
                .category(request.getCategory())
                .user(user)
                .build();

        SecureNote saved = noteRepository.save(note);
        log.info("Secure note created: id={}, user={}", saved.getId(), user.getEmail());

        return toResponse(saved, request.getContent());
    }

    @Transactional(readOnly = true)
    public List<NoteResponseDTO> getAllNotes() {
        User user = getCurrentUser();
        return noteRepository.findAllByUser(user)
                .stream()
                .map(note -> toResponse(note, aesEncryption.decrypt(note.getEncryptedContent())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NoteResponseDTO getNoteById(Long id) {
        User user = getCurrentUser();
        SecureNote note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        return toResponse(note, aesEncryption.decrypt(note.getEncryptedContent()));
    }

    @Transactional
    public NoteResponseDTO updateNote(Long id, UpdateRequestDTO request) {
        User user = getCurrentUser();
        SecureNote note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        if (request.getTitle() != null) note.setTitle(request.getTitle());
        if (request.getCategory() != null) note.setCategory(request.getCategory());

        String decryptedContent;
        if (request.getContent() != null) {
            note.setEncryptedContent(aesEncryption.encrypt(request.getContent()));
            decryptedContent = request.getContent();
        } else {
            decryptedContent = aesEncryption.decrypt(note.getEncryptedContent());
        }

        SecureNote updated = noteRepository.save(note);
        return toResponse(updated, decryptedContent);
    }

    @Transactional
    public void deleteNote(Long id) {
        User user = getCurrentUser();
        SecureNote note = noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        noteRepository.delete(note);
        log.info("Secure note deleted: id={}, user={}", id, user.getEmail());
    }


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    private NoteResponseDTO toResponse(SecureNote note, String decryptedContent) {
        return NoteResponseDTO.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(decryptedContent)
                .category(note.getCategory())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
