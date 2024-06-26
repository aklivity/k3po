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

import static io.aklivity.k3po.runtime.lang.internal.RegionInfo.newSequential;

import org.jboss.netty.buffer.ChannelBuffer;

import io.aklivity.k3po.runtime.lang.internal.RegionInfo;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;

public class ReadByteArrayBytesDecoder extends ReadFixedLengthBytesDecoder<byte[]> {

    public ReadByteArrayBytesDecoder(RegionInfo regionInfo, int length) {
        super(regionInfo, length);
    }

    public ReadByteArrayBytesDecoder(RegionInfo regionInfo, int length, ExpressionContext environment, String captureName) {
        super(regionInfo, length, environment, captureName);
    }

    // Read the data into an array of bytes
    @Override
    public byte[] readBuffer(final ChannelBuffer buffer) {
        int len = getLength();
        byte[] matched = new byte[len];
        buffer.readBytes(matched);
        return matched;
    }

    // unit tests
    ReadByteArrayBytesDecoder(int length) {
        this(newSequential(0, 0), length);
    }

    // unit tests
    ReadByteArrayBytesDecoder(int length, ExpressionContext environment, String captureName) {
        this(newSequential(0, 0), length, environment, captureName);
    }
}
