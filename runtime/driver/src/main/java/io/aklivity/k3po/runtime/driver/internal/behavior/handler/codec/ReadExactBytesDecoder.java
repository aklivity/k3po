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
import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;

import org.jboss.netty.buffer.ChannelBuffer;

import io.aklivity.k3po.runtime.driver.internal.behavior.ScriptProgressException;
import io.aklivity.k3po.runtime.driver.internal.util.Utils;
import io.aklivity.k3po.runtime.lang.internal.RegionInfo;

public class ReadExactBytesDecoder extends MessageDecoder {

    private final ChannelBuffer expected;

    public ReadExactBytesDecoder(RegionInfo regionInfo, byte[] expected) {
        super(regionInfo);
        this.expected = copiedBuffer(expected);
    }

    @Override
    protected Object decodeBuffer(ChannelBuffer buffer) throws Exception {

        if (buffer.readableBytes() < expected.readableBytes()) {
            return null;
        }

        ChannelBuffer observed = buffer.readSlice(expected.readableBytes());
        if (!observed.equals(expected)) {
            throw new ScriptProgressException(getRegionInfo(), Utils.format(observed));
        }

        return buffer;
    }

    @Override
    public String toString() {
        return Utils.format(expected.array());
    }

    // unit tests
    ReadExactBytesDecoder(byte[] expected) {
        this(newSequential(0, 0), expected);
    }

}
