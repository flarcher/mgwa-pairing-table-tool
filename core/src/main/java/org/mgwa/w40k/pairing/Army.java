package org.mgwa.w40k.pairing;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Immutable
public class Army {

	public static List<Army> createArmies(Collection<String> names, boolean isRow) {
		AtomicInteger i = new AtomicInteger();
		return names.stream()
				.map(name -> new Army(name, i.getAndIncrement(), isRow))
				.collect(Collectors.toList());
	}

	public static Army renamed(Army originalArmy, String newName) {
		return new Army(newName, originalArmy.index, originalArmy.isRow);
	}

	/**
	 * Represents a player's army.
	 * @param name Army name.
	 * @param index  Index starting with 1 (not zero-based)
	 * @param isRow Is the army on a row or a column from the data source.
	 */
	public Army(String name, int index, boolean isRow) {
		this.name = name;
		this.index = index;
		this.isRow = isRow;
	}

	private final String name;
	private final int index;
	private final boolean isRow;

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public boolean isRow() {
		return isRow;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Army army = (Army) o;
		return index == army.index &&
				isRow == army.isRow;
	}

	@Override
	public int hashCode() {
		return Objects.hash(index, isRow);
	}
}
