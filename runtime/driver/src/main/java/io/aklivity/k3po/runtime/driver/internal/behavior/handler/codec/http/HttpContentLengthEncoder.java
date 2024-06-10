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
package io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.http;

import org.jboss.netty.channel.Channel;

import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.ChannelEncoder;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.http.HttpChannelConfig;

public class HttpContentLengthEncoder implements ChannelEncoder {

    private final int maximumBufferedContentLength;

    public HttpContentLengthEncoder() {
        // TODO: configure?
        this.maximumBufferedContentLength = Integer.MAX_VALUE;
    }

    @Override
    public void encode(Channel channel) throws Exception {
        HttpChannelConfig httpConfig = (HttpChannelConfig) channel.getConfig();
        httpConfig.setMaximumBufferedContentLength(maximumBufferedContentLength);
    }

    @Override
    public String toString() {
        return "http:content-length";
    }
}
