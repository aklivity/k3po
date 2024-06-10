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
package io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.udp;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.socket.nio.NioDatagramChannel;

import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.channel.AbstractChannelSink;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddress;

import java.net.SocketAddress;

import static io.aklivity.k3po.runtime.driver.internal.channel.Channels.toInetSocketAddress;
import static org.jboss.netty.channel.Channels.fireChannelClosed;
import static org.jboss.netty.channel.Channels.fireChannelDisconnected;
import static org.jboss.netty.channel.Channels.fireChannelUnbound;

class UdpChildChannelSink extends AbstractChannelSink {

    private final NioDatagramChannel serverChannel;
    private final UdpChildChannelSource childChannelSource;

    UdpChildChannelSink(UdpChildChannelSource childChannelSource) {
        this.childChannelSource = childChannelSource;
        this.serverChannel = childChannelSource.serverChannel.getTransport();
    }

    protected void writeRequested(ChannelPipeline pipeline, MessageEvent e) throws Exception {
        assert e.getChannel() instanceof UdpChildChannel;
        assert e.getRemoteAddress() != null;

        SocketAddress toAddress = toInetSocketAddress((ChannelAddress) e.getChannel().getRemoteAddress());

        serverChannel.write(e.getMessage(), toAddress);
        e.getFuture().setSuccess();
    }

    @Override
    protected void closeRequested(ChannelPipeline pipeline, ChannelStateEvent evt) throws Exception {
        final UdpChildChannel channel = (UdpChildChannel) evt.getChannel();

        if (channel.isConnected()) {
            childChannelSource.closeChildChannel(channel);
        }

        if (channel.setClosed())
        {
            fireChannelDisconnected(channel);
            fireChannelUnbound(channel);
            fireChannelClosed(channel);
        }

        evt.getFuture().setSuccess();
    }

}
