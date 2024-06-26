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
package io.aklivity.k3po.runtime.driver.internal.behavior.handler.barrier;

import io.aklivity.k3po.runtime.driver.internal.behavior.Barrier;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.ExecutionHandler;

public abstract class AbstractBarrierHandler extends ExecutionHandler {

    protected final Barrier barrier;

    public AbstractBarrierHandler(Barrier barrier) {
        if (barrier == null) {
            throw new NullPointerException("barrier");
        }
        this.barrier = barrier;
    }

    public Barrier getBarrier() {
        return barrier;
    }

}
