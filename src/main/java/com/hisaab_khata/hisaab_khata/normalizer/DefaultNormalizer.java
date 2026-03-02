package com.hisaab_khata.hisaab_khata.normalizer;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Default normalizer: lowercase, trim, collapse spaces, unit token cleanup.
 */
@Component
public class DefaultNormalizer implements Normalizer {

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");

    @Override
    public String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().toLowerCase();
        s = MULTIPLE_SPACES.matcher(s).replaceAll(" ");
        s = normalizeUnitTokens(s);
        return s.trim();
    }

    /** Clean common unit tokens that may appear in product names (e.g. "1 kg" -> keep for matching). */
    private String normalizeUnitTokens(String s) {
        // Normalize unit abbreviations: "kg" "kgs" "gm" "gms" "ml" "l" "ltr" "pcs" "pc" etc. to canonical form
        // For now we only collapse spaces; optional: replace "kgs" -> "kg", "gms" -> "gm"
        return s.replaceAll("\\s+kgs?\\b", " kg")
                .replaceAll("\\s+gms?\\b", " gm")
                .replaceAll("\\s+ltrs?\\b", " l")
                .replaceAll("\\s+ml\\b", " ml")
                .replaceAll("\\s+pcs?\\b", " pcs");
    }
}
