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
package io.aklivity.k3po.runtime.driver.internal.behavior.handler;

import static io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelFutureListeners.chainedFuture;
import static org.jboss.netty.channel.Channels.succeededFuture;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;

import io.aklivity.k3po.runtime.driver.internal.behavior.handler.prepare.DownstreamPreparationEvent;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.prepare.PreparationEvent;

public class CompletionHandler extends ExecutionHandler {

    @Override
    public void prepareRequested(final ChannelHandlerContext ctx, final PreparationEvent evt) {

        super.prepareRequested(ctx, evt);

        // when the pipeline future completes, trigger success of this handler
        // future
        ChannelFuture pipelineFuture = getPipelineFuture();
        ChannelFuture handlerFuture = getHandlerFuture();

        pipelineFuture.addListener(chainedFuture(handlerFuture));

        ChannelFuture prepareFuture = evt.getFuture();
        prepareFuture.setSuccess();

        Channel channel = evt.getChannel();
        ctx.sendDownstream(new DownstreamPreparationEvent(channel, succeededFuture(channel)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // ignore (already tracking completion status via completion future cause)
    }

    @Override
    protected StringBuilder describe(StringBuilder sb) {
        return sb.append("completion");
    }
}
