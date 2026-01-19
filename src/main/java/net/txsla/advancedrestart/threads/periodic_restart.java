package net.txsla.advancedrestart.threads;

import org.bukkit.Bukkit;
import net.txsla.advancedrestart.RestartWarningManager;
import net.txsla.advancedrestart.config;
import net.txsla.advancedrestart.format;
public class periodic_restart {
    Thread periodicRestart;
    public static int remaining() {
        // returns minutes remaining
        return (int) ( config.periodicRestart_duration - ((System.currentTimeMillis() - startTime) / 60000 ) );
    }
    public static double startTime;
    public periodic_restart() {
        setTimer();
    }
    public void setTimer()
    {
        startTime = System.currentTimeMillis();
        if (config.debug) Bukkit.getServer().getConsoleSender().sendMessage("[periodicRestart.setTimer] startTime = " + startTime);

        periodicRestart = new Thread(()->
        {
            // sleep until the current time is greater than the startTime - ( current_time + duration )
            while ((System.currentTimeMillis() - startTime) < (config.periodicRestart_duration * 60000)) {
                try {Thread.sleep(1000);} catch(InterruptedException e) {Thread.currentThread().interrupt();}
                if (config.debug) System.out.println("[PeriodicRestart] countdown " + (System.currentTimeMillis() - startTime) + " < " + (config.periodicRestart_duration * 60000));
            }

            // is this if statement unnecessarily redundant?
            if (System.currentTimeMillis() - startTime >= (config.periodicRestart_duration * 60000))
            {
                if (config.debug) Bukkit.getServer().getConsoleSender().sendMessage("[periodicRestart.setTimer] starting shutdown sequence");

                // send restart message and replace placeholder with runtime
                try {
                    if (config.periodicRestart_message != null)
                        format.sendMessage(config.periodicRestart_message.replaceAll("%RUNTIME", "" + ((int) (System.currentTimeMillis() - startTime) / 60000)));
                } catch (Exception e) {
                    if (config.debug) System.out.println(e);
                }
                
                // Use new warning system
                RestartWarningManager.executeWarningSequence();

                stop_server.shutdown();
            }
        });
        periodicRestart.start();
    }
}