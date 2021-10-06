package org.mgwa.w40k.pairing;

import java.util.*;
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

	public Stream<Assignment> completedAssignments(
			Collection<Army> rowArmies,
			Collection<Army> colArmies
	) {

		List<Integer> unassignedTableIndices = IntStream.range(0, getTableCount())
				.filter(i -> !isAssigned(i))
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		if (   unassignedTableIndices.size() != rowArmies.size()
			|| unassignedTableIndices.size() != colArmies.size()) {
			throw new IllegalArgumentException("Illegal army list size (not " + unassignedTableIndices.size() + ")");
		}

		switch (unassignedTableIndices.size()) {
			case 0:
				return Stream.of(this);
			case 1:
				return Stream.of(clone().assign(
						unassignedTableIndices.get(0),
						rowArmies.iterator().next(),
						colArmies.iterator().next()));
			default:
				// See below
		}

		Stream.Builder<Assignment> builder = Stream.builder();

		rowArmies.forEach(rowArmy -> {
			Set<Army> remainingRowArmies = new HashSet<>(rowArmies);
			remainingRowArmies.remove(rowArmy);

			colArmies.forEach(colArmy -> {
				Set<Army> remainingColArmies = new HashSet<>(colArmies);
				remainingColArmies.remove(colArmy);

				unassignedTableIndices.forEach(tableIndex -> clone()
					.assign(tableIndex, rowArmy, colArmy)
					.completedAssignments(remainingRowArmies, remainingColArmies) // Recursive call
					.forEach(builder));
			});
		});

		return builder.build();
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
