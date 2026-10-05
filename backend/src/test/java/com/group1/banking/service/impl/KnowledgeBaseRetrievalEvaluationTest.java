package com.group1.banking.service.impl;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Opt-in retrieval benchmark against the configured chatbot vector store.
 * Run with {@code mvnw.cmd -Dtest=KnowledgeBaseRetrievalEvaluationTest
 * -Drag.eval.enabled=true test} after the knowledge base has been ingested.
 */
@SpringBootTest
@EnabledIfSystemProperty(named = "rag.eval.enabled", matches = "true")
class KnowledgeBaseRetrievalEvaluationTest {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseRetrievalEvaluationTest.class);

    private static final List<EvaluationCase> CASES = List.of(
            new EvaluationCase("How much should I save for an emergency fund?",
                    List.of("01-emergency-fund-basics.md")),
            new EvaluationCase("How does the 50/30/20 budgeting rule work?",
                    List.of("02-budgeting-503020-rule.md")),
            new EvaluationCase("How can I automate my savings?",
                    List.of("03-automating-savings.md")),
            new EvaluationCase("How can I reduce discretionary spending?",
                    List.of("04-reducing-discretionary-spend.md")),
            new EvaluationCase("How do savings goals work?",
                    List.of("05-savings-goals-faq.md")),
            new EvaluationCase("What is the difference between savings and chequing accounts?",
                    List.of("06-savings-vs-chequing-education.md")),
            new EvaluationCase("How does a GIC work, and can I access the money during its term?",
                    List.of("07-gics-explained.md")));

    private final VectorStore vectorStore;
    private final int topK;
    private final double similarityThreshold;

        @Autowired
        KnowledgeBaseRetrievalEvaluationTest(
            VectorStore vectorStore,
            @Value("${app.chatbot.knowledge-base.top-k:4}") int topK,
            @Value("${app.chatbot.knowledge-base.similarity-threshold:0.5}") double similarityThreshold) {
        this.vectorStore = vectorStore;
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
    }

    @Test
    void reportPrecisionRecallAndF1ForLabeledQueries() {
        double precisionTotal = 0.0;
        double recallTotal = 0.0;
        double f1Total = 0.0;

        for (EvaluationCase evaluationCase : CASES) {
            List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(evaluationCase.query())
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .build());
            KnowledgeBaseRetrievalMetrics.Result result = KnowledgeBaseRetrievalMetrics.evaluateDocuments(
                    documents, evaluationCase.relevantSources());

            precisionTotal += result.precisionAtK();
            recallTotal += result.recallAtK();
            f1Total += result.f1AtK();
            log.info("RAG eval query='{}': relevant sources {}/{}, precision@k={}, recall@k={}, f1@k={}",
                    evaluationCase.query(), result.relevantRetrievedCount(), result.relevantSourceCount(),
                    result.precisionAtK(), result.recallAtK(), result.f1AtK());
        }

        int caseCount = CASES.size();
        log.info("RAG eval macro average ({} queries): precision@k={}, recall@k={}, f1@k={}",
                caseCount, precisionTotal / caseCount, recallTotal / caseCount, f1Total / caseCount);
    }

    private record EvaluationCase(String query, List<String> relevantSources) {
    }
}