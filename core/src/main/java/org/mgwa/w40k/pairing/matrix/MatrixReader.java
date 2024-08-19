package org.mgwa.w40k.pairing.matrix;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Reads a Matrix.
 */
public interface MatrixReader
	extends
		AutoCloseable,
		Supplier<Matrix> {

	Matrix read() throws IOException;

	@Override
	default Matrix get() {
		try {
			return read();
		} catch (IOException ioe) {
			throw new IllegalStateException(ioe);
		}
	}
}
