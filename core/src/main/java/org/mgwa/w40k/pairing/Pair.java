package org.mgwa.w40k.pairing;

import javax.annotation.concurrent.Immutable;
import java.util.*;

/**
 * Immutable pairing.
 */
@Immutable
public class Pair {

    public Pair(int row, int column) {
        this.row = row;
        this.column = column;
    }

    private final int row;
    private final int column;

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return row == pair.row && column == pair.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }

    public static int possiblePairsCount(int size) {
        if (size <= 0) {
            return 0;
        }
        else {
            return size * size;
        }
    }

    /**
     * @return All possible pairs from given rows and columns.
     */
    public static Collection<Pair> possiblePairs(Collection<Integer> rows, Collection<Integer> columns) {
        if (rows.size() != columns.size()) {
            throw new IllegalArgumentException("Inconsistent size of rows/columns");
        }
        Collection<Pair> result = new ArrayList<>(possiblePairsCount(rows.size()));
        for (int row : rows) {
            for (int col : columns) {
                result.add(new Pair(row, col));
            }
        }
        return result;
    }
}
