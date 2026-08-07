package org.alanjordan.drcdesigner;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class AppLogger {
    private static final Logger LOGGER = Logger.getLogger("org.alanjordan.drcdesigner");
    private static boolean initialized = false;

    private AppLogger() {
    }

    public static synchronized Logger getLogger() {
        if (!initialized) {
            initialize();
        }
        return LOGGER;
    }

    private static void initialize() {
        try {
            File logDir = new File(Options.getAppDataDirectory(), "logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            File logFile = new File(logDir, "DRCDesigner.log");
            FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath(), true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.INFO);
            initialized = true;
            LOGGER.info("Logging initialized: " + logFile.getAbsolutePath());
        }
        catch (IOException ioe) {
            initialized = true;
            System.err.println("Unable to initialize logging: " + ioe.getMessage());
        }
    }
}
