package org.mgwa.w40k.pairing.matrix.xls;

import org.mgwa.w40k.pairing.matrix.Matrix;
import org.junit.Assert;
import org.junit.Test;
import org.mgwa.w40k.pairing.matrix.Score;
import org.mgwa.w40k.pairing.matrix.ScoreParser;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Optional;

public class MatrixReaderTest {

	private final ScoreParser scoreParser = new ScoreParser();

	private void expectScoreString(Optional<Score> readScore, String strValue) {
		Assert.assertTrue(readScore.isPresent());
		Score parsedScore = scoreParser.applyOrFail(strValue);
		Assert.assertEquals(parsedScore, readScore.get());
	}

	private static XlsMatrixReader getReader(String resourcePath) {
		InputStream resourceAsStream = MatrixReaderTest.class.getResourceAsStream("/" + resourcePath);
		Assert.assertNotNull(resourceAsStream);
		return XlsMatrixReader.fromStream(new BufferedInputStream(resourceAsStream));
	}

	@Test
	public void testXlsRead() {
		try (XlsMatrixReader reader = getReader("example.xlsx")) {
			Matrix matrix = reader.get();
			Assert.assertEquals(8, matrix.getSize());
			expectScoreString(matrix.getScore(0,0), "10");
			expectScoreString(matrix.getScore(2,0), "0");
			expectScoreString(matrix.getScore(3,1), "20");
			expectScoreString(matrix.getScore(3, 7), "G");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			Assert.fail();
		}
	}

	@Test
	public void testXlsExportRead() {
		try (XlsMatrixReader reader = getReader("export.xlsx")) {
			Matrix matrix = reader.get();
			Assert.assertEquals(8, matrix.getSize());
			expectScoreString(matrix.getScore(0,0), "10");
			expectScoreString(matrix.getScore(2,0), "0");
			expectScoreString(matrix.getScore(3,1), "20");
			expectScoreString(matrix.getScore(3, 7), "G");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			Assert.fail();
		}
	}
}
