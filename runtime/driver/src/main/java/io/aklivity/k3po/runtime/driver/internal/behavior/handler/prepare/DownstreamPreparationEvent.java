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
package io.aklivity.k3po.runtime.driver.internal.behavior.handler.prepare;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

public class DownstreamPreparationEvent extends AbstractPreparationEvent {

    public DownstreamPreparationEvent(Channel channel, ChannelFuture future) {
        super(channel, future);
    }

    @Override
    public String toString() {
        String channelString = getChannel().toString();
        StringBuilder buf = new StringBuilder(channelString.length() + 32);
        buf.append(channelString);
        buf.append(" PREPARED");
        return buf.toString();
    }

}