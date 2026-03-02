package com.hisaab_khata.hisaab_khata.normalizer;

/**
 * Normalizes raw product/item names for matching: lowercase, trim, collapse spaces, unit token cleanup.
 */
public interface Normalizer {
    String normalize(String raw);
}
