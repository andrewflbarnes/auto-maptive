package net.aflb.maptive.auto.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.status.StatusLogger;

public class CoreInit {
    public static void init() {
        StatusLogger.getLogger().setLevel(Level.OFF);
    }
}
