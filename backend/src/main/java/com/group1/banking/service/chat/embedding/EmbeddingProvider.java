package com.group1.banking.service.chat.embedding;

/**
 * Exists for the same reason ResponseGenerator does: Groq doesn't offer an
 * embeddings endpoint, and rather than quietly reaching for a third API and
 * a third key nobody asked for, embeddings are behind an interface with a
 * local default (HashingEmbeddingProvider) and an easy swap point later.
 */
public interface EmbeddingProvider {

    float[] embed(String text);

    int dimensions();
}
