package org.mgwa.w40k.pairing.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mgwa.w40k.pairing.Army;
import org.mgwa.w40k.pairing.matrix.Matrix;

import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Immutable
public class SetupOverview {

    public static List<List<EstimatedScore>> getScoresFromMatrix(Matrix matrix, int size, EstimatedScore defaultScore) {
        Objects.requireNonNull(matrix);
        Objects.requireNonNull(defaultScore);
        return IntStream.range(0, size)
                .mapToObj(row ->
                        IntStream.range(0, size)
                                .mapToObj(column -> matrix.getScore(row, column)
                                        .map(EstimatedScore::from)
                                        .orElse(defaultScore))
                                .toList()
                )
                .toList();
    }

    public static List<ArmyReference> getArmiesFromMatrix(Matrix matrix, boolean isRow) {
        return matrix.getArmies(isRow).stream()
                .map(ArmyReference::from)
                .collect(Collectors.toList());
    }

    public static SetupOverview from(Match match, Matrix matrix, EstimatedScore defaultScore) {
        return new SetupOverview(
                match,
                getArmiesFromMatrix(matrix, true),
                getArmiesFromMatrix(matrix, false),
                getScoresFromMatrix(matrix, match.getTeamMemberCount(), defaultScore)
        );
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SetupOverview(
            @JsonProperty(value = "match") Match match,
            @JsonProperty(value = "row_armies") List<ArmyReference> rowArmies,
            @JsonProperty(value = "col_armies") List<ArmyReference> colArmies,
            @JsonProperty(value = "scores") List<List<EstimatedScore>> scores) {
        this.match = match;
        this.rowArmies = rowArmies;
        this.colArmies = colArmies;
        this.scores = scores;
    }

    @JsonProperty(value = "match")
    private final Match match;

    @JsonProperty(value = "row_armies")
    private final List<ArmyReference> rowArmies;

    @JsonProperty(value = "col_armies")
    private final List<ArmyReference> colArmies;

    @JsonProperty(value = "scores")
    private final List<List<EstimatedScore>> scores;

    public Match getMatch() {
        return match;
    }

    public List<ArmyReference> getRowArmies() {
        return rowArmies;
    }

    public List<ArmyReference> getColArmies() {
        return colArmies;
    }

    public List<List<EstimatedScore>> getScores() {
        return scores;
    }

    private static List<Army> mapArmies(List<ArmyReference> references) {
        return references.stream().map(ArmyReference::toArmy).collect(Collectors.toList());
    }

    @JsonIgnore
    public Matrix getMatrix() {
        Matrix matrix = Matrix.createWithoutScores(mapArmies(rowArmies), mapArmies(colArmies));
        IntStream.range(0, rowArmies.size()).forEach(row -> {
            IntStream.range(0, colArmies.size()).forEach(col -> {
                matrix.setScore(row, col, scores.get(row).get(col).toScore());
            });
        });
        return matrix;
    }
}
