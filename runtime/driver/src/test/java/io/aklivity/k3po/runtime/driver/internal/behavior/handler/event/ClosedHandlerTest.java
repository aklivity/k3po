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
import static java.lang.Boolean.FALSE;
import static java.lang.System.currentTimeMillis;
import static org.jboss.netty.channel.ChannelState.INTEREST_OPS;
import static org.jboss.netty.channel.ChannelState.OPEN;
import static org.jboss.netty.channel.Channels.fireChannelBound;
import static org.jboss.netty.channel.Channels.fireChannelClosed;
import static org.jboss.netty.channel.Channels.fireChannelConnected;
import static org.jboss.netty.channel.Channels.fireChannelDisconnected;
import static org.jboss.netty.channel.Channels.fireChannelInterestChanged;
import static org.jboss.netty.channel.Channels.fireChannelUnbound;
import static org.jboss.netty.channel.Channels.fireExceptionCaught;
import static org.jboss.netty.channel.Channels.fireMessageReceived;
import static org.jboss.netty.channel.Channels.fireWriteComplete;
import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.channel.Channels.succeededFuture;
import static org.jboss.netty.handler.timeout.IdleState.ALL_IDLE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.DefaultChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.jboss.netty.handler.timeout.DefaultIdleStateEvent;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.junit.Before;
import org.junit.Test;

import io.aklivity.k3po.runtime.driver.internal.behavior.handler.TestChannelEvent;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.event.ClosedHandler;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.prepare.PreparationEvent;
import io.aklivity.k3po.runtime.driver.internal.jmock.Expectations;
import io.aklivity.k3po.runtime.driver.internal.jmock.Mockery;

public class ClosedHandlerTest {

    private Mockery context;
    private ChannelUpstreamHandler upstream;
    private ChannelPipeline pipeline;
    private ChannelFactory channelFactory;
    private ClosedHandler handler;
    private boolean blockChannelOpen = true;

    @Before
    public void setUp() throws Exception {
        context = new Mockery() {
            {
                setThrowFirstErrorOnAssertIsSatisfied(true);
            }
        };

        upstream = context.mock(ChannelUpstreamHandler.class);

        handler = new ClosedHandler();
        handler.setRegionInfo(newSequential(0, 0));

        pipeline = pipeline(new SimpleChannelHandler() {
            @Override
            public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                // block implicit channel open?
                if (!blockChannelOpen) {
                    ctx.sendUpstream(e);
                }
            }
        }, handler, new SimpleChannelHandler() {
            @Override
            public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
                upstream.handleUpstream(ctx, e);
                super.handleUpstream(ctx, e);
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
                // prevent console error message
            }
        });

        channelFactory = new DefaultLocalClientChannelFactory();
    }

    @Test
    public void shouldPropagateUpstreamChildOpenedEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(ChildChannelStateEvent.class)));
            }
        });

        Channel parentChannel = channelFactory.newChannel(pipeline);
        Channel childChannel = channelFactory.newChannel(pipeline(new SimpleChannelUpstreamHandler()));
        // child channel is open
        pipeline.sendUpstream(new DefaultChildChannelStateEvent(parentChannel, childChannel));

        ChannelFuture handlerFuture = handler.getHandlerFuture();
        assertFalse(handlerFuture.isDone());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldPropagateUpstreamChildClosedEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(ChildChannelStateEvent.class)));
            }
        });

        Channel parentChannel = channelFactory.newChannel(pipeline);
        Channel childChannel = channelFactory.newChannel(pipeline(new SimpleChannelUpstreamHandler()));
        childChannel.close().sync();
        // child channel is closed
        pipeline.sendUpstream(new DefaultChildChannelStateEvent(parentChannel, childChannel));

        ChannelFuture handlerFuture = handler.getHandlerFuture();
        assertFalse(handlerFuture.isDone());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldPropagateUpstreamInterestOpsEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(channelState(INTEREST_OPS)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        ChannelFuture handlerFuture = handler.getHandlerFuture();
        fireChannelInterestChanged(channel);

        assertFalse(handlerFuture.isDone());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldPropagateUpstreamIdleStateEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(IdleStateEvent.class)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        ChannelFuture handlerFuture = handler.getHandlerFuture();
        pipeline.sendUpstream(new DefaultIdleStateEvent(channel, ALL_IDLE, currentTimeMillis()));

        assertFalse(handlerFuture.isDone());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldPropagateUpstreamWriteCompletionEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(WriteCompletionEvent.class)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        fireWriteComplete(channel, 1024);

        ChannelFuture handlerFuture = handler.getHandlerFuture();
        assertFalse(handlerFuture.isDone());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldPropagateUpstreamExceptionEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(ExceptionEvent.class)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        ChannelFuture handlerFuture = handler.getHandlerFuture();
        fireExceptionCaught(channel, new Exception());

        assertFalse(handlerFuture.isDone());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldPropagateUpstreamUnknownEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(TestChannelEvent.class)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        ChannelFuture handlerFuture = handler.getHandlerFuture();
        pipeline.sendUpstream(new TestChannelEvent(channel, succeededFuture(channel)));

        assertFalse(handlerFuture.isDone());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldConsumeUpstreamOpenedEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
            }
        });

        blockChannelOpen = false;
        channelFactory.newChannel(pipeline);
        ChannelFuture handlerFuture = handler.getHandlerFuture();

        assertTrue(handlerFuture.isDone());
        assertFalse(handlerFuture.isSuccess());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldConsumeUpstreamBoundEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        ChannelFuture handlerFuture = handler.getHandlerFuture();
        fireChannelBound(channel, new LocalAddress("test"));

        assertTrue(handlerFuture.isDone());
        assertFalse(handlerFuture.isSuccess());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldConsumeUpstreamConnectedEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        ChannelFuture handlerFuture = handler.getHandlerFuture();
        fireChannelConnected(channel, new LocalAddress("test"));

        assertTrue(handlerFuture.isDone());
        assertFalse(handlerFuture.isSuccess());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldConsumeUpstreamMessageEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        ChannelFuture handlerFuture = handler.getHandlerFuture();
        fireMessageReceived(channel, new Object());

        assertTrue(handlerFuture.isDone());
        assertFalse(handlerFuture.isSuccess());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldConsumeUpstreamDisconnectedEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        ChannelFuture handlerFuture = handler.getHandlerFuture();
        fireChannelDisconnected(channel);

        assertTrue(handlerFuture.isDone());
        assertFalse(handlerFuture.isSuccess());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldConsumeUpstreamUnboundEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        ChannelFuture handlerFuture = handler.getHandlerFuture();
        fireChannelUnbound(channel);

        assertTrue(handlerFuture.isDone());
        assertFalse(handlerFuture.isSuccess());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldConsumeUpstreamClosedEvent() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        ChannelFuture handlerFuture = handler.getHandlerFuture();
        fireChannelClosed(channel);

        assertTrue(handlerFuture.isSuccess());

        context.assertIsSatisfied();
    }

    @Test
    public void shouldPropagateUpstreamClosedEventAfterFutureDone() throws Exception {

        context.checking(new Expectations() {
            {
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(any(PreparationEvent.class)));
                oneOf(upstream).handleUpstream(with(any(ChannelHandlerContext.class)), with(channelState(OPEN, FALSE)));
            }
        });

        Channel channel = channelFactory.newChannel(pipeline);
        fireChannelClosed(channel);
        fireChannelClosed(channel);

        context.assertIsSatisfied();
    }
}
