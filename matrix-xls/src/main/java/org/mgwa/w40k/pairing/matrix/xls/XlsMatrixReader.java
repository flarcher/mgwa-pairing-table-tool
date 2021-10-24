package org.mgwa.w40k.pairing.matrix.xls;

import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.MatrixReader;
import org.mgwa.w40k.pairing.matrix.Score;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class XlsMatrixReader implements MatrixReader {

	public static XlsMatrixReader fromFile(File inputFile) {
		if (!inputFile.canRead()) {
			throw new IllegalArgumentException(String.format("Can not read %s", inputFile.getAbsolutePath()));
		}
		try {
			return new XlsMatrixReader(new FileInputStream(inputFile));
		}
		catch (FileNotFoundException fnf) {
			throw new IllegalArgumentException(String.format("Can not find %s", inputFile.getAbsolutePath()));
		}
	}

	public static XlsMatrixReader fromStream(InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("No stream");
		}
		return new XlsMatrixReader(inputStream);
	}

	private XlsMatrixReader(InputStream inputStream) {
		try {
			this.document = new XSSFWorkbook(inputStream);
		}
		catch (IOException ioe) {
			try {
				inputStream.close();
			}
			catch (IOException ioe2) {}
			throw new IllegalStateException(ioe);
		}
	}

	private final XSSFWorkbook document;
	private final static int MAX_ORIGIN_SEARCH_CELL_DISTANCE = 10;
	private final static Logger LOGGER = LoggerSupplier.INSTANCE.getLogger();

	private static boolean notEmptyCell(XSSFCell cell) {
		return cell != null && cell.getCellType() != CellType.BLANK;
	}

	private XSSFCell getOrigin() {
		XSSFSheet sheet = document.getSheetAt(0);// Assuming we use the first sheet
		for (int i = 0; i < MAX_ORIGIN_SEARCH_CELL_DISTANCE; i++) {
			XSSFRow row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			for (int j = 0; j < MAX_ORIGIN_SEARCH_CELL_DISTANCE; j++) {
				XSSFCell cell = row.getCell(j);
				if (notEmptyCell(cell)) {
					return cell;
				}
			}
		}
		throw new IllegalArgumentException("No content found");
	}

	private static void printArmies(List<Army> armies) {
		System.out.println(armies.toString());
	}

	@Override
	public Matrix get() {
		XSSFCell origin = getOrigin();
		XSSFCell cell = origin;

		// Reading column armies
		int index = 0;
		List<Army> columnArmies = new ArrayList<>();
		do {
			columnArmies.add(new Army(cell.getStringCellValue(), index++, false));
			cell = cell.getRow().getCell(cell.getColumnIndex() + 1);
		} while (notEmptyCell(cell));
		printArmies(columnArmies);

		// Reading row armies
		index = 0;
		List<Army> rowArmies = new ArrayList<>();
		cell = origin.getSheet()
				.getRow(origin.getRowIndex() + 1)
				.getCell(origin.getColumnIndex() - 1);
		do {
			rowArmies.add(new Army(cell.getStringCellValue(), index++, true));
			cell = cell.getSheet().getRow(cell.getRowIndex() + 1).getCell(cell.getColumnIndex());
		} while (notEmptyCell(cell));
		printArmies(rowArmies);

		if (rowArmies.size() != columnArmies.size()) {
			throw new IllegalArgumentException("Not same army count on each side?");
		}

		int armyCount = rowArmies.size();
		Matrix matrix = new Matrix(armyCount);
		matrix.setArmies(false, columnArmies);
		matrix.setArmies(true, rowArmies);

		fillMatrix(matrix,
			origin.getSheet().getRow(origin.getRowIndex() + 1).getCell(origin.getColumnIndex()));

		return matrix;
	}

	private static Optional<Integer> readInteger(XSSFCell cell) {
		switch (cell.getCellType()) {
			case STRING:
				String strValue = cell.getStringCellValue();
				try {
					return Optional.of(Integer.parseUnsignedInt(strValue));
				}
				catch (NumberFormatException e) {
					LOGGER.warning(() -> String.format("Unable to read cell value %s as a score", strValue));
					return Optional.empty();
					/*throw new IllegalArgumentException(String.format("Impossible to read score from cell @%s:%s from %s",
							cell.getRowIndex(), cell.getColumnIndex(), strValue), e);*/
				}
			case NUMERIC:
				return Optional.of(Double.valueOf(cell.getNumericCellValue()).intValue());
			default:
				LOGGER.warning(() -> String.format("Unable to handle type of cell at row:%d and column:%d", cell.getRowIndex(), cell.getColumnIndex()));
				return Optional.empty();
				/*throw new IllegalArgumentException(String.format("Impossible to read score from cell @%s:%s",
						cell.getRowIndex(), cell.getColumnIndex()));*/
		}
	}

	private void fillMatrix(Matrix matrix, XSSFCell origin) {
		XSSFCell cell;
		int size = matrix.getSize();
		for (int row = 0; row < size; row++) {
			for (int column = 0; column < size; column++) {
				cell = origin.getSheet()
						.getRow(origin.getRowIndex() + row)
						.getCell(origin.getColumnIndex() + column);
				Score score = readInteger(cell).map(Score::new).orElseGet(Score::new);
				matrix.setScore(row, column, score);
			}
		}
	}

	@Override
	public void close() throws Exception {
		try {
			document.close();
		}
		catch (IOException ioe) {
			throw new IllegalStateException(ioe);
		}
	}
}
