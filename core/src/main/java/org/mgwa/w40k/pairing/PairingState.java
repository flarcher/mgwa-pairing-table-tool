package org.mgwa.w40k.pairing;

import java.util.BitSet;
import java.util.function.Predicate;

/**
 * Represents a state about the pairing (without table assignment).
 * See {@link Assignment} For another state description, including the assignment with tables.
 */
class PairingState implements Predicate<Pair>, Cloneable {

	PairingState(int tableCount) {
		this(new BitSet(tableCount), new BitSet(tableCount), tableCount);
	}

	private PairingState(BitSet rowsSet, BitSet colsSet, int count) {
		assignedRows = rowsSet;
		assignedColumns = colsSet;
		size = count;
		remainingAssignment = count;
	}

	private final BitSet assignedRows;
	private final BitSet assignedColumns;
	private final int size;
	private int remainingAssignment;

	public int getSize() {
		return  size;
	}

	public int getAssignLeftCount() {
		return remainingAssignment;
	}

	PairingState assign(Pair pair) {
		assignedRows.set(pair.getRow());
		assignedColumns.set(pair.getColumn());
		remainingAssignment--;
		if (remainingAssignment < 0) {
			throw new IllegalStateException();
		}
		return this;
	}

	boolean isNotAssigned(Pair pair) {
		return !assignedRows.get(pair.getRow()) &&
		       !assignedColumns.get(pair.getColumn());
	}

	@Override
	public boolean test(Pair pair) {
		return isNotAssigned(pair);
	}

	protected PairingState cloneIt() {
		return new PairingState(
				(BitSet) assignedRows.clone(),
				(BitSet) assignedColumns.clone(),
				remainingAssignment);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return cloneIt();
	}
}
