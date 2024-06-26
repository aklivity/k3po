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
package io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.http;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelSink;

import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.channel.AbstractServerChannel;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.channel.ChannelConfig;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.channel.DefaultServerChannelConfig;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddress;

public class HttpServerChannel extends AbstractServerChannel<ChannelConfig> {

    HttpServerChannel(ChannelFactory factory, ChannelPipeline pipeline, ChannelSink sink) {
        super(factory, pipeline, sink, new DefaultServerChannelConfig());
    }

    @Override
    protected void setLocalAddress(ChannelAddress localAddress) {
        super.setLocalAddress(localAddress);
    }

    @Override
    protected void setBound() {
        super.setBound();
    }

    @Override
    protected void setTransport(Channel transport) {
        super.setTransport(transport);
    }

    @Override
    protected boolean setClosed() {
        return super.setClosed();
    }

    @Override
    protected Channel getTransport() {
        return super.getTransport();
    }

}
