package org.mgwa.w40k.pairing.matrix.xls;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.MatrixWriter;
import org.mgwa.w40k.pairing.matrix.Score;
import org.mgwa.w40k.pairing.matrix.ScoreParser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XlsMatrixWriter implements MatrixWriter {

    public static XlsMatrixWriter fromStream(OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("No stream");
        }
        return new XlsMatrixWriter(outputStream);
    }

    private XlsMatrixWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    private final OutputStream outputStream;

    private static void createColumnNamesCells(XSSFRow row, int startColumnIndex, List<Army> armies) {
        IntStream.range(0, armies.size())
                .forEach(index -> {
                    XSSFCell cell = row.createCell(index + startColumnIndex, CellType.STRING);
                    cell.setCellValue(armies.get(index).getName());
                });
    }

    private static List<XSSFRow> createScoreRows(XSSFSheet sheet, int startRowIndex, List<Army> armies) {
        return IntStream.range(0, armies.size())
                .mapToObj(index -> {
                    XSSFRow row = sheet.createRow(startRowIndex + index);
                    XSSFCell cell = row.createCell(0, CellType.STRING);
                    cell.setCellValue(armies.get(index).getName());
                    return row;
                })
                .collect(Collectors.toList());
    }

    private static void createScoreCells(List<XSSFRow> scoreRows, int startColumnIndex,
                                         BiFunction<Integer, Integer, Optional<Score>> scoreGetter, int size) {
        if (scoreRows.size() != size) {
            throw new IllegalStateException("Inconsistent row count");
        }
        IntStream.range(0, size).forEach(rowIndex -> {
            IntStream.range(0, size).forEach(colIndex -> {
                XSSFCell cell = scoreRows.get(rowIndex)
                        .createCell(colIndex + startColumnIndex, CellType.STRING);
                scoreGetter.apply(rowIndex, colIndex).ifPresentOrElse(
                    (score) -> {
                        cell.setCellValue(ScoreParser.reverse().apply(score));
                    },
                    () ->  {
                        cell.setCellValue("?");
                    }
                );
            });
        });
    }

    private XSSFWorkbook createWorkBook(Matrix data) {
        XSSFWorkbook workBook = new XSSFWorkbook();
        XSSFSheet sheet = workBook.createSheet("scores");
        XSSFRow row = sheet.createRow(0);
        row.createCell(0); // Corner cell
        createColumnNamesCells(row, 1, data.getArmies(false));
        List<XSSFRow> scoreRows = createScoreRows(sheet, 1, data.getArmies(true));
        createScoreCells(scoreRows, 1, data::getScore, data.getSize());
        return workBook;
    }

    @Override
    public void write(Matrix data) throws IOException {
        try (XSSFWorkbook workBook = createWorkBook(data)) {
            workBook.write(outputStream);
        }
    }

    @Override
    public void close() throws Exception {
        outputStream.close();
    }
}
