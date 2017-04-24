/*
 *
 *  Copyright 2016 Vladimir Bukhtoyarov
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.github.rollingmetrics.histogram.accumulator;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;
import com.github.rollingmetrics.histogram.HdrBuilder;
import com.github.rollingmetrics.histogram.OverflowResolver;
import com.github.rollingmetrics.histogram.OverflowResolver;
import com.github.rollingmetrics.util.BackgroundClock;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class HistogramRecordingBenchmark {

    @State(Scope.Benchmark)
    public static class HistogramState {

        BackgroundClock backgroundClock;

        final Histogram chunkedHistogram = new HdrBuilder()
                .resetReservoirPeriodicallyByChunks(Duration.ofSeconds(3), 3)
                .buildHistogram();

        final Histogram upperLimitedChunkedHistogram = new HdrBuilder()
                .resetReservoirPeriodicallyByChunks(Duration.ofSeconds(3), 3)
                .withLowestDiscernibleValue(TimeUnit.MICROSECONDS.toNanos(1))
                .withHighestTrackableValue(TimeUnit.MINUTES.toNanos(5), OverflowResolver.REDUCE_TO_HIGHEST_TRACKABLE)
                .buildHistogram();

        final Histogram resetOnSnapshotHistogram = new HdrBuilder()
                .resetReservoirOnSnapshot()
                .withLowestDiscernibleValue(TimeUnit.MICROSECONDS.toNanos(1))
                .withHighestTrackableValue(TimeUnit.MINUTES.toNanos(5), OverflowResolver.REDUCE_TO_HIGHEST_TRACKABLE)
                .buildHistogram();

        final Histogram resetPeriodicallyHistogram = new HdrBuilder()
                .resetReservoirPeriodically(Duration.ofSeconds(300))
                .withLowestDiscernibleValue(TimeUnit.MICROSECONDS.toNanos(1))
                .withHighestTrackableValue(TimeUnit.MINUTES.toNanos(5), OverflowResolver.REDUCE_TO_HIGHEST_TRACKABLE)
                .buildHistogram();

        final Histogram uniformHistogram = new HdrBuilder()
                .neverResetReservoir()
                .withLowestDiscernibleValue(TimeUnit.MICROSECONDS.toNanos(1))
                .withHighestTrackableValue(TimeUnit.MINUTES.toNanos(5), OverflowResolver.REDUCE_TO_HIGHEST_TRACKABLE)
                .buildHistogram();

        final Histogram metricsCoreHistogram = new Histogram(new ExponentiallyDecayingReservoir());

        private Histogram chunkedHistogramWithBackgroundClock;
        private Histogram upperLimitedChunkedHistogramWithBackgroundClock;
        private Histogram resetPeriodicallyHistogramWithBackgroundClock;

        @Setup
        public void setup() {
            backgroundClock = new BackgroundClock(100);

            this.chunkedHistogramWithBackgroundClock = new HdrBuilder(backgroundClock)
                    .resetReservoirPeriodicallyByChunks(Duration.ofSeconds(3), 3)
                    .buildHistogram();

            this.upperLimitedChunkedHistogramWithBackgroundClock = new HdrBuilder(backgroundClock)
                    .resetReservoirPeriodicallyByChunks(Duration.ofSeconds(3), 3)
                    .withLowestDiscernibleValue(TimeUnit.MICROSECONDS.toNanos(1))
                    .withHighestTrackableValue(TimeUnit.MINUTES.toNanos(5), OverflowResolver.REDUCE_TO_HIGHEST_TRACKABLE)
                    .buildHistogram();

            this.resetPeriodicallyHistogramWithBackgroundClock = new HdrBuilder(backgroundClock)
                    .resetReservoirPeriodically(Duration.ofSeconds(300))
                    .withLowestDiscernibleValue(TimeUnit.MICROSECONDS.toNanos(1))
                    .withHighestTrackableValue(TimeUnit.MINUTES.toNanos(5), OverflowResolver.REDUCE_TO_HIGHEST_TRACKABLE)
                    .buildHistogram();
        }

        @TearDown
        public void tearDown() {
            backgroundClock.stop();
        }

    }

    @Benchmark
    public long baseLine() {
        return getRandomValue();
    }

    @Benchmark
    public long baseLineGetCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Benchmark
    public void updateMetricsCoreHistogram(HistogramState state) {
        state.metricsCoreHistogram.update(getRandomValue());
    }

    @Benchmark
    public void updateUniformHistogram(HistogramState state) {
        state.uniformHistogram.update(getRandomValue());
    }

    @Benchmark
    public void updateResetPeriodicallyHistogramWithBackgroundClock(HistogramState state) {
        state.resetPeriodicallyHistogramWithBackgroundClock.update(getRandomValue());
    }

    @Benchmark
    public void updateChunkedHistogramWithBackgroundClock(HistogramState state) {
        state.chunkedHistogramWithBackgroundClock.update(getRandomValue());
    }

    @Benchmark
    public void updateChunkedUpperLimitedHistogramWithBackgroundClock(HistogramState state) {
        state.upperLimitedChunkedHistogramWithBackgroundClock.update(getRandomValue());
    }

    @Benchmark
    public void updateResetPeriodicallyHistogram(HistogramState state) {
        state.resetPeriodicallyHistogram.update(getRandomValue());
    }

    @Benchmark
    public void updateResetOnSnapshotHistogram(HistogramState state) {
        state.resetOnSnapshotHistogram.update(getRandomValue());
    }

    @Benchmark
    public void updateChunkedHistogram(HistogramState state) {
        state.chunkedHistogram.update(getRandomValue());
    }

    @Benchmark
    public void updateChunkedUpperLimitedHistogram(HistogramState state) {
        state.upperLimitedChunkedHistogram.update(getRandomValue());
    }

    private static long getRandomValue() {
        return ThreadLocalRandom.current().nextLong(15_000_000) + 5_000_000;
    }

    public static class OneThread {
        public static void main(String[] args) throws RunnerException {
            Options opt = new OptionsBuilder()
                    .include(((Class) HistogramRecordingBenchmark.class).getSimpleName())
                    .warmupIterations(5)
                    .measurementIterations(5)
                    .threads(1)
                    .forks(1)
                    .build();
            try {
                new Runner(opt).run();
            } catch (RunnerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class FourThread {
        public static void main(String[] args) throws RunnerException {
            Options opt = new OptionsBuilder()
                    .include(((Class) HistogramRecordingBenchmark.class).getSimpleName())
                    .warmupIterations(5)
                    .measurementIterations(5)
                    .threads(4)
                    .forks(1)
                    .build();
            try {
                new Runner(opt).run();
            } catch (RunnerException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
