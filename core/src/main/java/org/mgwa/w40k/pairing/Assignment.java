package org.mgwa.w40k.pairing;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An assignment of armies on several tables.
 */
public class Assignment implements Cloneable {

	static Assignment createEmpty(int tableCount) {
		return new Assignment(
				new int[tableCount],  // Filled with 0
				new int[tableCount]   // Filled with 0
		);
	}

	private Assignment(int[] rowArmyIndexes, int[] colArmyIndexes) {
		this.colArmyIndexes = colArmyIndexes;
		this.rowArmyIndexes = rowArmyIndexes;
	}

	private final int[] rowArmyIndexes; // 0 means not-assigned
	private final int[] colArmyIndexes; // 0 means not-assigned

	public Assignment assign(int table, Army row, Army column) {
		return assign(table, row.getIndex(), column.getIndex());
	}

	private Assignment assign(int table, int row, int column) {
		rowArmyIndexes[table] = row + 1;
		colArmyIndexes[table] = column + 1;
		return this;
	}

	private boolean isAssigned(Army army, boolean isRow) {
		int[] tables = isRow ? rowArmyIndexes : colArmyIndexes;
		for (int index : tables) {
			if (index == army.getIndex()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isIndexAssigned(int index) {
		return index != 0;
	}

	public int getRowArmyIndex(int table) {
		return rowArmyIndexes[table] - 1;
	}

	public int getColArmyIndex(int table) {
		return colArmyIndexes[table] - 1;
	}

	public boolean isAssigned(int table) {
		return isIndexAssigned(rowArmyIndexes[table]) && isIndexAssigned(colArmyIndexes[table]);
	}

	public boolean contains(Army row, Army column) {
		return contains(row.getIndex(), column.getIndex());
	}

	public boolean contains(int row, int column) {
		return IntStream.range(0, getTableCount())
				.anyMatch(i -> getRowArmyIndex(i) == row && getColArmyIndex(i) == column);
	}

	public OptionalInt getTableOf(int row, int column) {
		return IntStream.range(0, getTableCount())
				.filter(i -> getRowArmyIndex(i) == row && getColArmyIndex(i) == column)
				.findAny();
	}

	public OptionalInt getTableOfRow(int row) {
		return IntStream.range(0, getTableCount())
				.filter(i -> getRowArmyIndex(i) == row)
				.findAny();
	}

	public OptionalInt getTableOfColumn(int column) {
		return IntStream.range(0, getTableCount())
				.filter(i -> getColArmyIndex(i) == column)
				.findAny();
	}

	private static Optional<Army> fromIndex(int[] array, List<Army> armies, int tableIndex) {
		int armyIndex = array[tableIndex] - 1;
		return !isIndexAssigned(armyIndex) ? Optional.empty() : Optional.of(armies.get(armyIndex));
	}

	public Table getTable(
			List<Army> rowArmies,
			List<Army> colArmies,
			int index) {
		return new Table(index,
				fromIndex(rowArmyIndexes, rowArmies, index),
				fromIndex(colArmyIndexes, colArmies, index));
	}

	public Stream<Table> getTables(
			List<Army> rowArmies,
			List<Army> colArmies) {
		int l = getTableCount();
		if (l == 0) {
			return Stream.empty();
		}
		return IntStream.range(0, l)
				.mapToObj(index -> getTable(rowArmies, colArmies, index));
	}

	public boolean isComplete() {
		return IntStream.concat(
				Arrays.stream(rowArmyIndexes),
				Arrays.stream(colArmyIndexes)
		).allMatch(Assignment::isIndexAssigned);
	}

	public int getTableCount() {
		return rowArmyIndexes.length;
	}

	private static IntStream unassignedArmies(int[] armyIndices) {
		return Arrays.stream(armyIndices)
				.filter(i -> !isIndexAssigned(i));
	}

	/**
	 * @param rowArmies Row-based armies
	 * @param colArmies Column-based armies
	 * @return All possible completed assignments from this assignment (or itself if this assignment is complete).
	 */
	public Stream<Assignment> completedAssignments(
			Collection<Army> rowArmies,
			Collection<Army> colArmies
	) {
		int mySize = getTableCount();
		if (   mySize != rowArmies.size()
			|| mySize != colArmies.size()) {
			throw new IllegalArgumentException("Illegal army list size (not " + mySize + ")");
		}

		long rowCount = rowArmies.stream().map(Army::getIndex).distinct().count();
		long colCount = colArmies.stream().map(Army::getIndex).distinct().count();
		if (   mySize != rowCount
			|| mySize != colCount) {
			throw new IllegalArgumentException("Same index for several Armies (in rows or in columns");
		}

		return completedAssignments(rowArmies, colArmies, true);
	}

	/**
	 * @param rowArmies Row-based armies
	 * @param colArmies Column-based armies
     * @param initialCall If true, it means that there are possibly some assigned tables
	 * @return All possible completed assignments from this assignment (or itself if this assignment is complete).
	 */
	private Stream<Assignment> completedAssignments(
			Collection<Army> rowArmies,
			Collection<Army> colArmies,
			boolean initialCall) {

		Set<Integer> assignedRowArmies = initialCall ? new HashSet<>() : Collections.emptySet();
		Set<Integer> assignedColArmies = initialCall ? new HashSet<>() : Collections.emptySet();
		List<Integer> unassignedTableIndices = IntStream.range(0, getTableCount())
				.filter(tableIndex -> {
					if (isAssigned(tableIndex)) {
						if (initialCall) {
							assignedRowArmies.add(getRowArmyIndex(tableIndex));
							assignedColArmies.add(getColArmyIndex(tableIndex));
						}
						return false;
					}
					return true;
				})
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

		switch (unassignedTableIndices.size()) {
			case 0:
				return Stream.of(this);
			case 1: {
				Army unassignedRowArmy = !initialCall
					? rowArmies.iterator().next()
					: rowArmies.stream()
						.filter(army -> !assignedRowArmies.contains(army.getIndex()))
						.findAny().orElseThrow();
				Army unassignedColArmy = !initialCall
					? colArmies.iterator().next()
					: colArmies.stream()
						.filter(army -> !assignedColArmies.contains(army.getIndex()))
						.findAny().orElseThrow();
				return Stream.of(clone().assign(unassignedTableIndices.get(0), unassignedRowArmy, unassignedColArmy));
			}
			default:
				// See below
		}

		Stream.Builder<Assignment> builder = Stream.builder();

		Collection<Army> unassignedRowArmies = initialCall
			? rowArmies.stream()
				.filter(army -> !assignedRowArmies.contains(army.getIndex()))
				.collect(Collectors.toList())
			: rowArmies;
		Collection<Army> unassignedColArmies = initialCall
			? colArmies.stream()
				.filter(army -> !assignedColArmies.contains(army.getIndex()))
				.collect(Collectors.toList())
			: colArmies;

		unassignedRowArmies.forEach(rowArmy -> {
			Collection<Army> remainingRowArmies = unassignedRowArmies.stream()
				.filter(ura -> ura.getIndex() != rowArmy.getIndex())
				.collect(Collectors.toList());

			unassignedColArmies.forEach(colArmy -> {
				Collection<Army> remainingColArmies = unassignedColArmies.stream()
						.filter(uca -> uca.getIndex() != colArmy.getIndex())
						.collect(Collectors.toList());

				unassignedTableIndices.forEach(tableIndex -> clone()
					.assign(tableIndex, rowArmy, colArmy)
					.completedAssignments(remainingRowArmies, remainingColArmies, false) // Recursive call
					.forEach(builder));
			});
		});

		return builder.build();
	}

	public int getUnassignedTableCount() {
		return (int) IntStream.range(0, getTableCount()).filter(this::isAssigned).count();
	}

	/**
	 * @return Possibles pairs to be done based on unassigned tables.
	 */
	public Collection<Pair> possiblePairs() {
		int[] assignedTables = IntStream.range(0, getTableCount())
				.filter(this::isAssigned)
				.toArray();
		Set<Integer> assignedRows = new HashSet<>(assignedTables.length);
		Set<Integer> assignedCols = new HashSet<>(assignedTables.length);
		IntStream.of(assignedTables).forEach(i -> {
			assignedRows.add(getRowArmyIndex(i));
			assignedCols.add(getColArmyIndex(i));
		});
		List<Integer> unassignedRows = new ArrayList<>(getTableCount() - assignedTables.length);
		List<Integer> unassignedCols = new ArrayList<>(getTableCount() - assignedTables.length);
		for (int i = 0; i < getTableCount() /* == Armies count */; i++) {
			if (!assignedRows.contains(i)) {
				unassignedRows.add(i);
			}
			if (!assignedCols.contains(i)) {
				unassignedCols.add(i);
			}
		}
		return Pair.possiblePairs(unassignedRows, unassignedCols);
	}

	@Override
	public Assignment clone() {
		return new Assignment(rowArmyIndexes.clone(), colArmyIndexes.clone());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Assignment that = (Assignment) o;
		return Arrays.equals(rowArmyIndexes, that.rowArmyIndexes) &&
				Arrays.equals(colArmyIndexes, that.colArmyIndexes);
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(rowArmyIndexes);
		result = 31 * result + Arrays.hashCode(colArmyIndexes);
		return result;
	}

	public String toString(
			List<Army> rowArmies,
			List<Army> colArmies
	) {
		final int l = getTableCount();
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < l; i++) {
			if (isAssigned(i)) {
				str
				.append('[')
				.append(rowArmies.get(rowArmyIndexes[i] - 1).getName())
				.append('|')
				.append(colArmies.get(colArmyIndexes[i] - 1).getName())
				.append(']');
			}
			else {
				str.append("[-|-]");
			}
		}
		return str.toString();
	}

	@Override
	public String toString() {
		return "Assignment{" +
				"rowArmyIndexes=" + Arrays.toString(rowArmyIndexes) +
				", colArmyIndexes=" + Arrays.toString(colArmyIndexes) +
				'}';
	}
}
