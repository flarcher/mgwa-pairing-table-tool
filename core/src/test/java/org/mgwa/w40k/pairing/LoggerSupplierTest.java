package org.mgwa.w40k.pairing;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class LoggerSupplierTest {

	@Test
	public void testLogWrite() throws IOException {
		LoggerSupplier loggerSupplier = LoggerSupplier.INSTANCE;
		Logger logger = loggerSupplier.getLogger();
		String value = "Blabla";
		logger.info(value);
		Path logFilePath = loggerSupplier.getLogFilePath();
		String fileContent = Files.readString(logFilePath);
		Assert.assertTrue(fileContent.contains(value));
	}
}
