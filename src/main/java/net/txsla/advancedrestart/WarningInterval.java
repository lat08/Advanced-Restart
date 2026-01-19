package net.txsla.advancedrestart;

/**
 * Represents a single warning interval (time + message)
 * Parses format: "30m:Message here" or "15s:Message here"
 */
public class WarningInterval implements Comparable<WarningInterval> {
    private final int timeInSeconds;
    private final String message;
    private final String originalTimeString;

    public WarningInterval(String intervalString) throws IllegalArgumentException {
        if (intervalString == null || !intervalString.contains(":")) {
            throw new IllegalArgumentException("Invalid interval format. Expected 'time:message' but got: " + intervalString);
        }

        String[] parts = intervalString.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid interval format. Expected 'time:message' but got: " + intervalString);
        }

        this.originalTimeString = parts[0].trim();
        this.timeInSeconds = parseTimeToSeconds(this.originalTimeString);
        this.message = parts[1]; // Keep the full message with MiniMessage formatting
    }

    /**
     * Parse time string to seconds
     * Supports: 30m, 15m, 10m, 5m, 1m, 30s, 15s, 10s, 5s, 3s, 2s, 1s
     */
    private int parseTimeToSeconds(String timeStr) throws IllegalArgumentException {
        timeStr = timeStr.trim().toLowerCase();

        // Check if it ends with 'm' (minutes) or 's' (seconds)
        if (timeStr.endsWith("m")) {
            try {
                int minutes = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
                if (minutes <= 0) {
                    throw new IllegalArgumentException("Time must be positive: " + timeStr);
                }
                return minutes * 60;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid minute format: " + timeStr);
            }
        } else if (timeStr.endsWith("s")) {
            try {
                int seconds = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
                if (seconds <= 0) {
                    throw new IllegalArgumentException("Time must be positive: " + timeStr);
                }
                return seconds;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid second format: " + timeStr);
            }
        } else {
            throw new IllegalArgumentException("Time must end with 'm' (minutes) or 's' (seconds): " + timeStr);
        }
    }

    public int getTimeInSeconds() {
        return timeInSeconds;
    }

    public String getMessage() {
        return message;
    }

    public String getOriginalTimeString() {
        return originalTimeString;
    }

    @Override
    public int compareTo(WarningInterval other) {
        // Sort in descending order (longest time first)
        return Integer.compare(other.timeInSeconds, this.timeInSeconds);
    }

    @Override
    public String toString() {
        return "WarningInterval{" +
                "time=" + originalTimeString +
                " (" + timeInSeconds + "s)" +
                ", message='" + message.substring(0, Math.min(30, message.length())) + "...'" +
                '}';
    }
}
