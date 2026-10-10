package com.echarge.common.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link FirmwareFilenameUtil}.
 *
 * @author Edwin
 */
class FirmwareFilenameUtilTest {

    @ParameterizedTest
    @CsvSource({
            "2.2.3, N3Lite-2.2.3.bin",
            "v2.2.3, N3Lite-2.2.3.bin",
            "V2.2.3, N3Lite-2.2.3.bin"
    })
    void shouldBuildStrictN3LiteFilename(String version, String expectedFilename) {
        assertEquals(expectedFilename, FirmwareFilenameUtil.buildN3LiteFilename(version));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2.2", "2.2.3_20260921", "release-2.2.3"})
    void shouldRejectInvalidVersion(String version) {
        assertThrows(IllegalArgumentException.class,
                () -> FirmwareFilenameUtil.buildN3LiteFilename(version));
    }
}
