package org.mgwa.w40k.pairing.matrix;

import java.util.function.Supplier;

/**
 * Reads a Matrix.
 */
public interface MatrixReader
	extends
		AutoCloseable,
		Supplier<Matrix> {

	Matrix read();

	@Override
	default Matrix get() {
		return read();
	}
}
