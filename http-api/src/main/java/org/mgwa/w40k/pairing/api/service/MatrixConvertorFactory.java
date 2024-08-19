package org.mgwa.w40k.pairing.api.service;

import org.mgwa.w40k.pairing.matrix.MatrixReader;
import org.mgwa.w40k.pairing.matrix.MatrixWriter;
import org.mgwa.w40k.pairing.matrix.xls.XlsMatrixReader;
import org.mgwa.w40k.pairing.matrix.xls.XlsMatrixWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

enum MatrixConvertorFactory {

    EXCEL_FORMAT(XlsMatrixReader::fromStream, XlsMatrixWriter::fromStream),
    JSON(JsonMatrixReader::fromStream, JsonMatrixWriter::fromStream),

    ;

    @FunctionalInterface
    interface MatrixReaderBuilder {
        MatrixReader newReader(InputStream is) throws IOException;
    }
    @FunctionalInterface
    interface MatrixWriterBuilder {
        MatrixWriter newWriter(OutputStream os) throws IOException;
    }

    MatrixConvertorFactory(MatrixReaderBuilder readerBuilder, MatrixWriterBuilder writerBuilder) {
        this.readerBuilder = Objects.requireNonNull(readerBuilder);
        this.writerBuilder = Objects.requireNonNull(writerBuilder);
    }

    private final MatrixReaderBuilder readerBuilder;
    private final MatrixWriterBuilder writerBuilder;

    MatrixReaderBuilder getReaderBuilder() {
        return readerBuilder;
    }

    MatrixWriterBuilder getWriterBuilder() {
        return writerBuilder;
    }
}
