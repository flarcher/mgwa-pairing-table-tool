package org.mgwa.w40k.pairing.state;

import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.matrix.Score;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * A mutable class that describes the application state.
 */
public class AppState {

	public static final AppState INSTANCE = new AppState();

	public static final int DEFAULT_ARMY_COUNT = 4;

	/** Simple singleton pattern: forces to use one state. */
	private AppState() {}

	private String rowTeamName = "";
	private String colTeamName = "";
	private boolean youHaveTheTableToken = true;
	private Optional<Path> matrixFilePath = Optional.empty();
	private Optional<Matrix> scoreMatrix = Optional.empty();
	private int armyCount = DEFAULT_ARMY_COUNT;

	//-- getters and setters

	public String getRowTeamName() {
		return rowTeamName;
	}

	public void setRowTeamName(String rowTeamName) {
		this.rowTeamName = Objects.requireNonNull(rowTeamName);
	}

	public String getColTeamName() {
		return colTeamName;
	}

	public void setColTeamName(String colTeamName) {
		this.colTeamName = Objects.requireNonNull(colTeamName);
	}

	public boolean youHaveTheTableToken() {
		return youHaveTheTableToken;
	}

	public void setYouHaveTheTableToken(boolean youHaveTheTableToken) {
		this.youHaveTheTableToken = youHaveTheTableToken;
	}

	public void setMatrixFilePath(@Nullable Path path) {
		this.matrixFilePath = Optional.ofNullable(path);
	}

	public Optional<Path> getMatrixFilePath() {
		return this.matrixFilePath;
	}

	public void setMatrix(@Nonnull Matrix matrix) {
		this.scoreMatrix = Optional.of(matrix);
		this.armyCount = matrix.getSize();
	}

	public Optional<Matrix> getMatrix() {
		return this.scoreMatrix;
	}

	public int getArmyCount() {
		return scoreMatrix.map(Matrix::getSize).orElse(armyCount);
	}

	public void forceArmyCountConsistency(Score defaultScore) {
		if (scoreMatrix.isPresent()) {
			Matrix currentMatrix = scoreMatrix.get();
			if (currentMatrix.getSize() != armyCount) {
				Matrix newMatrix = currentMatrix.withSize(armyCount, defaultScore);
				scoreMatrix = Optional.of(newMatrix);
			}
		}
	}

	public void setArmyCount(int armyCount) {
		this.armyCount = armyCount;
	}
}
