package org.mgwa.w40k.pairing.api.resource;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.mgwa.w40k.pairing.matrix.Score;

import java.util.Objects;
import java.util.Optional;

class WebInputUtils {

    private WebInputUtils() {}

    static boolean isRow(String rowsOrColumn) {
        switch (rowsOrColumn) {
            case "rows", "row" -> {
                return true;
            }
            case "columns", "column", "col", "cols" -> {
                return false;
            }
            default ->
                    throw new WebApplicationException(
                            "Unknown kind of army " + rowsOrColumn,
                            Response.Status.BAD_REQUEST);
        }
    }

    static int expectUnsignedInteger(String value) {
        try {
            return Integer.parseUnsignedInt(value);
        } catch (NumberFormatException nfe) {
            throw new WebApplicationException("Expected an integer, got <"+value+">", Response.Status.BAD_REQUEST);
        }
    }

    static Optional<Integer> parseOptionalUnsignedInteger(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String newValue = value.trim();
        if (newValue.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(expectUnsignedInteger(newValue));
    }

    static Score considerScore(String minimum, String maximum, Score defaultScore) {
        Objects.requireNonNull(defaultScore);
        if (defaultScore.getMinValue() > defaultScore.getMaxValue()) {
            throw new IllegalArgumentException("Invalid default score " + defaultScore);
        }
        Optional<Integer> min = parseOptionalUnsignedInteger(minimum);
        Optional<Integer> max = parseOptionalUnsignedInteger(maximum);
        if (min.isPresent() && max.isPresent()) {
            if (min.get() <= max.get()) {
                return Score.of(min.get(), max.get());
            } else {
                throw new WebApplicationException(String.format("Minimum %d can not be greater than maximum %d", min.get(), max.get()), Response.Status.BAD_REQUEST);
            }
        } else if (min.isPresent()) {
            return Score.of(min.get(), Math.max(min.get(), defaultScore.getMaxValue()));
        } else if (max.isPresent()) {
            return Score.of(Math.min(defaultScore.getMinValue(), max.get()), max.get());
        } else {
            return defaultScore;
        }
    }
}
