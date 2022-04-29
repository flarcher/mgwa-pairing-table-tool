package org.mgwa.w40k.pairing;

import org.mgwa.w40k.pairing.matrix.Matrix;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Contains the algorithm that suggest the best pairing.
 */
public final class PairingGuidance {

    private PairingGuidance() {} // Utility class

    /**
     * @param assignment Tables assignment.
     * @return Possibles pairs to be done based on unassigned tables.
     */
    public static Collection<Pair> possiblePairs(Assignment assignment) {
        int[] assignedTables = IntStream.range(0, assignment.getTableCount())
            .filter(assignment::isAssigned)
            .toArray();
        Set<Integer> assignedRows = new HashSet<>(assignedTables.length);
        Set<Integer> assignedCols = new HashSet<>(assignedTables.length);
        IntStream.of(assignedTables).forEach(i -> {
                assignedRows.add(assignment.getRowArmyIndex(i));
                assignedCols.add(assignment.getColArmyIndex(i));
            });
        List<Integer> unassignedRows = new ArrayList<>(assignment.getTableCount() - assignedTables.length);
        List<Integer> unassignedCols = new ArrayList<>(assignment.getTableCount() - assignedTables.length);
        for (int i = 0; i < assignment.getTableCount() /* == Armies count */; i++) {
            if (!assignedRows.contains(i)) {
                unassignedRows.add(i);
            }
            if (!assignedCols.contains(i)) {
                unassignedCols.add(i);
            }
        }
        return Pair.possiblePairs(unassignedRows, unassignedCols);
    }

    public static class ScoredPair implements Comparable<ScoredPair> {

        public ScoredPair(@Nonnull Pair pair) {
            this.pair = Objects.requireNonNull(pair);
            this.score = 0;
        }

        private final Pair pair;
        private int score;

        public Pair getPair() {
            return pair;
        }

        public int getScore() {
            return score;
        }

        private void setScore(int score) {
            this.score = score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScoredPair that = (ScoredPair) o;
            return pair.equals(that.pair) && score == that.score;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pair, score);
        }

        @Override
        public int compareTo(ScoredPair o) {
            return SCORED_PAIR_COMPARATOR.compare(this, o);
        }
    }

    private static final Comparator<ScoredPair> SCORED_PAIR_COMPARATOR = Comparator.<ScoredPair>
            comparingInt(sp -> sp.pair.getRow())
            .thenComparingInt(sp -> sp.pair.getColumn())
            .thenComparingInt(sp -> sp.score);

    /**
     * @param matrix Score mapping/matrix.
     * @param scoreReading Policy about the reading of the score.
     * @param assignment Current tables assignment.
     * @param nextPairFilter Filters for next pairs.
     * @param method How the future paths will be considered.
     * @return A sorted suggestion for the next pair to be considered.
     */
    public static SortedSet<ScoredPair> suggestPairing(
            Matrix matrix,
            ScoreReading scoreReading,
            Assignment assignment,
            Predicate<Pair> nextPairFilter,
            ForecastMethod method) {

        return suggestPairing(matrix, scoreReading, possiblePairs(assignment), nextPairFilter, method);
    }

    /**
     * @param matrix Score mapping/matrix.
     * @param scoreReading Policy about the reading of the score.
     * @param possiblePairs All possible pairs to sort.
     * @param nextPairFilter Filters for next possible pairs.
     * @param method How the future paths will be considered.
     * @return A sorted suggestion for the next pair to be considered.
     */
    public static SortedSet<ScoredPair> suggestPairing(
            Matrix matrix,
            ScoreReading scoreReading,
            Collection<Pair> possiblePairs,
            Predicate<Pair> nextPairFilter,
            ForecastMethod method) {

        // TODO : to be implemented

        return Collections.emptySortedSet();
    }

}
