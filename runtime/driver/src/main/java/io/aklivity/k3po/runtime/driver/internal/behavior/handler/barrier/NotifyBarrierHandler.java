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

import static io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelFutureListeners.chainedFuture;
import static java.lang.String.format;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;

import io.aklivity.k3po.runtime.driver.internal.behavior.Barrier;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.prepare.PreparationEvent;

public class NotifyBarrierHandler extends AbstractBarrierHandler {

    public NotifyBarrierHandler(Barrier barrier) {
        super(barrier);
    }

    @Override
    public void prepareRequested(final ChannelHandlerContext ctx, PreparationEvent evt) {

        super.prepareRequested(ctx, evt);

        Barrier barrier = getBarrier();
        final ChannelFuture barrierFuture = barrier.getFuture();

        ChannelFuture pipelineFuture = getPipelineFuture();
        ChannelFuture handlerFuture = getHandlerFuture();

        pipelineFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    // We only want to set barrier future when the pipeline is success. Otherwise it could cause other
                    // streams to "fail" incorrectly.
                    barrierFuture.setSuccess();
                }
            }
        });

        pipelineFuture.addListener(chainedFuture(handlerFuture));
    }

    @Override
    protected StringBuilder describe(StringBuilder sb) {
        return sb.append(format("read|write notify %s", getBarrier()));
    }

}
