package com.tamdao.taixiu_be.repository;

import com.tamdao.taixiu_be.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    Page<ChatMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT c FROM ChatMessage c ORDER BY c.createdAt DESC LIMIT 50")
    List<ChatMessage> findLast50Messages();
}
