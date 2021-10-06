package org.mgwa.w40k.pairing.matrix;

import java.util.function.Supplier;

public interface MatrixReader
	extends
		AutoCloseable,
		Supplier<Matrix> {

}
