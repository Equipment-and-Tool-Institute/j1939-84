/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import org.etools.j1939_84.ui.UserInterfaceView;

/**
 * Main class for the J1939_84 Application
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class J1939_84 {

    private static final Logger logger = Logger.getGlobal();

    /**
     * System independent New Line
     */
    public static final String NL = System.lineSeparator();

    /** Page break including text for editors that do not support page break. */
    public static final String PAGE_BREAK = " <<PAGE BREAK>> " + NL + "\f";

    /**
     * The name of the property that is set when the application is being used
     * in a testing mode
     */
    public static final String TESTING_PROPERTY_NAME = "TESTING";

    /**
     * The name of the property that is set when the application is being used
     * in a testing mode
     */
    public static final String ENV_PROPERTY_NAME = "ENV";

    static {
        try {
            System.setProperty("java.util.logging.SimpleFormatter.format",
                               "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-6s %2$s %5$s%6$s%n");
            logger.setUseParentHandlers(false);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            logger.addHandler(consoleHandler);

            FileHandler fileHandler = new FileHandler("%t/j1939_84%g.log", 10 * 1024 * 1024, 100, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(consoleHandler.getFormatter());
            logger.addHandler(fileHandler);

            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error setting up logging", e);
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    /**
     * Returns true if the application is under test
     *
     * @return true if it's being tested
     */
    public static final boolean isTesting() {
        return Boolean.getBoolean(TESTING_PROPERTY_NAME);
    }

    /**
     * Returns true if the application is under test
     *
     * @return true if it's being tested
     */
    public static final boolean isDevEnv() {
        return Boolean.getBoolean(ENV_PROPERTY_NAME);
    }

    /**
     * Launch the application.
     *
     * @param args
     *         The arguments used to start the application
     */
    public static void main(String[] args) {
        getLogger().info("J1939_84 starting");
        setTesting(argAsBoolean(args, TESTING_PROPERTY_NAME));
        setEnv(argAsBoolean(args, ENV_PROPERTY_NAME));

        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            getLogger().log(Level.INFO, "Unable to set Look and Feel");
        }

        EventQueue.invokeLater(() -> {
            try {
                new UserInterfaceView().getFrame().setVisible(true);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Error showing frame", e);
            }
        });
    }

    private static Boolean argAsBoolean(String[] args, String argName) {
        return Arrays.stream(args)
                .filter(arg -> arg.contains(argName))
                .map(s -> s.substring(s.indexOf('=') + 1))
                .map(Boolean::parseBoolean)
                .findFirst()
                .orElse(false);
    }

    /**
     * Sets the System Property to indicate the system is under test
     *
     * @param env
     *         - true to indicate the environment is under development
     */
    public static final void setEnv(boolean env) {
        System.setProperty(ENV_PROPERTY_NAME, Boolean.toString(env));
    }

    /**
     * Sets the System Property to indicate the system is under test
     *
     * @param testing
     *         - true to indicate the system is under test
     */
    public static final void setTesting(boolean testing) {
        System.setProperty(TESTING_PROPERTY_NAME, Boolean.toString(testing));
    }
}
