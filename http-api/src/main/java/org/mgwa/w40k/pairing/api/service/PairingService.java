package org.mgwa.w40k.pairing.api.service;

import org.mgwa.w40k.pairing.*;
import org.mgwa.w40k.pairing.api.model.ArmyReference;
import org.mgwa.w40k.pairing.api.model.AssignedPair;
import org.mgwa.w40k.pairing.api.model.PairingRequest;
import org.mgwa.w40k.pairing.api.model.PairingResponseItem;
import org.mgwa.w40k.pairing.matrix.Matrix;
import org.mgwa.w40k.pairing.state.AppState;
import org.mgwa.w40k.pairing.util.LoggerSupplier;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * HTTP API service.
 */
public class PairingService {

    private static final Logger LOGGER = LoggerSupplier.INSTANCE.getLogger();
    public PairingService(AppState state) {
        this.state = state;
    }

    private final AppState state;

    private static Optional<Army> getArmy(Matrix matrix, ArmyReference ref) {
        if (!ref.isValid()) {
            throw ServiceUtils.badRequest("Invalid army reference");
        }
        else if (ref.getIndex().isPresent()) {
            List<Army> armies = matrix.getArmies(ref.isRow().orElseThrow(() -> ServiceUtils.badRequest("Missing row information in army reference")));
            int listIndex = ref.getIndex().get();
            if (listIndex >= armies.size()) {
                throw ServiceUtils.badRequest("Army reference index too big");
            }
            return Optional.ofNullable(armies.get(listIndex));
        }
        else if (ref.getName().isPresent()) {
            List<Boolean> armyListTypes;
            if (ref.isRow().isPresent()) {
                armyListTypes = List.of(ref.isRow().orElse(Boolean.TRUE));
            }
            else {
                armyListTypes = Arrays.asList( Boolean.TRUE, Boolean.FALSE );
            }
            String armyName = ref.getName().get();
            return armyListTypes.stream()
                    .map(matrix::getArmies)
                    .flatMap(List::stream)
                    .filter(army -> army.getName().equals(armyName))
                    .findAny();
        }
        else {
            throw ServiceUtils.badRequest("Invalid army reference: no way to find army");
        }
    }

    private static Predicate<Pair> nextAssignmentPredicate(Matrix matrix, ArmyReference ref) {
        Optional<Army> optionalArmy = getArmy(matrix, ref);
        if (optionalArmy.isEmpty()) {
            throw ServiceUtils.badRequest(String.format("Army %s not found", ref));
        }
        return Pair.isWithArmy(optionalArmy.get());
    }

    private static Assignment getAssignment(Matrix matrix, Collection<AssignedPair> pairs) {
        Assignment assignment = Assignment.createEmpty(matrix.getSize());
        SortedSet<Integer> tableIndexes = new TreeSet<>(Comparator.reverseOrder());
        Function<ArmyReference, RuntimeException> onMissingArmy = ref -> ServiceUtils.badRequest(String.format("Army %s is not found", ref));
        pairs.forEach(pair -> {
            pair.getTableIndex().ifPresent(tableIndexes::add);
            int tableIndex = pair.getTableIndex().orElseGet(() -> {
                Optional<Integer> max = tableIndexes.stream().max(Comparator.naturalOrder());
                int newMax = max.map(m -> m+1).orElse(0);
                tableIndexes.add(newMax);
                return newMax;
            });
            Army leftArmy = getArmy(matrix, pair.getLeftArmy())
                    .orElseThrow(() -> onMissingArmy.apply(pair.getLeftArmy()));
            Army rightArmy = getArmy(matrix, pair.getRightArmy())
                    .orElseThrow(() -> onMissingArmy.apply(pair.getRightArmy()));
            Army rowArmy = leftArmy.isRow() ? leftArmy : rightArmy;
            Army columnArmy = rightArmy.isRow() ? leftArmy : rightArmy;
            if (!rowArmy.isRow() || columnArmy.isRow()) {
                throw ServiceUtils.badRequest("Missing row or column army in an assigned pair");
            }
            assignment.assign(tableIndex, rowArmy, columnArmy);
        });
        return assignment;
    }

    private static PairingGuidance getGuidance(Matrix matrix, PairingRequest request) {
        Predicate<Pair> nextAssignmentPredicate = nextAssignmentPredicate(matrix, request.getNextArmy());
        return new PairingGuidance(matrix, LOGGER::fine)
                .setForecastMethod(request.getMethod())
                .setScoreReading(request.getReading())
                .setNextPairFilter(nextAssignmentPredicate)
                .setFilterRedundantPath(true);
    }

    private static PairingResponseItem convert(ScoredPair pair) {
        ArmyReference rowRef = new ArmyReference().setIsRow(true).setIndex(pair.getPair().getRow());
        ArmyReference columnRef = new ArmyReference().setIsRow(false).setIndex(pair.getPair().getColumn());
        return new PairingResponseItem(pair.getScore(), rowRef, columnRef);
    }

    public List<PairingResponseItem> estimatePairing(PairingRequest request) {
        Matrix matrix = getMatrix();
        PairingGuidance guidance = getGuidance(matrix, request);
        Assignment assignment = getAssignment(matrix, request.getAssignedPairs());
        SortedSet<ScoredPair> result = guidance.suggestPairing(assignment);
        return result.stream()
            .map(PairingService::convert)
            .collect(Collectors.toList());
    }

    public Matrix getMatrix() {
        return state.getMatrix()
            .orElseThrow(() -> ServiceUtils.internalError("No matrix defined yet"));
    }
}
