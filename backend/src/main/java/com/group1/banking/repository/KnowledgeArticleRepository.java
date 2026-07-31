package com.group1.banking.repository;

import com.group1.banking.entity.KnowledgeArticle;
import com.group1.banking.enums.ChatTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    List<KnowledgeArticle> findByActiveTrueAndTopic(ChatTopic topic);

    List<KnowledgeArticle> findByActiveTrue();
}
