/*
 *
 *  Copyright 2017 Vladimir Bukhtoyarov
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

package com.github.rollingmetrics.top.impl.collector;

import com.github.rollingmetrics.top.TopTestData;
import org.junit.Test;


import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;


public class MultiPositionCollectorTest {

    private PositionCollector collector = new MultiPositionCollector(2);
    private PositionCollector collector2 = new MultiPositionCollector(2);

    @Test
    public void test() {
        PositionCollectorTestUtil.assertEmpty(collector);

        assertTrue(collector.add(TopTestData.first));
        assertTrue(collector.add(TopTestData.first));
        PositionCollectorTestUtil.checkOrder(collector, TopTestData.first);

        assertTrue(collector.add(TopTestData.second));
        assertFalse(collector.add(TopTestData.second));
        PositionCollectorTestUtil.checkOrder(collector, TopTestData.second, TopTestData.first);

        assertTrue(collector.add(TopTestData.third));
        assertFalse(collector.add(TopTestData.third));
        PositionCollectorTestUtil.checkOrder(collector, TopTestData.third, TopTestData.second);

        assertFalse(collector.add(TopTestData.first));
        PositionCollectorTestUtil.checkOrder(collector, TopTestData.third, TopTestData.second);
    }

    @Test
    public void testAddInto() {
        collector.addInto(collector2);
        PositionCollectorTestUtil.assertEmpty(collector2);

        collector.add(TopTestData.first);
        collector.addInto(collector2);
        PositionCollectorTestUtil.checkOrder(collector2, TopTestData.first);

        collector.add(TopTestData.second);
        collector.addInto(collector2);
        PositionCollectorTestUtil.checkOrder(collector2, TopTestData.second, TopTestData.first);

        collector.add(TopTestData.third);
        collector.addInto(collector2);
        PositionCollectorTestUtil.checkOrder(collector2, TopTestData.third, TopTestData.second);
    }

    @Test
    public void testReset() {
        collector.add(TopTestData.first);

        collector.reset();
        PositionCollectorTestUtil.assertEmpty(collector);
    }

}