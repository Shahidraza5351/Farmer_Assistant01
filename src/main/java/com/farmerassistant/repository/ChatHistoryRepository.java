package com.farmerassistant.repository;

import com.farmerassistant.entity.ChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ChatHistory entity operations.
 */
@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    /**
     * Fetch paginated chat history for a specific user.
     */
    Page<ChatHistory> findByUserIdOrderByAskedAtDesc(Long userId, Pageable pageable);

    /**
     * Count total chats by a specific user.
     */
    long countByUserId(Long userId);
}
