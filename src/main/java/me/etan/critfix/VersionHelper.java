package me.etan.critfix;

import java.util.Map;

public class VersionHelper {
    private static final Map<String, String> onGroundFieldNames = Map.of(
            "1_20_R1", "aJ",
            "1_19_R3", "N",
            "1_18_R2", "y",
            "1_17_R1", "z",
            "1_15_R1", "t",
            "1_14_R1", "w"
    );

    private static final String MAX_NMS_VERSION = "1_20_R3";

    private static class Version implements Comparable<Version> {
        final int major;
        final int minor;
        final String release;

        Version(String versionString) {
            // Remove any non-numeric prefixes like 'v'
            versionString = versionString.replaceFirst("[^0-9]*", "");

            String[] parts = versionString.split("_");
            this.major = Integer.parseInt(parts[0]);
            this.minor = Integer.parseInt(parts[1]);
            this.release = parts[2];
        }

        @Override
        public int compareTo(Version other) {
            if (this.major != other.major) {
                return Integer.compare(this.major, other.major);
            }
            if (this.minor != other.minor) {
                return Integer.compare(this.minor, other.minor);
            }
            return this.release.compareTo(other.release);
        }

        public boolean isLessThanOrEqual(Version other) {
            return this.compareTo(other) <= 0;
        }
    }

    public static boolean isSupportedVersion(String version) {
        Version queryVersion = new Version(version);
        Version maxSupportedVersion = new Version(MAX_NMS_VERSION);
        return queryVersion.isLessThanOrEqual(maxSupportedVersion);
    }

    public static String getOnGroundFieldName(String queryVersionString) {
        Version queryVersion = new Version(queryVersionString);
        Map.Entry<String, String> closestEntry = null;

        for (Map.Entry<String, String> entry : onGroundFieldNames.entrySet()) {
            Version keyVersion = new Version(entry.getKey());
            if (keyVersion.isLessThanOrEqual(queryVersion)) {
                if (closestEntry == null || new Version(closestEntry.getKey()).compareTo(keyVersion) < 0) {
                    closestEntry = entry;
                }
            }
        }

        return (closestEntry != null) ? closestEntry.getValue() : null;
    }
}
