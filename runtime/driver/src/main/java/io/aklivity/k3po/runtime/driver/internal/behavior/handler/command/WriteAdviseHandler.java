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
package io.aklivity.k3po.runtime.driver.internal.behavior.handler.command;

import static io.aklivity.k3po.runtime.driver.internal.netty.channel.Channels.adviseOutput;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.ChannelEncoder;

public class WriteAdviseHandler extends AbstractCommandHandler {

    private final Object value;
    private final List<ChannelEncoder> encoders;

    public WriteAdviseHandler(Object value, ChannelEncoder encoder) {
        this(value, singletonList(encoder));
    }

    public WriteAdviseHandler(Object value, List<ChannelEncoder> encoders) {
        requireNonNull(encoders, "encoders");
        if (encoders.size() == 0) {
            throw new IllegalArgumentException("must have at least one encoder");
        }
        this.value = value;
        this.encoders = encoders;
    }

    @Override
    protected void invokeCommand(ChannelHandlerContext ctx) throws Exception {
        try {
            Channel channel = ctx.getChannel();
            for (ChannelEncoder encoder : encoders) {
                encoder.encode(channel);
            }
            adviseOutput(ctx, getHandlerFuture(), value);
        }
        catch (Exception e) {
            getHandlerFuture().setFailure(e);
        }
    }

    @Override
    protected StringBuilder describe(StringBuilder sb) {
        return sb.append(format("write advise %s %s", value, encoders));
    }

}
