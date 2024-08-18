package org.mgwa.w40k.pairing.matrix;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Writes the matrix information.
 */
public interface MatrixWriter
    extends
        AutoCloseable,
        Consumer<Matrix> {

    void write(Matrix data) throws IOException;

    @Override
    default void accept(Matrix matrix) {
        try {
            write(matrix);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
}
