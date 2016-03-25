package com.github.metricscore.hdrhistogram;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.fail;


public class HdrBuilderArgumentCheckingTest {

    @Test(expected = NullPointerException.class)
    public void shouldPreventToSetNullAccumulationStrategy() {
        new HdrBuilder().withAccumulationStrategy(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNegativeSignificantDigits() {
        new HdrBuilder().withSignificantDigits(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowTooBigSignificantDigits() {
        new HdrBuilder().withSignificantDigits(6);
    }

    @Test
    public void shouldAllowSignificantDigitsBetweenZeroAndFive() {
        for (int digits = 0; digits < 6; digits++) {
            new HdrBuilder().withSignificantDigits(digits);
        }
    }

    @Test
    public void shouldNotAllowTooSmallSignificantDigitsLowestDiscernibleValue() {
        for (int value : new int[] {0, -1}) {
            try {
                new HdrBuilder().withLowestDiscernibleValue(value);
                fail();
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }

    @Test
    public void shouldNotAllowTooSmallHighestTrackableValue() {
        for (int value : new int[] {0, -1, 1}) {
            try {
                new HdrBuilder().withHighestTrackableValue(value, OverflowResolving.PASS_THRU);
                fail();
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldCheckThatHighestValueShouldBeTwoTimesGeaterThenLowest() {
        new HdrBuilder().withLowestDiscernibleValue(10).withHighestTrackableValue(11, OverflowResolving.PASS_THRU).buildReservoir();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldRequireHighestValueIfLowestSpecified() {
        new HdrBuilder().withLowestDiscernibleValue(10).buildReservoir();
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullOverflowHandlingStrategy() {
        new HdrBuilder().withHighestTrackableValue(42, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNegativeCachingDuration() {
        new HdrBuilder().withSnapshotCachingDuration(Duration.ofMillis(-1000));
    }

    @Test
    public void shouldAllowZeroCachingDuration() {
        new HdrBuilder().withSnapshotCachingDuration(Duration.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNegativePercentiles() {
        new HdrBuilder().withPredefinedPercentiles(new double[] {0.1, -0.2, 0.4});
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowTooBigPercentiles() {
        new HdrBuilder().withPredefinedPercentiles(new double[] {0.1, 0.2, 1.1});
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullPercentiles() {
        new HdrBuilder().withPredefinedPercentiles(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowEmptyPercentiles() {
        new HdrBuilder().withPredefinedPercentiles(new double[0]);
    }

    @Test
    public void shouldSuccessfullyBuild() {
        new HdrBuilder().withLowestDiscernibleValue(3).withLowestDiscernibleValue(1000)
                .withHighestTrackableValue(3600000l, OverflowResolving.REDUCE_TO_MAXIMUM)
                .withPredefinedPercentiles(new double[] {0.9, 0.95, 0.99})
                .withSnapshotCachingDuration(Duration.ofMinutes(1))
                .buildReservoir();
    }

}