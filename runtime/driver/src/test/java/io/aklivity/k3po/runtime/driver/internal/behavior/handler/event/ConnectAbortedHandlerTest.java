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

import static io.aklivity.k3po.runtime.lang.internal.RegionInfo.newSequential;
import static org.jboss.netty.channel.ChannelState.CONNECTED;
import static org.jboss.netty.channel.Channels.pipeline;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.channel.DownstreamChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.junit.Before;
import org.junit.Test;

import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.ChannelDecoder;

public class ConnectAbortedHandlerTest {

    private ChannelFactory channelFactory;

    @Before
    public void setUp() throws Exception {
        channelFactory = new DefaultLocalClientChannelFactory();
    }

    @Test
    public void shouldSucceedWhenConnectFails() throws Exception {

        ConnectAbortedHandler handler = new ConnectAbortedHandler();
        ChannelFuture handlerFuture = fireConnectOutcome(handler, false);

        assertTrue(handlerFuture.isDone());
        assertTrue(handlerFuture.isSuccess());
    }

    @Test
    public void shouldFailWhenConnectSucceeds() throws Exception {

        ConnectAbortedHandler handler = new ConnectAbortedHandler();
        ChannelFuture handlerFuture = fireConnectOutcome(handler, true);

        assertTrue(handlerFuture.isDone());
        assertFalse(handlerFuture.isSuccess());
    }

    @Test
    public void shouldSucceedWhenAbortedWithMatchingExtension() throws Exception {

        ChannelDecoder decoder = channel -> true;
        ConnectAbortedHandler handler = new ConnectAbortedHandler(decoder);
        ChannelFuture handlerFuture = fireConnectOutcome(handler, false);

        assertTrue(handlerFuture.isDone());
        assertTrue(handlerFuture.isSuccess());
    }

    @Test
    public void shouldFailWhenAbortedWithMismatchedExtension() throws Exception {

        ChannelDecoder decoder = channel -> false;
        ConnectAbortedHandler handler = new ConnectAbortedHandler(decoder);
        ChannelFuture handlerFuture = fireConnectOutcome(handler, false);

        assertTrue(handlerFuture.isDone());
        assertFalse(handlerFuture.isSuccess());
    }

    @Test
    public void shouldFailWhenDecoderThrows() throws Exception {

        ChannelDecoder decoder = channel -> {
            throw new Exception("decode failed");
        };
        ConnectAbortedHandler handler = new ConnectAbortedHandler(decoder);
        ChannelFuture handlerFuture = fireConnectOutcome(handler, false);

        assertTrue(handlerFuture.isDone());
        assertFalse(handlerFuture.isSuccess());
    }

    private ChannelFuture fireConnectOutcome(ConnectAbortedHandler handler, boolean connected) throws Exception {

        handler.setRegionInfo(newSequential(0, 0));

        ChannelPipeline pipeline = pipeline(new SimpleChannelDownstreamHandler() {
            @Override
            public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
                // swallow — prevent the synthetic connect event from reaching the real channel sink
            }
        }, handler);

        Channel channel = channelFactory.newChannel(pipeline);

        ChannelFuture connectFuture = new DefaultChannelFuture(channel, false);
        pipeline.sendDownstream(new DownstreamChannelStateEvent(channel, connectFuture, CONNECTED, null));

        if (connected) {
            connectFuture.setSuccess();
        }
        else {
            connectFuture.setFailure(new Exception("connect refused"));
        }

        return handler.getHandlerFuture();
    }
}
