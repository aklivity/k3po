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
package io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;

import io.aklivity.k3po.runtime.driver.internal.util.Utils;

public class WriteBytesEncoder implements MessageEncoder {

    private final byte[] bytes;

    public WriteBytesEncoder(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public ChannelBuffer encode(ChannelBufferFactory bufferFactory) {
        return bufferFactory.getBuffer(bytes, 0, bytes.length);
    }

    @Override
    public String toString() {
        return Utils.format(bytes);
    }

}