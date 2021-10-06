package org.mgwa.w40k.pairing.matrix;

import org.mgwa.w40k.pairing.Army;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Matrix {

	public Matrix(int armyCountInEachSize) {
		rowArmies = new Army[armyCountInEachSize];
		colArmies = new Army[armyCountInEachSize];
		scores = new Score[armyCountInEachSize][armyCountInEachSize];
	}

	private final Army[] rowArmies;
	private final Army[] colArmies;
	private final Score[][] scores;

	public int getSize() {
		return scores.length;
	}

	public Optional<Score> getScore(int row, int column) {
		return Optional.ofNullable(scores[row][column]);
	}

	public Matrix setScore(int row, int column, Score score) {
		scores[row][column] = score != null && score.getCount() > 0 ? score : null;
		return this;
	}

	public List<Army> getArmies(boolean isRow) {
		return Arrays.asList(isRow ? rowArmies : colArmies);
	}

	public Matrix setArmies(boolean isRow, List<Army> armies) {
		if (armies.size() != rowArmies.length) {
			throw new IllegalArgumentException("Unexpected size");
		}
		armies.toArray(isRow ? rowArmies : colArmies);
		return this;
	}

	@Override
	public String toString() {
		return "Matrix{" +
				"rowArmies=" + Arrays.toString(rowArmies) +
				", colArmies=" + Arrays.toString(colArmies) +
				", scores=" + Arrays.toString(scores) +
				'}';
	}
}
