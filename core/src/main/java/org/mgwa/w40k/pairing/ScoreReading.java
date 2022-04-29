package org.mgwa.w40k.pairing;

import org.mgwa.w40k.pairing.matrix.Score;

public enum ScoreReading {

    CONFIDENT {
        @Override
        int readScore(Score score) {
            return score.getMaxValue();
        }
    },

    MITIGATED {
        @Override
        int readScore(Score score) {
            return (score.getMaxValue() + score.getMinValue()) / 2;
        }
    },

    PESSIMISTIC {
        @Override
        int readScore(Score score) {
            return score.getMinValue();
        }
    }

    ;

    abstract int readScore(Score score);
}
