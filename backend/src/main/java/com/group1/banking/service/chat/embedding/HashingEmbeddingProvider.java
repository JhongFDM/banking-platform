package com.group1.banking.service.chat.embedding;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The current (and only) implementation: a deterministic, local
 * "embedding" using the feature-hashing trick -- each word in the input
 * votes into one of 256 buckets, and the resulting vector is L2-normalized.
 * No network call, no API key, same output every time for the same input.
 *
 * This is an honest placeholder: it captures word overlap, not real
 * semantic meaning, which is a reasonable tradeoff for a small curated
 * knowledge base of 7 articles. Swapping in a real embedding model later
 * means writing one new class and re-running the indexer -- nothing
 * downstream changes.
 */
@Component
public class HashingEmbeddingProvider implements EmbeddingProvider {

    private static final int DIMENSIONS = 256;
    private static final Pattern WORD_PATTERN = Pattern.compile("[a-z0-9]+");

    @Override
    public float[] embed(String text) {
        float[] vector = new float[DIMENSIONS];
        if (text == null || text.isBlank()) {
            return vector;
        }

        Matcher matcher = WORD_PATTERN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            int bucket = Math.floorMod(matcher.group().hashCode(), DIMENSIONS);
            vector[bucket] += 1f;
        }

        normalize(vector);
        return vector;
    }

    @Override
    public int dimensions() {
        return DIMENSIONS;
    }

    private void normalize(float[] vector) {
        double sumSquares = 0;
        for (float v : vector) {
            sumSquares += (double) v * v;
        }
        if (sumSquares == 0) {
            return;
        }
        float norm = (float) Math.sqrt(sumSquares);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= norm;
        }
    }
}
