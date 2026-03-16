package com.audiofp.matching;

import com.audiofp.audio.TripletAnalyzer.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Computes similarity between two sets of spectral triplets.
 *
 * The original Correlation.java had several defects that are fixed here:
 *
 *  1. Outer loop bug — the original loop stepped by 2 over a 2-row array,
 *     so only a single pass ever ran. This rewrite processes triplets correctly.
 *
 *  2. Undefined correlation — Pearson correlation with n = 3 identical x-values
 *     gives a zero denominator. This is guarded with a NaN check.
 *
 *  3. ArithmeticException / NaN from empty corrSet — now handled explicitly.
 *
 *  4. Algorithm clarity — matching is now by nearest-frequency lookup, making
 *     the intent obvious.
 */
public final class Correlation {

    private static final Logger log = LoggerFactory.getLogger(Correlation.class);

    /** Maximum frequency distance (Hz) to consider two triplets a match. */
    private static final double FREQ_MATCH_TOLERANCE_HZ = 5.0;

    private Correlation() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Computes a [0, 1] match score between a recorded set of triplets and
     * a stored fingerprint set.
     *
     * <ol>
     *   <li>For each recorded triplet, find the stored triplet whose peak
     *       frequency is within {@value #FREQ_MATCH_TOLERANCE_HZ} Hz.</li>
     *   <li>Compute Pearson correlation between their amplitude arrays.</li>
     *   <li>Average the per-triplet correlations.</li>
     * </ol>
     *
     * @return score in [0, 1], or −1 if no matching triplets were found
     */
    public static double computeMatchScore(List<Triplet> recorded, List<Triplet> stored) {
        if (recorded == null || recorded.isEmpty()) return -1;
        if (stored    == null || stored.isEmpty())   return -1;

        double totalCorr = 0;
        int    matches   = 0;

        for (Triplet rec : recorded) {
            Triplet best = findNearestByPeakFreq(rec, stored);
            if (best == null) continue;

            double corr = pearsonCorrelation(rec.amplitudeArray(), best.amplitudeArray());
            if (!Double.isNaN(corr) && corr > 0) {
                totalCorr += corr;
                matches++;
            }
        }

        if (matches == 0) return -1;

        double score = totalCorr / matches;
        log.debug("Match score {:.4f} from {} / {} triplet pairs",
                score, matches, recorded.size());
        return score;
    }

    /**
     * Pearson correlation coefficient r ∈ [−1, 1].
     *
     * Returns {@link Double#NaN} if either array has zero variance (constant),
     * or if inputs are null / different lengths.
     */
    public static double pearsonCorrelation(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length || x.length == 0) {
            return Double.NaN;
        }
        int n = x.length;

        double sumX = 0, sumY = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
        }
        double meanX = sumX / n;
        double meanY = sumY / n;

        double cov = 0, varX = 0, varY = 0;
        for (int i = 0; i < n; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            cov  += dx * dy;
            varX += dx * dx;
            varY += dy * dy;
        }

        double denom = Math.sqrt(varX * varY);
        if (denom == 0) return Double.NaN;
        return cov / denom;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static Triplet findNearestByPeakFreq(Triplet target, List<Triplet> candidates) {
        Triplet best     = null;
        double  bestDist = FREQ_MATCH_TOLERANCE_HZ;

        for (Triplet c : candidates) {
            double dist = Math.abs(c.peakFreq() - target.peakFreq());
            if (dist < bestDist) {
                bestDist = dist;
                best     = c;
            }
        }
        return best;
    }
}
