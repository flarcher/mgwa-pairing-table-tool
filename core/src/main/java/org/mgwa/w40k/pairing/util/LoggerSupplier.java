package org.mgwa.w40k.pairing.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a {@link Logger} using a temporary file.
 *
 * Singleton pattern based on an enum.
 */
public enum LoggerSupplier implements Supplier<Logger> {

	INSTANCE("MGWA_", "org.mgwa.w40k.pairing")

	;

	private static <T> T exitOnError(Exception exception) {
		exception.printStackTrace(System.err);
		System.exit(1);
		return null;
	}

	private static Path getLogFilePath(String filePrefix) {
		Objects.requireNonNull(filePrefix);
		try {
			Path path = Files.createTempFile(filePrefix, ".log");
			path.toFile().deleteOnExit();
			return path;
		}
		catch (IOException e) {
			return exitOnError(e);
		}
	}

	private static Logger createFileLogger(Path filePath, String name) {
		Logger logger = Logger.getLogger(name);
		logger.setLevel(Level.FINEST);
		try {
			logger.addHandler(new FileHandler(filePath.toString(), 2000, 1));
			return logger;
		}
		catch (IOException e) {
			return exitOnError(e);
		}
	}

	LoggerSupplier(String filePrefix, String name) {
		this.name = Objects.requireNonNull(name);
		this.logFilePath = getLogFilePath(filePrefix);
		this.logger = createFileLogger(logFilePath, name);
	}

	private final String name;
	private final Path logFilePath;
	private final Logger logger;

	public Logger getLogger() {
		return logger;
	}

	@Override
	public Logger get() {
		return logger;
	}

	public Path getLogFilePath() {
		return logFilePath;
	}

	public String getName() {
		return name;
	}
}
