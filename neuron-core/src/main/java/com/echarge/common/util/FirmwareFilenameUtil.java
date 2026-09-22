package com.echarge.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Firmware filename utilities.
 *
 * @author Edwin
 */
public final class FirmwareFilenameUtil {

    private static final String N3_LITE_PREFIX = "N3Lite-";
    private static final String BINARY_SUFFIX = ".bin";
    private static final Pattern VERSION_PATTERN = Pattern.compile("^[vV]?(\\d+\\.\\d+\\.\\d+)$");

    private FirmwareFilenameUtil() {
    }

    /**
     * Builds the strict N3 Lite OTA filename expected by device firmware.
     *
     * @param version semantic version, with an optional v/V prefix
     * @return filename in N3Lite-x.y.z.bin format
     */
    public static String buildN3LiteFilename(String version) {
        if (version == null) {
            throw new IllegalArgumentException("Firmware version must not be null");
        }
        Matcher matcher = VERSION_PATTERN.matcher(version.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Firmware version must use x.y.z format");
        }
        return N3_LITE_PREFIX + matcher.group(1) + BINARY_SUFFIX;
    }
}
