package me.etan.critfix;

import org.bukkit.Bukkit;

import java.util.Map;

public class VersionHelper {
    private static final Map<String, String> onGroundFieldNames = Map.of(
            "1.21.0", "aG",
            "1.20.6", "aI",
            "1.20.0", "aJ",
            "1.19.4", "N",
            "1.18.2", "y"
    );

    private static final String MAX_VERSION = "1.21.1";

    private static class Version implements Comparable<Version> {
        final int major;
        final int minor;
        final String release;

        Version(String versionString) {
            String[] parts = versionString.split("\\.");
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
        Version maxSupportedVersion = new Version(MAX_VERSION);
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
