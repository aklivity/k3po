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

import static org.jboss.netty.channel.Channels.pipeline;

import java.util.NavigableMap;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddress;

final class HttpChildChannelPipelineFactory implements ChannelPipelineFactory {

    private final NavigableMap<ChannelAddress, HttpServerChannel> httpBindings;

    public HttpChildChannelPipelineFactory(NavigableMap<ChannelAddress, HttpServerChannel> httpBindings) {
        this.httpBindings = httpBindings;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        return pipeline(new HttpRequestDecoder(), new HttpResponseEncoder(), new HttpChildChannelSource(httpBindings));
    }

}
