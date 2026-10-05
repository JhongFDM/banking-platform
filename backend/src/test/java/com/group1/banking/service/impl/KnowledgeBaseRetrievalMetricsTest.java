package com.group1.banking.service.impl;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

class KnowledgeBaseRetrievalMetricsTest {

    @Test
    void evaluate_countsRelevantSourcesAndIgnoresDuplicateChunks() {
        KnowledgeBaseRetrievalMetrics.Result result = KnowledgeBaseRetrievalMetrics.evaluate(
                List.of("01-emergency-fund-basics.md", "01-emergency-fund-basics.md", "02-budgeting-503020-rule.md"),
                List.of("01-emergency-fund-basics.md", "03-automating-savings.md"));

        assertThat(result.retrievedSourceCount()).isEqualTo(2);
        assertThat(result.relevantSourceCount()).isEqualTo(2);
        assertThat(result.relevantRetrievedCount()).isEqualTo(1);
        assertThat(result.precisionAtK()).isEqualTo(0.5);
        assertThat(result.recallAtK()).isEqualTo(0.5);
        assertThat(result.f1AtK()).isEqualTo(0.5);
    }

    @Test
    void evaluate_returnsZeroMetricsWhenNothingIsRetrieved() {
        KnowledgeBaseRetrievalMetrics.Result result = KnowledgeBaseRetrievalMetrics.evaluate(
                List.of(), List.of("01-emergency-fund-basics.md"));

        assertThat(result.precisionAtK()).isZero();
        assertThat(result.recallAtK()).isZero();
        assertThat(result.f1AtK()).isZero();
    }

    @Test
    void evaluateDocuments_readsSourceMetadataFromRetrievedChunks() {
        KnowledgeBaseRetrievalMetrics.Result result = KnowledgeBaseRetrievalMetrics.evaluateDocuments(
                List.of(
                        new Document("First chunk", Map.of("source", "01-emergency-fund-basics.md")),
                        new Document("Second chunk", Map.of("source", "01-emergency-fund-basics.md"))),
                List.of("01-emergency-fund-basics.md"));

        assertThat(result.retrievedSourceCount()).isEqualTo(1);
        assertThat(result.precisionAtK()).isEqualTo(1.0);
        assertThat(result.recallAtK()).isEqualTo(1.0);
    }

    @Test
    void evaluate_requiresRelevantSourcesForRecall() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KnowledgeBaseRetrievalMetrics.evaluate(List.of("01-article.md"), List.of()));
    }
}