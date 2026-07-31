package com.group1.banking.repository;

import com.group1.banking.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop20ByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
