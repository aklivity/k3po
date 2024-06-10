/*
 * Copyright 2024 Aklivity Inc.
 *
 * Aklivity licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.aklivity.k3po.runtime.driver.internal.behavior.handler.event;

import static io.aklivity.k3po.runtime.driver.internal.behavior.handler.event.AbstractEventHandler.ChannelEventKind.BOUND;
import static io.aklivity.k3po.runtime.driver.internal.behavior.handler.event.AbstractEventHandler.ChannelEventKind.CHILD_CLOSED;
import static io.aklivity.k3po.runtime.driver.internal.behavior.handler.event.AbstractEventHandler.ChannelEventKind.CHILD_OPEN;
import static io.aklivity.k3po.runtime.driver.internal.behavior.handler.event.AbstractEventHandler.ChannelEventKind.CLOSED;
import static io.aklivity.k3po.runtime.driver.internal.behavior.handler.event.AbstractEventHandler.ChannelEventKind.OPEN;
import static io.aklivity.k3po.runtime.driver.internal.behavior.handler.event.AbstractEventHandler.ChannelEventKind.UNBOUND;
import static java.util.EnumSet.of;

import java.util.Set;

public abstract class AbstractServerEventHandler extends AbstractEventHandler {

    public AbstractServerEventHandler(Set<ChannelEventKind> expectedEvents) {
        super(of(OPEN, BOUND, CHILD_OPEN, CHILD_CLOSED, UNBOUND, CLOSED), expectedEvents);
    }

    public AbstractServerEventHandler(Set<ChannelEventKind> interestingEvents, Set<ChannelEventKind> expectedEvents) {
        super(interestingEvents, expectedEvents);
    }
}
