package org.mgwa.w40k.pairing.matrix.xls;

import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.MatrixReader;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mgwa.w40k.pairing.matrix.ScoreParser;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class XlsMatrixReader implements MatrixReader {

	public static XlsMatrixReader fromFile(File inputFile) throws IOException {
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

	public static XlsMatrixReader fromStream(InputStream inputStream) throws IOException {
		if (inputStream == null) {
			throw new IllegalArgumentException("No stream");
		}
		return new XlsMatrixReader(inputStream);
	}

	private XlsMatrixReader(InputStream inputStream) throws IOException {
		this.document = new XSSFWorkbook(inputStream);
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
		LOGGER.finer(String.format("Read armies: %s", armies.toString()));
	}

	private static List<Army> getColumnArmies(XSSFCell origin) {
		XSSFCell cell = origin;
		int index = 0;
		List<Army> columnArmies = new ArrayList<>();
		do {
			columnArmies.add(new Army(cell.getStringCellValue(), index++, false));
			cell = cell.getRow().getCell(cell.getColumnIndex() + 1);
		} while (notEmptyCell(cell));
		return columnArmies;
	}

	private static List<Army> getRowArmies(XSSFCell origin) {
		List<Army> rowArmies = new ArrayList<>();
		XSSFRow row = origin.getSheet()
				.getRow(origin.getRowIndex() + 1);
		if (row == null) {
			return rowArmies; // Empty list
		}
		int index = 0;
		XSSFCell cell = row.getCell(origin.getColumnIndex() - 1);
		do {
			rowArmies.add(new Army(cell.getStringCellValue(), index++, true));
			row = cell.getSheet().getRow(cell.getRowIndex() + 1);
			cell = row != null ? row.getCell(cell.getColumnIndex()) : null;
		} while (notEmptyCell(cell));
		return rowArmies;
	}

	@Override
	public Matrix read() {
		XSSFCell origin = getOrigin();

		// Reading column armies
		List<Army> columnArmies = getColumnArmies(origin);
		printArmies(columnArmies);

		// Reading row armies
		List<Army> rowArmies = getRowArmies(origin);
		printArmies(rowArmies);

		if (rowArmies.size() != columnArmies.size()) {
			throw new IllegalArgumentException("Not same army count on each side?");
		}

		Matrix matrix = Matrix.createWithoutScores(rowArmies, columnArmies);

		fillMatrix(matrix,
			origin.getSheet().getRow(origin.getRowIndex() + 1).getCell(origin.getColumnIndex()));

		return matrix;
	}

	private static Optional<String> readString(XSSFCell cell) {
		if (!notEmptyCell(cell)) {
			return Optional.empty();
		}
		switch (cell.getCellType()) {
			case STRING:
				String value = cell.getStringCellValue();
				//LOGGER.info(() -> String.format("Reading string cell %s at %d:%d", value, cell.getRowIndex(), cell.getColumnIndex()));
				return Optional.of(value);
			case NUMERIC:
				value = Integer.toString((int) cell.getNumericCellValue());
				//LOGGER.info(() -> String.format("Reading numeric cell %s at %d:%d", value, cell.getRowIndex(), cell.getColumnIndex()));
				return Optional.of(value);
			default:
				LOGGER.warning(() -> String.format("Unable to handle type of cell at %d:%d", cell.getRowIndex(), cell.getColumnIndex()));
				return Optional.empty();
		}
	}

	private final ScoreParser scoreParser = new ScoreParser();

	private void fillMatrix(Matrix matrix, XSSFCell origin) {
		XSSFCell cell;
		int size = matrix.getSize();
		AtomicBoolean hasError = new AtomicBoolean(false);
		for (int row = 0; row < size; row++) {
			for (int column = 0; column < size; column++) {
				cell = origin.getSheet()
					.getRow(origin.getRowIndex() + row)
					.getCell(origin.getColumnIndex() + column);
				final int finalRow = row;
				final int finalCol = column;
				readString(cell)
					.map(scoreParser)
					.ifPresentOrElse(
						s -> matrix.setScore(finalRow, finalCol, s),
						() -> {
							hasError.set(true);
							LOGGER.severe(String.format("Invalid cell at %d:%d", finalRow, finalCol));
						}
					);
			}
		}
		if (hasError.get()) {
			LOGGER.severe("Error(s) during reading of the input");
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
