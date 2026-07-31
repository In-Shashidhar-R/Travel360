package com.cts.util;

import com.cts.enumeration.IdProofType;
import com.cts.exception.InvalidTimelineException;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Indian formats:
 *   PAN              — 5 letters, 4 digits, 1 letter  (e.g. ABCDE1234F)
 *   AADHAAR          — exactly 12 digits, first digit 2-9 (e.g. 234567890123)
 *   DRIVING_LICENSE  — 2 letters + 2 digits + 11 digits, optionally with spaces/hyphen
 *   PASSPORT         — 1 letter followed by 7 digits (e.g. A1234567)
 */
public final class IdProofValidator {

    private IdProofValidator() {}

    private static final Map<IdProofType, Pattern> PATTERNS = Map.of(
            IdProofType.PAN,             Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$"),
            IdProofType.AADHAAR,         Pattern.compile("^[2-9][0-9]{11}$"),
            IdProofType.DRIVING_LICENSE, Pattern.compile("^[A-Z]{2}[0-9]{2}[\\s-]?[0-9]{11}$"),
            IdProofType.PASSPORT,        Pattern.compile("^[A-Z][0-9]{7}$")
    );

    private static final Map<IdProofType, String> EXAMPLES = Map.of(
            IdProofType.PAN,             "ABCDE1234F",
            IdProofType.AADHAAR,         "234567890123",
            IdProofType.DRIVING_LICENSE, "TN0120211234567",
            IdProofType.PASSPORT,        "A1234567"
    );


    public static void validate(IdProofType type, String rawNumber) {
        if (type == null) {
            throw new InvalidTimelineException("ID proof type is required.");
        }
        if (rawNumber == null || rawNumber.isBlank()) {
            throw new InvalidTimelineException("ID proof number is required for type " + type + ".");
        }
        String normalized = rawNumber.trim().toUpperCase();
        Pattern pattern = PATTERNS.get(type);
        if (pattern != null && !pattern.matcher(normalized).matches()) {
            throw new InvalidTimelineException(
                    "Invalid " + type + " number format. Expected pattern like '"
                    + EXAMPLES.get(type) + "'.");
        }
    }

    public static String normalize(String rawNumber) {
        return rawNumber == null ? null : rawNumber.trim().toUpperCase();
    }
}
