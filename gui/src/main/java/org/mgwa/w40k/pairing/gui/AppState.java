package org.mgwa.w40k.pairing.gui;

/**
 * A mutable class that describes the application state.
 */
public enum AppState {

	INSTANCE;

	private String rowTeamName;
	private String colTeamName;
	private boolean youHaveTheTableToken = true;

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
}
