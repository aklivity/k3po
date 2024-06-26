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

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;

public abstract class SimplePrepareDownstreamHandler extends SimpleChannelDownstreamHandler {

    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent evt) throws Exception {

        if (evt instanceof PreparationEvent) {
            prepareComplete(ctx, (PreparationEvent) evt);
        }
        else {
            super.handleDownstream(ctx, evt);
        }
    }

    public void prepareComplete(ChannelHandlerContext ctx, PreparationEvent evt) {
        ctx.sendDownstream(evt);
    }
}
