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

public class ReadIntLengthBytesDecoder extends ReadFixedLengthBytesDecoder<Integer> {

    public ReadIntLengthBytesDecoder(RegionInfo regionInfo, ExpressionContext environment, String captureName) {
        super(regionInfo, Integer.SIZE / Byte.SIZE, environment, captureName);
    }

    // Read the data into an Integer
    @Override
    public Integer readBuffer(ChannelBuffer buffer) {
        return buffer.readInt();
    }

    // unit tests
    ReadIntLengthBytesDecoder(ExpressionContext environment, String captureName) {
        this(newSequential(0, 0), environment, captureName);
    }
}
