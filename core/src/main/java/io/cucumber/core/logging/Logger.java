package io.cucumber.core.logging;

/**
 * Logs messages to {@link java.util.logging.Logger}.
 * <p>
 * The methods correspond to {@link java.util.logging.Level} in JUL:
 * <ul>
 * <li>{@code error} maps to {@link java.util.logging.Level#SEVERE}</li>
 * <li>{@code warn} maps to {@link java.util.logging.Level#WARNING}</li>
 * <li>{@code info} maps to {@link java.util.logging.Level#INFO}</li>
 * <li>{@code config} maps to {@link java.util.logging.Level#CONFIG}</li>
 * <li>{@code debug} maps to {@link java.util.logging.Level#FINE}</li>
 * <li>{@code trace} maps to {@link java.util.logging.Level#FINER}</li>
 * </ul>
 */
public interface Logger {

    /**
     * Log the {@code message} at error level.
     * 
     * @param message The message to log.
     */
    void error(String message);

    /**
     * Log the {@code message} and {@code throwable} at error level.
     * 
     * @param message The message to log.
     * @param throwable The throwable to log.
     */
    void error(String message, Throwable throwable);

    /**
     * Log the {@code message} at warning level.
     * 
     * @param message The message to log.
     */
    void warn(String message);

    /**
     * Log the {@code message} and {@code throwable} at warning level.
     * 
     * @param message The message to log.
     * @param throwable The throwable to log.
     */
    void warn(String message, Throwable throwable);

    /**
     * Log the {@code message} at info level.
     * 
     * @param message The message to log.
     */
    void info(String message);

    /**
     * Log the {@code message} and {@code throwable} at info level.
     * 
     * @param message The message to log.
     * @param throwable The throwable to log.
     */
    void info(String message, Throwable throwable);

    /**
     * Log the {@code message} at config level.
     * 
     * @param message The message to log.
     */
    void config(String message);

    /**
     * Log the {@code message} and {@code throwable} at config level.
     * 
     * @param message The message to log.
     * @param throwable The throwable to log.
     */
    void config(String message, Throwable throwable);

    /**
     * Log the {@code message} at debug level.
     * 
     * @param message The message to log.
     */
    void debug(String message);

    /**
     * Log {@code message} and {@code throwable} at debug level.
     * 
     * @param message The message to log.
     * @param throwable The throwable to log.
     */
    void debug(String message, Throwable throwable);

    /**
     * Log the {@code message} at trace level.
     * 
     * @param message The message to log.
     */
    void trace(String message);

    /**
     * Log the {@code message} and {@code throwable} at trace level.
     * 
     * @param message The message to log.
     * @param throwable The throwable to log.
     */
    void trace(String message, Throwable throwable);
}
