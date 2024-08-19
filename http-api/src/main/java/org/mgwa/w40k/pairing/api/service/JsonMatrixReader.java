package org.mgwa.w40k.pairing.api.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mgwa.w40k.pairing.api.model.SetupOverview;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.MatrixReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

class JsonMatrixReader implements MatrixReader {

    static JsonMatrixReader fromStream(InputStream is) throws IOException {
        JsonFactory factory = new JsonFactory();
        return new JsonMatrixReader(factory.createParser(is));
    }

    private JsonMatrixReader(JsonParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    //private final JsonFactory factory = new JsonFactory();
    private final JsonParser parser;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private void checkContent() throws IOException {
        while (true) {
            JsonToken token = parser.nextToken();
            if (token == null) {
                throw new IllegalArgumentException("No content");
            }
            if (!JsonToken.START_OBJECT.equals(token)) {
                throw new IllegalArgumentException("Expecting JSON object");
            }
            else {
                break;
            }
        }
    }

    private Matrix parseObjectNode() throws IOException {
        SetupOverview overview = mapper.readValue(parser, SetupOverview.class);
        return overview.getMatrix();
    }

    @Override
    public Matrix read() throws IOException {
        checkContent();
        return parseObjectNode();
    }

    @Override
    public void close() throws Exception {
        parser.close();
    }
}
