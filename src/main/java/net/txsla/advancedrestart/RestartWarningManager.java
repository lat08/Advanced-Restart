package net.txsla.advancedrestart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages restart warnings with flexible interval-based system
 * Handles the logic for sending warnings at specified intervals before restart
 */
public class RestartWarningManager {

    /**
     * Execute the warning sequence before shutting down the server
     * This method will sleep for the total warning time and send messages at specified intervals
     */
    public static void executeWarningSequence() {
        if (!config.restartWarning_enabled) {
            if (config.debug) {
                System.out.println("[RestartWarningManager] Warning system is disabled");
            }
            return;
        }

        if (config.restartWarning_intervals == null || config.restartWarning_intervals.isEmpty()) {
            if (config.debug) {
                System.out.println("[RestartWarningManager] No warning intervals configured");
            }
            return;
        }

        // Sort intervals in descending order (longest time first)
        List<WarningInterval> intervals = new ArrayList<>(config.restartWarning_intervals);
        Collections.sort(intervals);

        if (config.debug) {
            System.out.println("[RestartWarningManager] Starting warning sequence with " + intervals.size() + " intervals");
            System.out.println("[RestartWarningManager] Total warning time: " + config.restartWarning_totalWarningTime + " seconds");
            for (WarningInterval interval : intervals) {
                System.out.println("[RestartWarningManager]   - " + interval.getOriginalTimeString() + " (" + interval.getTimeInSeconds() + "s)");
            }
        }

        // Validate that totalWarningTime is at least as long as the longest interval
        if (!intervals.isEmpty()) {
            int longestInterval = intervals.get(0).getTimeInSeconds();
            if (config.restartWarning_totalWarningTime < longestInterval) {
                System.out.println("[RestartWarningManager] WARNING: totalWarningTime (" + config.restartWarning_totalWarningTime + 
                                 "s) is less than longest interval (" + longestInterval + "s). Some warnings may not be sent!");
            }
        }

        // Execute the warning sequence
        executeIntervals(intervals, config.restartWarning_totalWarningTime);
    }

    /**
     * Execute warning intervals with precise timing
     * Algorithm:
     * 1. Start with totalWarningTime as countdown
     * 2. For each interval (sorted descending), check if it's <= current countdown
     * 3. If yes, send message and continue
     * 4. Sleep until next interval or end
     */
    private static void executeIntervals(List<WarningInterval> intervals, int totalWarningTime) {
        long startTime = System.currentTimeMillis();
        int currentIndex = 0;

        // Convert total warning time to milliseconds
        long totalWarningMs = totalWarningTime * 1000L;
        long endTime = startTime + totalWarningMs;

        if (config.debug) {
            System.out.println("[RestartWarningManager] Start time: " + startTime);
            System.out.println("[RestartWarningManager] End time: " + endTime);
        }

        // Find first interval that fits within total warning time
        while (currentIndex < intervals.size() && 
               intervals.get(currentIndex).getTimeInSeconds() > totalWarningTime) {
            if (config.debug) {
                System.out.println("[RestartWarningManager] Skipping interval " + 
                                 intervals.get(currentIndex).getOriginalTimeString() + 
                                 " as it exceeds total warning time");
            }
            currentIndex++;
        }

        // Process all intervals
        while (currentIndex < intervals.size()) {
            long currentTime = System.currentTimeMillis();
            long timeRemaining = endTime - currentTime;
            int secondsRemaining = (int) (timeRemaining / 1000);

            WarningInterval interval = intervals.get(currentIndex);
            int intervalTime = interval.getTimeInSeconds();

            if (config.debug) {
                System.out.println("[RestartWarningManager] Time remaining: " + secondsRemaining + "s, checking interval: " + interval.getOriginalTimeString() + " (" + intervalTime + "s)");
            }

            // If we've reached or passed this interval's time
            if (secondsRemaining <= intervalTime) {
                // Send the warning message
                try {
                    format.sendMessage(interval.getMessage());
                    if (config.debug) {
                        System.out.println("[RestartWarningManager] Sent warning for interval: " + interval.getOriginalTimeString());
                    }
                } catch (Exception e) {
                    System.out.println("[RestartWarningManager] Error sending message for interval " + interval.getOriginalTimeString() + ": " + e.getMessage());
                    if (config.debug) {
                        e.printStackTrace();
                    }
                }

                // Move to next interval
                currentIndex++;

                // If there's another interval, sleep until we reach it
                if (currentIndex < intervals.size()) {
                    WarningInterval nextInterval = intervals.get(currentIndex);
                    int nextIntervalTime = nextInterval.getTimeInSeconds();

                    // Calculate how long to sleep until next interval
                    // We want to wake up when timeRemaining equals nextIntervalTime
                    int sleepSeconds = intervalTime - nextIntervalTime;
                    
                    if (sleepSeconds > 0) {
                        if (config.debug) {
                            System.out.println("[RestartWarningManager] Sleeping for " + sleepSeconds + "s until next interval at " + nextInterval.getOriginalTimeString());
                        }
                        sleepSafely(sleepSeconds * 1000L);
                    }
                } else {
                    // No more intervals, sleep until shutdown
                    if (timeRemaining > 0) {
                        if (config.debug) {
                            System.out.println("[RestartWarningManager] Final sleep for " + (timeRemaining / 1000) + "s until shutdown");
                        }
                        sleepSafely(timeRemaining);
                    }
                }
            } else {
                // We haven't reached this interval yet, sleep until we do
                long sleepTime = timeRemaining - (intervalTime * 1000L);
                if (sleepTime > 0) {
                    if (config.debug) {
                        System.out.println("[RestartWarningManager] Waiting " + (sleepTime / 1000) + "s to reach interval " + interval.getOriginalTimeString());
                    }
                    sleepSafely(sleepTime);
                }
            }
        }

        if (config.debug) {
            System.out.println("[RestartWarningManager] Warning sequence complete");
        }
    }

    /**
     * Sleep safely with exception handling
     */
    private static void sleepSafely(long milliseconds) {
        if (milliseconds <= 0) return;
        
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[RestartWarningManager] Sleep interrupted: " + e.getMessage());
        }
    }
}
