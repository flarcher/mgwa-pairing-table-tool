package org.mgwa.w40k.pairing.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Match {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Match(
            @JsonProperty("row_team") String rowTeamName,
            @JsonProperty("column_team") String columnTeamName,
            @JsonProperty("team_member_count") int teamMemberCount) {
        this.rowTeamName = rowTeamName;
        this.columnTeamName = columnTeamName;
        this.teamMemberCount = teamMemberCount;
    }

    @JsonProperty("row_team")
    private final String rowTeamName;
    @JsonProperty("column_team")
    private final String columnTeamName;
    @JsonProperty("team_member_count")
    private final int teamMemberCount;

    public String getRowTeamName() {
        return rowTeamName;
    }

    public String getColumnTeamName() {
        return columnTeamName;
    }

    public int getTeamMemberCount() {
        return teamMemberCount;
    }
}
