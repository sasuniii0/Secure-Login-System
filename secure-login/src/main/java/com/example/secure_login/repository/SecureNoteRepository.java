package com.example.secure_login.repository;

import com.example.secure_login.entity.SecureNote;
import com.example.secure_login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecureNoteRepository extends JpaRepository<SecureNote, Long> {
    List<SecureNote> findAllByUser(User user);
    Optional<SecureNote> findByIdAndUser(Long id, User user);
    List<SecureNote> findAllByUserAndCategory(User user, SecureNote.NoteCategory category);

}
