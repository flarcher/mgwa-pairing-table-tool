package flarcher.pairing;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;
import java.util.Optional;

@Immutable
public class Table {

	public Table(int index, Optional<Army> rowArmy, Optional<Army> columnArmy) {
		this.index = index;
		if (rowArmy.isPresent() != columnArmy.isPresent()) {
			throw new IllegalArgumentException();
		}
		this.rowArmy = rowArmy;
		this.columnArmy = columnArmy;
	}

	private final int index;
	private final Optional<Army> rowArmy;
	private final Optional<Army> columnArmy;

	public int getIndex() {
		return index;
	}

	public Optional<Army> getRowArmy() {
		return rowArmy;
	}

	public Optional<Army> getColumnArmy() {
		return columnArmy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Table table = (Table) o;
		return index == table.index;
	}

	@Override
	public int hashCode() {
		return Objects.hash(index);
	}
}
