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

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.US_ASCII;

import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpVersion;

import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.ChannelEncoder;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.MessageEncoder;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.http.HttpChannelConfig;

public class HttpVersionEncoder implements ChannelEncoder {

    private MessageEncoder versionEncoder;

    public HttpVersionEncoder(MessageEncoder versionEncoder) {
        this.versionEncoder = versionEncoder;
    }

    @Override
    public void encode(Channel channel) throws Exception {
        HttpChannelConfig httpConfig = (HttpChannelConfig) channel.getConfig();
        ChannelBufferFactory bufferFactory = httpConfig.getBufferFactory();
        String versionName = versionEncoder.encode(bufferFactory).toString(US_ASCII);
        HttpVersion version = HttpVersion.valueOf(versionName);
        httpConfig.setVersion(version);
    }

    @Override
    public String toString() {
        return format("http:version %s", versionEncoder);
    }

}
