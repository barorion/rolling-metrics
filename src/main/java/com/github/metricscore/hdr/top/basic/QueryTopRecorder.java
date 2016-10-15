/*
 *    Copyright 2016 Vladimir Bukhtoyarov
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.github.metricscore.hdr.top.basic;
import org.HdrHistogram.WriterReaderPhaser;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Is not a part of public API, this class just used as building block for different QueryTop implementations.
 */
public class QueryTopRecorder {

    private final WriterReaderPhaser recordingPhaser = new WriterReaderPhaser();

    private volatile ComposableQueryTop active;
    private ComposableQueryTop inactive;

    public QueryTopRecorder(int size, Duration slowQueryThreshold) {
        active = new ConcurrentQueryTop(size, slowQueryThreshold);
        inactive = new ConcurrentQueryTop(size, slowQueryThreshold);
    }

    public void update(long latencyTime, TimeUnit latencyUnit, Supplier<String> descriptionSupplier) {
        long criticalValueAtEnter = recordingPhaser.writerCriticalSectionEnter();
        try {
            active.update(latencyTime, latencyUnit, descriptionSupplier);
        } finally {
            recordingPhaser.writerCriticalSectionExit(criticalValueAtEnter);
        }
    }

    public synchronized ComposableQueryTop getIntervalQueryTop() {
        return getIntervalQueryTop(null);
    }

    public synchronized ComposableQueryTop getIntervalQueryTop(ComposableQueryTop queryTopToRecycle) {
        inactive = queryTopToRecycle;
        performIntervalSample();
        ComposableQueryTop sampledQueryTop = inactive;
        inactive = null; // Once we expose the sample, we can't reuse it internally until it is recycled
        return sampledQueryTop;
    }

    public synchronized void reset() {
        // the currently inactive query-top is reset each time we flip. So flipping twice resets both:
        performIntervalSample();
        performIntervalSample();
    }

    private void performIntervalSample() {
        try {
            recordingPhaser.readerLock();

            // Make sure we have an inactive version to flip in:
            if (inactive == null) {
                inactive = ComposableQueryTop.create(active.getSize(), Duration.ofNanos(active.getSlowQueryThresholdNanos()));
            } else {
                inactive.reset();
            }

            // Swap active and inactive top:
            final ComposableQueryTop temp = inactive;
            inactive = active;
            active = temp;

            recordingPhaser.flipPhase();
        } finally {
            recordingPhaser.readerUnlock();
        }
    }

}