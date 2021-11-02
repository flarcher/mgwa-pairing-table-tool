package org.mgwa.w40k.pairing.gui;

import org.mgwa.w40k.pairing.matrix.Matrix;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * A mutable class that describes the application state.
 */
public enum AppState {

	INSTANCE; // Classic enum-based singleton pattern

	private String rowTeamName = "";
	private String colTeamName = "";
	private boolean youHaveTheTableToken = true;
	private Optional<Path> matrixFilePath = Optional.empty();
	private Optional<Matrix> scoreMatrix = Optional.empty();

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

	public void setMatrix(@Nonnull  Matrix matrix) {
		this.scoreMatrix = Optional.of(matrix);
	}

	public Optional<Matrix> getMatrix() {
		return this.scoreMatrix;
	}
}
