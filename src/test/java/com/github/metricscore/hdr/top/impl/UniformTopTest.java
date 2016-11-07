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

package com.github.metricscore.hdr.top.impl;

import com.github.metricscore.hdr.top.Top;
import org.junit.Test;

import java.time.Duration;

import static com.github.metricscore.hdr.top.TestData.*;
import static com.github.metricscore.hdr.top.impl.TopTestUtil.*;


public class UniformTopTest {

    @Test
    public void testCommonAspects() {
        for (int i = 1; i <= 2; i++) {
            Top top = Top.builder(i)
                    .neverResetPostions()
                    .withSnapshotCachingDuration(Duration.ZERO)
                    .withSlowQueryThreshold(Duration.ofMillis(100))
                    .withMaxLengthOfQueryDescription(1000)
                    .build();
            testCommonScenarios(i, top, Duration.ofMillis(100).toNanos(), 1000);
        }
    }

    @Test
    public void test_size_1() throws Exception {
        Top top = Top.builder(1)
                .neverResetPostions()
                .withSnapshotCachingDuration(Duration.ZERO)
                .build();

        assertEmpty(top);

        update(top, first);
        checkOrder(top, first);

        update(top, second);
        checkOrder(top, second);

        update(top, first);
        checkOrder(top, second);
    }

    @Test
    public void test_size_3() throws Exception {
        Top top = Top.builder(3)
                .neverResetPostions()
                .withSnapshotCachingDuration(Duration.ZERO)
                .build();

        assertEmpty(top);

        update(top, first);
        checkOrder(top, first);

        update(top, second);
        checkOrder(top, second, first);

        update(top, third);
        checkOrder(top, third, second, first);

        update(top, fourth);
        checkOrder(top, fourth, third, second);

        update(top, fifth);
        checkOrder(top, fifth, fourth, third);

        update(top, first);
        checkOrder(top, fifth, fourth, third);

        update(top, fifth);
        checkOrder(top, fifth, fourth, third);
    }

    @Test(timeout = 32000)
    public void testThatConcurrentThreadsNotHung() throws InterruptedException {
        Top top = Top.builder(1)
                .neverResetPostions()
                .withSnapshotCachingDuration(Duration.ZERO)
                .build();
        TopTestUtil.runInParallel(top, Duration.ofSeconds(30), 0, 10_000);
    }

}