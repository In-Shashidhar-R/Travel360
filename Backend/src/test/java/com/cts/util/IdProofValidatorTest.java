package com.cts.util;

import com.cts.enumeration.IdProofType;
import com.cts.exception.InvalidTimelineException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdProofValidatorTest {

    @Test
    void pan_validFormat_passes() {
        assertDoesNotThrow(() -> IdProofValidator.validate(IdProofType.PAN, "ABCDE1234F"));
    }

    @Test
    void pan_lowercase_isNormalisedAndPasses() {
        assertDoesNotThrow(() -> IdProofValidator.validate(IdProofType.PAN, "abcde1234f"));
    }

    @Test
    void pan_invalidFormat_throws() {
        assertThrows(InvalidTimelineException.class,
                () -> IdProofValidator.validate(IdProofType.PAN, "AB1234"));
    }

    @Test
    void aadhaar_twelveDigits_passes() {
        assertDoesNotThrow(() -> IdProofValidator.validate(IdProofType.AADHAAR, "234567890123"));
    }

    @Test
    void aadhaar_startingWithOne_throws() {
        assertThrows(InvalidTimelineException.class,
                () -> IdProofValidator.validate(IdProofType.AADHAAR, "134567890123"));
    }

    @Test
    void aadhaar_wrongLength_throws() {
        assertThrows(InvalidTimelineException.class,
                () -> IdProofValidator.validate(IdProofType.AADHAAR, "12345"));
    }

    @Test
    void passport_validFormat_passes() {
        assertDoesNotThrow(() -> IdProofValidator.validate(IdProofType.PASSPORT, "A1234567"));
    }

    @Test
    void passport_invalidFormat_throws() {
        assertThrows(InvalidTimelineException.class,
                () -> IdProofValidator.validate(IdProofType.PASSPORT, "AB12"));
    }

    @Test
    void drivingLicense_validFormat_passes() {
        assertDoesNotThrow(() -> IdProofValidator.validate(IdProofType.DRIVING_LICENSE, "TN0120211234567"));
    }

    @Test
    void nullType_throws() {
        assertThrows(InvalidTimelineException.class, () -> IdProofValidator.validate(null, "ABCDE1234F"));
    }

    @Test
    void blankNumber_throws() {
        assertThrows(InvalidTimelineException.class, () -> IdProofValidator.validate(IdProofType.PAN, "  "));
    }

    @Test
    void normalize_trimsAndUppercases() {
        assertEquals("ABCDE1234F", IdProofValidator.normalize("  abcde1234f "));
        assertNull(IdProofValidator.normalize(null));
    }
}
