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
package io.aklivity.k3po.runtime.driver.internal.netty.channel;

import org.jboss.netty.channel.ChannelFuture;

public final class ChannelFutures {

    private ChannelFutures() {
        // no instances
    }

    public static String describeFuture(ChannelFuture future) {
        if (future == null) {
            return "null";
        }
        else if (future.isSuccess()) {
            return "success";
        }
        else if (future.isCancelled()) {
            return "cancelled";
        }
        else if (future.getCause() != null) {
            return "failed";
        }
        else {
            return "incomplete";
        }
    }
}
