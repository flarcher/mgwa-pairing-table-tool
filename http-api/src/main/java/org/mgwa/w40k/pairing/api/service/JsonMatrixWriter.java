package org.mgwa.w40k.pairing.api.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.mgwa.w40k.pairing.api.model.EstimatedScore;
import org.mgwa.w40k.pairing.api.model.Match;
import org.mgwa.w40k.pairing.api.model.SetupOverview;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.MatrixWriter;
import org.mgwa.w40k.pairing.matrix.Score;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class JsonMatrixWriter implements MatrixWriter {

    public static JsonMatrixWriter fromStream(OutputStream os) throws IOException {
        Objects.requireNonNull(os);
        JsonFactory factory = new JsonFactory();
        JsonGenerator generator = factory.createGenerator(os);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module()); // Optional<T>
        generator.setCodec(mapper);
        return new JsonMatrixWriter(generator);
    }

    private JsonMatrixWriter(JsonGenerator generator) {
        this.generator = generator;
    }

    private final JsonGenerator generator;

    @Override
    public void write(Matrix matrixData) throws IOException {
        SetupOverview overview = SetupOverview.from(
                new Match("", "", matrixData.getSize()),
                matrixData,
                EstimatedScore.from(Score.newDefault()));
        generator.writeObject(overview);
    }

    @Override
    public void close() throws Exception {
        generator.close();
    }
}
