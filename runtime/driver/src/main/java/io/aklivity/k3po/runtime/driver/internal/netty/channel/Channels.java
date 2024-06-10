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

import static org.jboss.netty.channel.Channels.close;
import static org.jboss.netty.channel.Channels.future;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelUpstreamHandler;

import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.channel.AbstractChannel;

public final class Channels {

    /**
     * Sends a {@code "shutdownInput"} event to the
     * {@link ChannelUpstreamHandler} which is placed in the closest upstream
     * from the handler associated with the specified
     * {@link ChannelHandlerContext}.
     */
    public static void fireInputShutdown(ChannelHandlerContext ctx) {
        ctx.getPipeline().sendUpstream(
                new UpstreamShutdownInputEvent(ctx.getChannel()));
    }

    /**
     * Sends a {@code "shutdownInput"} event to the first
     * {@link ChannelUpstreamHandler} in the {@link ChannelPipeline} of
     * the specified {@link Channel}.
     */
    public static void fireInputShutdown(Channel channel) {
        channel.getPipeline().sendUpstream(
                new UpstreamShutdownInputEvent(channel));
    }

    /**
     * Sends a {@code "flushed"} event to the first
     * {@link ChannelUpstreamHandler} in the {@link ChannelPipeline} of
     * the specified {@link Channel}.
     */
    public static void fireFlushed(Channel channel) {
        channel.getPipeline().sendUpstream(
                new UpstreamFlushEvent(channel));
    }

    /**
     * Sends a {@code "shutdownInput"} request to the last
     * {@link ChannelDownstreamHandler} in the {@link ChannelPipeline} of
     * the specified {@link Channel}.
     *
     * @param channel  the channel to bind
     *
     * @return the {@link ChannelFuture} which will be notified when the
     *         shutdownInput operation is done
     */
    public static ChannelFuture shutdownInput(Channel channel) {
        ChannelFuture future = future(channel);
        channel.getPipeline().sendDownstream(
                new DownstreamShutdownInputEvent(channel, future));
        return future;
    }

    /**
     * Sends a {@code "shutdownInput"} request to the
     * {@link ChannelDownstreamHandler} which is placed in the closest
     * downstream from the handler associated with the specified
     * {@link ChannelHandlerContext}.
     *
     * @param ctx     the context
     * @param future  the future which will be notified when the shutdownInput
     *                operation is done
     */
    public static void shutdownInput(ChannelHandlerContext ctx, ChannelFuture future) {
        ctx.sendDownstream(
                new DownstreamShutdownInputEvent(ctx.getChannel(), future));
    }

    /**
     * Sends a {@code "shutdownOutput"} event to the
     * {@link ChannelUpstreamHandler} which is placed in the closest upstream
     * from the handler associated with the specified
     * {@link ChannelHandlerContext}.
     */
    public static void fireOutputShutdown(ChannelHandlerContext ctx) {
        ctx.getPipeline().sendUpstream(
                new UpstreamShutdownOutputEvent(ctx.getChannel()));
    }

    /**
     * Sends a {@code "shutdownOutput"} event to the first
     * {@link ChannelUpstreamHandler} in the {@link ChannelPipeline} of
     * the specified {@link Channel}.
     */
    public static void fireOutputShutdown(Channel channel) {
        channel.getPipeline().sendUpstream(
                new UpstreamShutdownOutputEvent(channel));
    }

    /**
     * Sends a {@code "shutdownOutput"} request to the last
     * {@link ChannelDownstreamHandler} in the {@link ChannelPipeline} of
     * the specified {@link Channel}.
     *
     * @param channel  the channel to bind
     *
     * @return the {@link ChannelFuture} which will be notified when the
     *         shutdownOutput operation is done
     */
    public static ChannelFuture shutdownOutput(Channel channel) {
        ChannelFuture future = future(channel);
        channel.getPipeline().sendDownstream(
                new DownstreamShutdownOutputEvent(channel, future));
        return future;
    }

    /**
     * Sends a {@code "shutdownOutput"} request to the
     * {@link ChannelDownstreamHandler} which is placed in the closest
     * downstream from the handler associated with the specified
     * {@link ChannelHandlerContext}.
     *
     * @param ctx     the context
     * @param future  the future which will be notified when the shutdownOutput
     *                operation is done
     */
    public static void shutdownOutput(ChannelHandlerContext ctx, ChannelFuture future) {
        ctx.sendDownstream(
                new DownstreamShutdownOutputEvent(ctx.getChannel(), future));
    }

    /**
     * Sends a {@code "flush"} request to the last
     * {@link ChannelDownstreamHandler} in the {@link ChannelPipeline} of
     * the specified {@link Channel}.
     *
     * @param channel  the channel to bind
     *
     * @return the {@link ChannelFuture} which will be notified when the
     *         flush operation is done
     */
    public static ChannelFuture flush(Channel channel) {
        ChannelFuture future = future(channel);
        channel.getPipeline().sendDownstream(
                new DownstreamFlushEvent(channel, future));
        return future;
    }

    /**
     * Sends a {@code "flush"} request to the
     * {@link ChannelDownstreamHandler} which is placed in the closest
     * downstream from the handler associated with the specified
     * {@link ChannelHandlerContext}.
     *
     * @param ctx     the context
     * @param future  the future which will be notified when the flush
     *                operation is done
     */
    public static void flush(ChannelHandlerContext ctx, ChannelFuture future) {
        ctx.sendDownstream(
                new DownstreamFlushEvent(ctx.getChannel(), future));
    }

    public static ChannelFuture abortOutput(Channel channel) {
        ChannelFuture future = future(channel);
        channel.getPipeline().sendDownstream(
                new DownstreamWriteAbortEvent(channel, future));
        return future;
    }

    public static void abortOutput(ChannelHandlerContext ctx, ChannelFuture future) {
        ctx.sendDownstream(new DownstreamWriteAbortEvent(ctx.getChannel(), future));
    }

    public static void fireOutputAborted(Channel channel) {
        channel.getPipeline().sendUpstream(new UpstreamWriteAbortEvent(channel));
    }

    public static ChannelFuture abortInput(Channel channel) {
        ChannelFuture future = future(channel);
        channel.getPipeline().sendDownstream(
                new DownstreamReadAbortEvent(channel, future));
        return future;
    }

    public static void abortInput(ChannelHandlerContext ctx, ChannelFuture future) {
        ctx.sendDownstream(new DownstreamReadAbortEvent(ctx.getChannel(), future));
    }

    public static void fireInputAborted(Channel channel) {
        channel.getPipeline().sendUpstream(new UpstreamReadAbortEvent(channel));
    }

    public static ChannelFuture adviseOutput(Channel channel, Object value) {
        ChannelFuture future = future(channel);
        channel.getPipeline().sendDownstream(
                new DownstreamWriteAdviseEvent(channel, future, value));
        return future;
    }

    public static void adviseOutput(ChannelHandlerContext ctx, ChannelFuture future, Object value) {
        ctx.sendDownstream(new DownstreamWriteAdviseEvent(ctx.getChannel(), future, value));
    }

    public static void fireOutputAdvised(Channel channel, Object value) {
        channel.getPipeline().sendUpstream(new UpstreamWriteAdviseEvent(channel, value));
    }

    public static ChannelFuture adviseInput(Channel channel, Object value) {
        ChannelFuture future = future(channel);
        channel.getPipeline().sendDownstream(
                new DownstreamReadAdviseEvent(channel, future, value));
        return future;
    }

    public static void adviseInput(ChannelHandlerContext ctx, ChannelFuture future, Object value) {
        ctx.sendDownstream(new DownstreamReadAdviseEvent(ctx.getChannel(), future, value));
    }

    public static void fireInputAdvised(Channel channel, Object value) {
        channel.getPipeline().sendUpstream(new UpstreamReadAdviseEvent(channel, value));
    }

    public static ChannelFuture shutdownOutputOrClose(Channel channel) {
        if (channel instanceof AbstractChannel) {
            return shutdownOutput(channel);
        }
        else {
            return close(channel);
        }
    }

    public static void shutdownOutputOrClose(ChannelHandlerContext ctx, ChannelFuture future) {
        if (ctx.getChannel() instanceof AbstractChannel) {
            shutdownOutput(ctx, future);
        }
        else {
            close(ctx, future);
        }
    }

    public static ChannelFuture abortOutputOrClose(Channel channel) {
        if (channel instanceof AbstractChannel) {
            return abortOutput(channel);
        }
        else {
            return close(channel);
        }
    }

    public static void abortOutputOrClose(ChannelHandlerContext ctx, ChannelFuture future) {
        if (ctx.getChannel() instanceof AbstractChannel) {
            abortOutput(ctx, future);
        }
        else {
            close(ctx, future);
        }
    }

    public static void abortInputOrSuccess(ChannelHandlerContext ctx, ChannelFuture future) {
        if (ctx.getChannel() instanceof AbstractChannel) {
            abortInput(ctx, future);
        }
        else {
            future.setSuccess();
        }
    }

    private Channels() {
        // no instances
    }
}
