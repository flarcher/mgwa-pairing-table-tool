package org.mgwa.w40k.pairing;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.function.Predicate;

/**
 * Immutable pairing.
 */
@Immutable
public class Pair {

    public static Pair of(int row, int column) {
        if (row < 0 || column < 0) {
            throw new IllegalArgumentException("Illegal row/column index");
        }
        return new Pair(row, column);
    }

    private Pair(int row, int column) {
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
        return "["+row+","+column+"]";
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

    //--- Usual predicates about pairs

    public static Predicate<Pair> afterAssignmentOf(Pair pair) {
        return p -> p.getColumn() != pair.getColumn()
                 && p.getRow()    != pair.getRow();
    }

    public static Predicate<Pair> isWithRow(int rowToAssign) {
        return p -> p.getRow() == rowToAssign;
    }

    public static Predicate<Pair> isWithColumn(int columnToAssign) {
        return p -> p.getColumn() == columnToAssign;
    }
}
