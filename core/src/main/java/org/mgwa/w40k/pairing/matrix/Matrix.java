package org.mgwa.w40k.pairing.matrix;

import org.mgwa.w40k.pairing.Army;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Matrix implements Cloneable {

	public static Matrix createWithoutScores(List<Army> rowArmies, List<Army> colArmies) {
		Matrix matrix = new Matrix(rowArmies.size());
		matrix.setArmies(true, rowArmies);
		matrix.setArmies(false, colArmies);
		return matrix;
	}

	private Matrix(int armyCountInEachSize) {
		this(new Army[armyCountInEachSize], new Army[armyCountInEachSize], new Score[armyCountInEachSize][armyCountInEachSize]);
	}

	private Matrix(Army[] rowArmies, Army[] colArmies, Score[][] scores) {
		this.rowArmies = rowArmies;
		this.colArmies = colArmies;
		this.scores = scores;
	}

	private final Army[] rowArmies;
	private final Army[] colArmies;
	private final Score[][] scores;

	public int getSize() {
		return scores.length;
	}

	public Matrix withSize(int newSize, Score defaultScore) {
		Army[] rowArmies = Arrays.copyOf(this.rowArmies, newSize);
		Army[] colArmies = Arrays.copyOf(this.rowArmies, newSize);
		Score[][] scores = new Score[newSize][];
		int originalSize = getSize();
		for (int i = 0; i < originalSize; i++) {
			scores[i] = Arrays.copyOf(this.scores[i], newSize);
		}
		if (originalSize < newSize) { // Attempt to avoid later NPEs
			for (int j = originalSize; j < newSize; j++) {
				rowArmies[j] = new Army("", j, true);
				colArmies[j] = new Army("", j, false);
				scores[j] = new Score[newSize];
			}
		}
		Matrix m = new Matrix(rowArmies, colArmies, scores);
		if (originalSize < newSize) {
			return m.setDefaultScore(defaultScore);
		}
		else {
			return m;
		}
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

	public Matrix cloneIt() {
		return withSize(getSize(), Score.newDefault());
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return cloneIt();
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
