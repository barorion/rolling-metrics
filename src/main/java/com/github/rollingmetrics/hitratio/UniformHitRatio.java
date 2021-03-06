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

package com.github.rollingmetrics.hitratio;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The hit-ratio which never evicts collected values.
 *
 * <p>
 * Concurrency properties:
 * <ul>
 *     <li>Writing is lock-free. Writers do not block writers and readers.</li>
 *     <li>Reading is lock-free. Readers do not block writers and readers.</li>
 * </ul>
 *
 * <p>
 * Usage recommendations:
 * <ul>
 *     <li>When you do not need in "rolling time window" semantic. Else use {@link SmoothlyDecayingRollingHitRatio}</li>
 *     <li>Normally you should not use this implementation because in real world use-cases you need to show measurements which actual to current moment of time or time window.</li>
 * </ul>
 *
 * @see SmoothlyDecayingRollingHitRatio
 * @see ResetPeriodicallyHitRatio
 * @see ResetOnSnapshotHitRatio
 */
public class UniformHitRatio implements HitRatio {

    private final AtomicLong ratio = new AtomicLong();

    @Override
    public void update(int hitCount, int totalCount) {
        HitRatioUtil.updateRatio(ratio, hitCount, totalCount);
    }

    @Override
    public double getHitRatio() {
        return HitRatioUtil.getRatio(ratio.get());
    }

}
