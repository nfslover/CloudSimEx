package org.cloudbus.cloudsim.ex.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.apache.commons.io.output.NullOutputStream;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Replaces the primitive functionality of the standard CloudSim Log. Allows
 * easily to redirect log to a file, and to customize the output.
 * 
 * <br/>
 * 
 * Before using this class remember to call the {@link CustomLog.configLogger}
 * with the desired {@link Properties}, or with an empty {@link Properties} if
 * you want to use the defaults. The documentation of the public String
 * constants of the class describes what keys and values can be specified.
 * 
 * 
 * @author Nikolay Grozev
 * 
 *         Adapted from versions of:
 *         <ol>
 *         <li>Anton Beloglazov</li>
 *         <li>William Voorsluys</li>
 *         <li>Adel Nadjaran Toosi</li>
 *         </ol>
 * 
 * @since CloudSim Toolkit 2.0
 */
public class CustomLog {

    // //// Configuration properties
    /**
     * A key for the config property specifying what is the minimal logged
     * level. Must be a string of a constant from {@link Level}
     */
    public static final String LOG_LEVEL_PROP_KEY = "LogLevel";

    /**
     * A key for a boolean property, specifying if every log entry should start
     * with the current CloudSim time.
     */
    public static final String LOG_CLOUD_SIM_CLOCK_PROP_KEY = "LogCloudSimClock";

    /**
     * Specifies which methods of the {@link LogRecord} class should be used to
     * create the log entries. Calls must be seprated with a semicolon ";". For
     * example "getLevel;getMessage".
     */
    public static final String LOG_FORMAT_PROP_KEY = "LogFormat";

    /**
     * A key for a file property where the log is to be written. If not
     * specified the standard output is used instead of a file.
     */
    public static final String FILE_PATH_PROP_KEY = "FilePath";

    /**
     * A key for a boolean property specifying whether the standard CloudSim
     * logger should be turned off. That will cause all the log generated by the
     * system classes of CloudSim not to be printed.
     */
    public static final String SHUT_STANDART_LOGGER_PROP_KEY = "ShutStandardLogger";

    /**
     * The default log level used by this log, if not specified.
     */
    public final static Level DEFAULT_LEVEL = Level.INFO;

    private static final Logger LOGGER = Logger.getLogger(CustomLog.class.getPackage().getName());
    private static Level granularityLevel;
    private static Formatter formatter;

    /**
     * Prints the message passed as an object. Simply uses toString
     * implementation.
     * 
     * @param level
     *            - the level to use. If null the default level is used.
     * @param message
     *            - the message.
     */
    public static void print(final Level level, final Object message) {
	LOGGER.log(
		level == null ? DEFAULT_LEVEL : level, String.valueOf(message));
    }

    /**
     * Prints the message passed as an object. Simply uses toString
     * implementation. Uses the default log level.
     * 
     * @param message
     *            - the message.
     */
    public static void print(final Object message) {
	print(DEFAULT_LEVEL, message);
    }

    /**
     * Prints a line with the message to the log.
     * 
     * @param level
     *            - the log level. If null, the default log level is used.
     * @param msg
     *            - the message. Must not be null.
     */
    public static void printLine(final Level level, final String msg) {
	LOGGER.log(level == null ? DEFAULT_LEVEL : level, msg);
    }

    /**
     * Prints a line with the message to the log. Uses the default log level.
     * 
     * @param msg
     *            - the message. Must not be null.
     */
    public static void printLine(final String msg) {
	printLine(DEFAULT_LEVEL, msg);
    }

    /**
     * Prints the formatted string, resulting from applying the format string to
     * the arguements.
     * 
     * @param format
     *            - the format (as in String.format).
     * @param level
     *            - the level. If null the default level is used
     * @param args
     */
    public static void printf(final Level level, final String format, final Object... args) {
	LOGGER.log(level == null ? DEFAULT_LEVEL : level, String.format(format, args));
    }

    /**
     * Prints the formatted string with the default level, resulting from
     * applying the format string to the arguments.
     * 
     * @param format
     *            - the format (as in String.format).
     * @param args
     */
    public static void printf(final String format, final Object... args) {
	LOGGER.log(DEFAULT_LEVEL, String.format(format, args));
    }

    /**
     * Prints a header for the specified class. The format is as per the
     * specification in {@link TextUtil}
     * 
     * @param klass
     *            - the class. Must not be null.
     * @param delim
     *            - the delimeter. Must not be null.
     */
    public static void printHeader(final Class<?> klass, final String delim) {
	CustomLog.printLine(TextUtil.getCaptionLine(klass, delim));
    }

    /**
     * Prints a header for the specified class. The format is as per the
     * specification in {@link TextUtil}
     * 
     * @param klass
     *            - the class. Must not be null.
     */
    public static void printHeader(final Class<?> klass) {
	CustomLog.printLine(TextUtil.getCaptionLine(klass));
    }

    /**
     * Prints a line for the object. The format is as per the specification in
     * {@link TextUtil}
     * 
     * @param o
     *            - the object. Must not be null.
     * @param delim
     *            - the delimeter. Must not be null.
     */
    public static void printLineForObject(final Object o, final String delim) {
	CustomLog.print(TextUtil.getTxtLine(o, delim, false));
    }

    /**
     * Prints a line for the object. The format is as per the specification in
     * {@link TextUtil}
     * 
     * @param o
     *            - the object. Must not be null.
     */
    public static void printLineForObject(final Object o) {
	CustomLog.print(TextUtil.getTxtLine(o));
    }

    /**
     * Prints the objects' details with a header in a CSV - like format.
     * 
     * @param klass
     *            - the class to be used for the header. If null no header is printed.
     * @param list
     *            - list of objects. All objects, must be of type klass.
     */
    public static void printResults(final Class<?> klass, final List<?>... lines) {
	if(klass != null) {
	    // Print header line
	    printHeader(klass);
	}
	
	// Print details for each cloudlet
	for (List<?> list : lines) {
	    for (Object o : list) {
		printLineForObject(o);
	    }
	}
    }

    /**
     * Prints the objects' details with a header in a CSV - like format.
     * 
     * @param klass
     *            - the class to be used for the header. If null no header is printed.
     * @param delim
     *            - the delimeter to use.
     * @param list
     *            - list of objects. All objects, must be of type klass.
     */
    public static void printResults(final Class<?> klass, final String delim, final List<?>... lines) {
	if (klass != null) {
	    // Print header line
	    printHeader(klass, delim);
	}
	
	// Print details for each cloudlet
	for (List<?> list : lines) {
	    for (Object o : list) {
		printLineForObject(o, delim);
	    }
	}
    }

    /**
     * Logs the stacktrace of the exception.
     * 
     * @param level
     *            - the level to use.
     * @param message
     *            - the messag to append before that.
     * @param exc
     *            - the exception to log.
     */
    public static void logError(final Level level, final String message, final Throwable exc) {
	LOGGER.log(level, "", exc);
    }

    /**
     * Logs the stacktrace of the exception.
     * 
     * @param message
     *            - the messag to append before that.
     * @param exc
     *            - the exception to log.
     */
    public static void logError(final String message, final Throwable exc) {
	logError(DEFAULT_LEVEL, message, exc);
    }

    /**
     * Returns if this logger is disabled.
     * 
     * @return - if this logger is disabled.
     */
    public static boolean isDisabled() {
	return LOGGER.getLevel().equals(Level.OFF);
    }

    /**
     * Sets the output of this logger. This method is to be used for redirecting
     * to "nonstandard" (e.g. database) output streams. If you simply want to
     * redirect the logger to a file, you'd better use the initialization
     * properties.
     * 
     * @param output
     *            - the new output. Must not be null.
     */
    public static void setOutput(final OutputStream output) {
	LOGGER.addHandler(new StreamHandler(output, formatter));
    }

    /**
     * Returns a nicely formatted representation of the current CloudSim time.
     * 
     * @return a nicely formatted representation of the current CloudSim time.
     */
    public static String formatClockTime() {
	return TextUtil.toString(CloudSim.clock());
    }

    /**
     * Configures the logger. Must be called before the logger is used.
     * 
     * @param props
     *            - the configuration properties. See the predefined keys in
     *            this class, to get an idea of what is required.
     * @throws SecurityException
     *             - if the specified log format contains invalid method calls.
     * @throws IOException
     *             - if something goes wrong with the I/O.
     */
    public static void configLogger(final Properties props)
	    throws SecurityException, IOException {
	final String fileName = props.containsKey(FILE_PATH_PROP_KEY) ? props.getProperty(
		FILE_PATH_PROP_KEY).toString() : null;
	final String format = props.getProperty(LOG_FORMAT_PROP_KEY,
		"getLevel;getMessage").toString();
	final boolean prefixCloudSimClock = Boolean.parseBoolean(props
		.getProperty(LOG_CLOUD_SIM_CLOCK_PROP_KEY, "false")
		.toString());
	final boolean shutStandardMessages = Boolean.parseBoolean(props
		.getProperty(SHUT_STANDART_LOGGER_PROP_KEY, "false")
		.toString());
	granularityLevel = Level.parse(props.getProperty(
		LOG_LEVEL_PROP_KEY, DEFAULT_LEVEL.getName()).toString());

	if (shutStandardMessages) {
	    Log.setOutput(new NullOutputStream());
	}

	LOGGER.setUseParentHandlers(false);
	formatter = new CustomFormatter(prefixCloudSimClock, format);

	redirectToFile(fileName);
    }

    /**
     * Redirects this logger to a file.
     * 
     * @param fileName
     *            - the name of the new log file. If null the log is redirected
     *            to the standard output.
     */
    public static void redirectToFile(final String fileName) {
	for (Handler h : LOGGER.getHandlers()) {
	    LOGGER.removeHandler(h);
	}

	if (fileName != null) {
	    System.err.println("Rediricting output to " + new File(fileName).getAbsolutePath());
	}

	StreamHandler handler;
	try {
	    handler = fileName != null ? new FileHandler(fileName, false)
		    : new ConsoleHandler();
	    handler.setLevel(granularityLevel);
	    handler.setFormatter(formatter);
	    LOGGER.addHandler(handler);
	    LOGGER.setLevel(granularityLevel);

	} catch (SecurityException | IOException e) {
	    e.printStackTrace();
	}
    }

    private static class CustomFormatter extends Formatter {

	private final boolean prefixCloudSimClock;
	private final String format;
	SimpleFormatter defaultFormatter = new SimpleFormatter();

	public CustomFormatter(final boolean prefixCloudSimClock, final String format) {
	    super();
	    this.prefixCloudSimClock = prefixCloudSimClock;
	    this.format = format;
	}

	@Override
	public String format(final LogRecord record) {
	    final String[] methodCalls = format.split(";");
	    final StringBuffer result = new StringBuffer();
	    if (prefixCloudSimClock) {
		result.append(formatClockTime() + "\t");
	    }

	    // If there is an exception - use the standard formatter
	    if (record.getThrown() != null) {
		result.append(defaultFormatter.format(record));
	    } else {
		int i = 0;
		for (String method : methodCalls) {
		    try {
			result.append(record.getClass().getMethod(method)
				.invoke(record));
		    } catch (Exception e) {
			System.err.println("Error in logging:");
			e.printStackTrace(System.err);
			System.exit(1);
		    }
		    if (i++ < methodCalls.length - 1) {
			result.append('\t');
		    }
		}
	    }

	    result.append(TextUtil.NEW_LINE);

	    return result.toString();
	}
    }
}