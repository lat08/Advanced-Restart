package net.txsla.advancedrestart;

import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class config {
    public static Plugin plugin;
    public static boolean allow_restart;
    public static double disable_timer;
    public static boolean reload() { return load(); } // :)

    public static boolean load() {
        // load all configs (also make work with /config reload later on)
        try {
            if (debug) System.out.println("[Advanced Restart] Loading configs...");
            allow_restart = false;
            // redundant the first time, but allows this class to be reused
            plugin.reloadConfig();

            //set configs to mem
            scheduledRestart_enabled = plugin.getConfig().getBoolean("scheduledRestart.enabled");
            scheduledRestart_schedule = plugin.getConfig().getStringList("scheduledRestart.schedule");
            scheduledRestart_message = plugin.getConfig().getString("scheduledRestart.message");

            periodicRestart_enabled = plugin.getConfig().getBoolean("periodicRestart.enabled");
            periodicRestart_duration = plugin.getConfig().getDouble("periodicRestart.duration");
            periodicRestart_message = plugin.getConfig().getString("periodicRestart.message");

            restartWarning_enabled = plugin.getConfig().getBoolean("restartWarning.enabled");
            restartWarning_totalWarningTime = plugin.getConfig().getInt("restartWarning.totalWarningTime");
            
            // Parse warning intervals
            restartWarning_intervals = new ArrayList<>();
            List<String> intervalStrings = plugin.getConfig().getStringList("restartWarning.intervals");
            for (String intervalStr : intervalStrings) {
                try {
                    WarningInterval interval = new WarningInterval(intervalStr);
                    restartWarning_intervals.add(interval);
                    if (debug) {
                        System.out.println("[Config] Loaded warning interval: " + interval.getOriginalTimeString() + " (" + interval.getTimeInSeconds() + "s)");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("[Config] ERROR parsing interval '" + intervalStr + "': " + e.getMessage());
                }
            }
            if (debug) {
                System.out.println("[Config] Loaded " + restartWarning_intervals.size() + " warning intervals");
            }

            inactiveRestart_enabled = plugin.getConfig().getBoolean("inactiveRestart.enabled");
            inactiveRestart_timer = plugin.getConfig().getInt("inactiveRestart.timer");
            inactiveRestart_message = plugin.getConfig().getString("inactiveRestart.message");

            lagRestart_lowTPS_enabled = plugin.getConfig().getBoolean("lagRestart.lowTPS.enabled");
            lagRestart_lowTPS_minTPS = plugin.getConfig().getDouble("lagRestart.lowTPS.minTPS");
            lagRestart_lowTPS_checks = plugin.getConfig().getInt("lagRestart.lowTPS.checks");
            lagRestart_lowTPS_message = plugin.getConfig().getString("lagRestart.lowTPS.message");

            lagRestart_lowMemory_enabled = plugin.getConfig().getBoolean("lagRestart.lowMemory.enabled");
            lagRestart_lowMemory_maxMemUsage = plugin.getConfig().getInt("lagRestart.lowMemory.maxMemUsage");
            lagRestart_lowMemory_checks = plugin.getConfig().getInt("lagRestart.lowMemory.checks");
            lagRestart_lowMemory_message = plugin.getConfig().getString("lagRestart.lowMemory.message");

            shutdownMethod = plugin.getConfig().getInt("shutdownMethod");
            shutdownMessage = plugin.getConfig().getString("shutdownMessage");

            shutdownCommands = plugin.getConfig().getStringList("shutdownCommands");


            debug = plugin.getConfig().getBoolean("debug");

            allow_restart = true;
            if (debug) System.out.println("[Advanced Restart] Config loading complete!");
        }
        // throw error if you cannot load the configs
        catch (Exception e) {
            System.out.println("\n\n[Advanced Restart] Failed to load configs\n" + e + "\n\n");
            return false;
        }
        return true;
    }

    public static boolean scheduledRestart_enabled;
    public static List<String> scheduledRestart_schedule;
    public static String scheduledRestart_message;


    public static boolean periodicRestart_enabled;
    public static double periodicRestart_duration;
    public static String periodicRestart_message;

    public static boolean restartWarning_enabled;
    public static int restartWarning_totalWarningTime;
    public static List<WarningInterval> restartWarning_intervals;

    public static boolean inactiveRestart_enabled;
    public static double inactiveRestart_timer;
    public static String inactiveRestart_message;

    public static boolean lagRestart_lowTPS_enabled;
    public static double lagRestart_lowTPS_minTPS;
    public static int lagRestart_lowTPS_checks;
    public static String lagRestart_lowTPS_message;

    public static boolean lagRestart_lowMemory_enabled;
    public static double lagRestart_lowMemory_maxMemUsage;
    public static int lagRestart_lowMemory_checks;
    public static String lagRestart_lowMemory_message;

    public static int shutdownMethod;
    public static String shutdownMessage;

    public static List<String> shutdownCommands;


    public static boolean debug;

}
