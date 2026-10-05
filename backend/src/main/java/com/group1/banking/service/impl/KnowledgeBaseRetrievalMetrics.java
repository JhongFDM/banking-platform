package com.group1.banking.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.ai.document.Document;

/**
 * Source-level retrieval metrics for evaluating labeled knowledge-base queries.
 * Pass the source filenames from the top-K retrieved chunks and the filenames
 * labeled relevant for the query.
 */
public final class KnowledgeBaseRetrievalMetrics {

    private KnowledgeBaseRetrievalMetrics() {
    }

    public static Result evaluate(Collection<String> retrievedSources, Collection<String> relevantSources) {
        if (retrievedSources == null || relevantSources == null) {
            throw new IllegalArgumentException("Retrieved and relevant sources must not be null");
        }

        Set<String> retrieved = normalizedSources(retrievedSources);
        Set<String> relevant = normalizedSources(relevantSources);
        if (relevant.isEmpty()) {
            throw new IllegalArgumentException("At least one relevant source is required to calculate recall");
        }

        Set<String> relevantRetrieved = new HashSet<>(retrieved);
        relevantRetrieved.retainAll(relevant);

        double precision = retrieved.isEmpty() ? 0.0 : (double) relevantRetrieved.size() / retrieved.size();
        double recall = (double) relevantRetrieved.size() / relevant.size();
        double f1 = precision + recall == 0.0 ? 0.0 : 2.0 * precision * recall / (precision + recall);

        return new Result(retrieved.size(), relevant.size(), relevantRetrieved.size(), precision, recall, f1);
    }

    public static Result evaluateDocuments(Collection<Document> retrievedDocuments,
            Collection<String> relevantSources) {
        if (retrievedDocuments == null) {
            throw new IllegalArgumentException("Retrieved documents must not be null");
        }

        List<String> retrievedSources = new ArrayList<>();
        for (Document document : retrievedDocuments) {
            Object source = document.getMetadata().get("source");
            if (!(source instanceof String sourceFilename) || sourceFilename.isBlank()) {
                throw new IllegalArgumentException("Every retrieved document must have a non-blank source filename");
            }
            retrievedSources.add(sourceFilename);
        }
        return evaluate(retrievedSources, relevantSources);
    }

    private static Set<String> normalizedSources(Collection<String> sources) {
        Set<String> normalized = new HashSet<>();
        for (String source : sources) {
            if (source == null || source.isBlank()) {
                throw new IllegalArgumentException("Source filenames must not be null or blank");
            }
            normalized.add(source.trim());
        }
        return normalized;
    }

    public record Result(
            int retrievedSourceCount,
            int relevantSourceCount,
            int relevantRetrievedCount,
            double precisionAtK,
            double recallAtK,
            double f1AtK) {
    }
}