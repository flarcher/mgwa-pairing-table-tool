package org.mgwa.w40k.pairing.matrix;

import org.mgwa.w40k.pairing.Army;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Matrix {

	public static Matrix createWithoutScores(List<Army> rowArmies, List<Army> colArmies) {
		Matrix matrix = new Matrix(rowArmies.size());
		matrix.setArmies(true, rowArmies);
		matrix.setArmies(false, colArmies);
		return matrix;
	}

	private Matrix(int armyCountInEachSize) {
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

	public Matrix setDefaultScore(Score score) {
		if (score == null) {
			throw new IllegalArgumentException("Null default score");
		}
		int size = getSize();
		for (int i=0; i < size; i++) {
			for (int j=0; j < size; j++) {
				if (scores[i][j] == null) {
					scores[i][j] = score.cloneIt();
				}
			}
		}
		return this;
	}

	/**
	 * @return {@code true} if all scores are defined.
	 */
	public boolean isComplete() {
		int size = getSize();
		for (int i=0; i < size; i++) {
			for (int j=0; j < size; j++) {
				if (scores[i][j] == null) {
					return false;
				}
			}
		}
		return true;
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
