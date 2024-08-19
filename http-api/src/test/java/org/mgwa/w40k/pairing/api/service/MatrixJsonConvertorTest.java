package org.mgwa.w40k.pairing.api.service;

import org.junit.Assert;
import org.junit.Test;
import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.MatrixReader;
import org.mgwa.w40k.pairing.matrix.MatrixWriter;
import org.mgwa.w40k.pairing.matrix.Score;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MatrixJsonConvertorTest {

    private static MatrixConvertorFactory FACTORY = MatrixConvertorFactory.JSON;
    private static Score DEFAULT_SCORE = Score.newDefault();
    private static Charset EXPORT_CHARSET = StandardCharsets.UTF_8;
    private static String STRING_EXPORT = "{\"match\":{\"row_team\":\"\",\"column_team\":\"\",\"team_member_count\":2},\"row_armies\":[{\"is_row\":true,\"index\":0,\"name\":\"foo\"},{\"is_row\":true,\"index\":1,\"name\":\"zub\"}],\"col_armies\":[{\"is_row\":false,\"index\":0,\"name\":\"bar\"},{\"is_row\":false,\"index\":1,\"name\":\"baz\"}],\"scores\":[[{\"min\":0,\"max\":20},{\"min\":8,\"max\":12}],[{\"min\":4,\"max\":10},{\"min\":0,\"max\":20}]]}";

    private static List<Army> generateArmies(String[] names, boolean isRow) {
        return IntStream.range(0, names.length)
                .mapToObj(index -> new Army(names[index], index, isRow))
                .collect(Collectors.toList());
    }

    private Matrix getTestMatrix() {
        Matrix m =  Matrix.createWithoutScores(
                generateArmies(new String[]{"foo", "zub"}, true),
                generateArmies(new String[]{"bar", "baz"}, false));
        m.setDefaultScore(DEFAULT_SCORE);
        m.setScore(0,1, Score.of(8, 12));
        m.setScore(1,0, Score.of(4, 10));
        return m;
    }

    private static void assertScore(Score expected, Matrix m, int row, int col) {
        Optional<Score> optScore = m.getScore(row, col);
        Assert.assertTrue(optScore.isPresent());
        Assert.assertEquals(expected, optScore.get());
    }

    private void assertMatrixContent(Matrix m) {
        Assert.assertEquals("foo", m.getArmies(true).get(0).getName());
        Assert.assertEquals("zub", m.getArmies(true).get(1).getName());
        Assert.assertEquals("bar", m.getArmies(false).get(0).getName());
        Assert.assertEquals("baz", m.getArmies(false).get(1).getName());
        assertScore(DEFAULT_SCORE, m,0, 0);
        assertScore(Score.of(8, 12), m,0, 1);
        assertScore(Score.of(4, 10), m,1, 0);
        assertScore(DEFAULT_SCORE, m,1, 1);
    }

    @Test
    public void testJsonToStream() {
        Matrix testMatrix = getTestMatrix();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            MatrixWriter matrixWriter = FACTORY.getWriterBuilder().newWriter(os);
            matrixWriter.write(testMatrix);
            matrixWriter.close();
        }
        catch (Exception exception) {
            exception.printStackTrace(System.err);
            Assert.fail();
        }
        String stringExport = os.toString(EXPORT_CHARSET);
        Assert.assertEquals(STRING_EXPORT, stringExport);
    }

    @Test
    public void testStreamToJson() {
        byte[] data = STRING_EXPORT.getBytes(EXPORT_CHARSET);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try {
            MatrixReader matrixReader = FACTORY.getReaderBuilder().newReader(bais);
            Matrix m = matrixReader.read();
            matrixReader.close();
            assertMatrixContent(m);
        }
        catch (Exception exception) {
            exception.printStackTrace(System.err);
            Assert.fail();
        }
    }
}
