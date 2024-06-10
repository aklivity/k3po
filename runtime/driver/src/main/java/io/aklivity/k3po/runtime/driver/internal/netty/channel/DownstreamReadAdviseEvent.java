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

import static java.util.Objects.requireNonNull;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

final class DownstreamReadAdviseEvent implements ReadAdviseEvent {

    private final Channel channel;
    private final ChannelFuture future;
    private final Object value;

    DownstreamReadAdviseEvent(
            Channel channel,
            ChannelFuture future,
            Object value) {
        requireNonNull(channel);
        requireNonNull(future);
        this.channel = channel;
        this.future = future;
        this.value = value;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public ChannelFuture getFuture() {
        return future;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        String channelString = getChannel().toString();
        StringBuilder buf = new StringBuilder(channelString.length() + 64);
        buf.append(channelString);
        buf.append(" READ_ADVISE_REQUEST");
        if (value != null) {
            buf.append(" ");
            buf.append(value);
        }
        return buf.toString();
    }

}
