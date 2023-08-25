package org.mgwa.w40k.pairing;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.function.Predicate;

/**
 * Immutable pairing.
 */
@Immutable
public class Pair implements Comparable<Pair> {

    private static final short MAX_VALUE = 1 << 14;

    public static Pair of(int row, int column) {
        return of((short) row, (short) column);
    }

    public static Pair of(short row, short column) {
        if (row < 0 || column < 0 || row >= MAX_VALUE || column >= MAX_VALUE) {
            throw new IllegalArgumentException("Illegal row/column index");
        }
        return new Pair(row, column);
    }

    private Pair(short row, short column) {
        this.row = row;
        this.column = column;
    }

    private final short row;
    private final short column;

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    int uniqueValue() {
        return row | (column << 15);
    }

    @Override
    public int compareTo(Pair pair) {
        return Integer.compare(uniqueValue(), pair.uniqueValue());
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
        return uniqueValue();
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
                result.add(new Pair((short) row, (short) col));
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

    public static Predicate<Pair> isWithArmy(Army army) {
        return pair -> {
            if (army.isRow()) {
                return pair.getRow() == army.getIndex();
            } else {
                return pair.getColumn() == army.getIndex();
            }};
    }
}
