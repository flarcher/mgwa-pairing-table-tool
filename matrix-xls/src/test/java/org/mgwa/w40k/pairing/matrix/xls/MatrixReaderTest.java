package org.mgwa.w40k.pairing.matrix.xls;

import org.mgwa.w40k.pairing.matrix.Matrix;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class MatrixReaderTest {

	@Test
	public void testXlsRead() {
		InputStream resourceAsStream = MatrixReaderTest.class.getResourceAsStream("/example.xlsx");
		try (XlsMatrixReader reader = XlsMatrixReader.fromStream(resourceAsStream)) {
			Matrix matrix = reader.get();
			Assert.assertEquals(8, matrix.getSize());
			Assert.assertEquals(10, matrix.getScore(0, 0).get().getValue());
			Assert.assertEquals(0, matrix.getScore(2, 0).get().getValue());
			Assert.assertEquals(20, matrix.getScore(3, 1).get().getValue());
			Assert.assertFalse(matrix.getScore(3, 7).isPresent());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			Assert.fail();
		}
	}
}
