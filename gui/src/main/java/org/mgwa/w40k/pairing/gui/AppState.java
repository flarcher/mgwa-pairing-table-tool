package org.mgwa.w40k.pairing.gui;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A mutable class that describes the application state.
 */
public class AppState {

	private String rowTeamName;
	private String colTeamName;
	private boolean youHaveTheTableToken = true;
	private Optional<Path> matrixFilePath = Optional.empty();

	//-- getters and setters

	public String getRowTeamName() {
		return rowTeamName;
	}

	public void setRowTeamName(String rowTeamName) {
		this.rowTeamName = rowTeamName;
	}

	public String getColTeamName() {
		return colTeamName;
	}

	public void setColTeamName(String colTeamName) {
		this.colTeamName = colTeamName;
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
}
