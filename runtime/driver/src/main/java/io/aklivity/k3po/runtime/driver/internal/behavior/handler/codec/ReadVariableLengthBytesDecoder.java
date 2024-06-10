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
import static java.lang.String.format;

import javax.el.ValueExpression;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import io.aklivity.k3po.runtime.lang.internal.RegionInfo;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralBytesValue;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;

public class ReadVariableLengthBytesDecoder extends MessageDecoder {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(ReadVariableLengthBytesDecoder.class);

    private final ValueExpression length;
    private final ExpressionContext environment;
    private final String captureName;

    public ReadVariableLengthBytesDecoder(RegionInfo regionInfo, ValueExpression length, ExpressionContext environment) {
        this(regionInfo, length, environment, null);
    }

    public ReadVariableLengthBytesDecoder(RegionInfo regionInfo, ValueExpression length, ExpressionContext environment,
            String captureName) {
        super(regionInfo);
        this.length = length;
        this.environment = environment;
        this.captureName = captureName;
    }

    @Override
    public String toString() {
        return format("%s bytes", length);
    }

    @Override
    protected Object decodeBuffer(ChannelBuffer buffer) throws Exception {
        final int resolvedLength;
        // TODO: Remove when JUEL sync bug is fixed https://github.com/k3po/k3po/issues/147
        synchronized (environment) {
            resolvedLength = (Integer) length.getValue(environment);
        }

        if (buffer.readableBytes() < resolvedLength) {
            return null;
        }

        if (captureName == null) {
            buffer.readSlice(resolvedLength);
        } else {
            byte[] bytes = new byte[resolvedLength];
            buffer.readBytes(bytes, 0, resolvedLength);
            // TODO: Remove when JUEL sync bug is fixed https://github.com/k3po/k3po/issues/147
            synchronized (environment) {
                environment.getELResolver().setValue(environment, null, captureName, bytes);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(format("Setting value for ${%s} to %s", captureName, AstLiteralBytesValue.toString(bytes)));
            }
        }
        return buffer;
    }

    // unit tests
    ReadVariableLengthBytesDecoder(ValueExpression length, ExpressionContext environment) {
        this(newSequential(0, 0), length, environment);
    }

    // unit tests
    ReadVariableLengthBytesDecoder(ValueExpression length, ExpressionContext environment, String captureName) {
        this(newSequential(0, 0), length, environment, captureName);
    }
}
