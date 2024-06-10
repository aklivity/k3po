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

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import io.aklivity.k3po.runtime.driver.internal.behavior.ScriptProgressException;
import io.aklivity.k3po.runtime.lang.internal.RegionInfo;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;
import io.aklivity.k3po.runtime.lang.internal.regex.NamedGroupMatcher;
import io.aklivity.k3po.runtime.lang.internal.regex.NamedGroupPattern;

public class ReadRegexDecoder extends MessageDecoder {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(ReadRegexDecoder.class);

    private final NamedGroupPattern pattern;
    private final Charset charset;
    private final ExpressionContext environment;

    public ReadRegexDecoder(RegionInfo regionInfo, NamedGroupPattern pattern, Charset charset, ExpressionContext environment) {
        super(regionInfo);
        this.pattern = pattern;
        this.environment = environment;
        this.charset = charset;
    }

    @Override
    public String toString() {
        return format("/%s/", pattern);
    }

    @Override
    protected Object decodeBufferLast(final ChannelBuffer buffer) throws Exception {
        return decodeBuffer(buffer, true);
    }

    @Override
    protected Object decodeBuffer(final ChannelBuffer buffer) throws Exception {
        return decodeBuffer(buffer, false);
    }

    // unit tests
    ReadRegexDecoder(NamedGroupPattern pattern, Charset charset, ExpressionContext environment) {
        this(newSequential(0, 0), pattern, charset, environment);
    }

    private Object decodeBuffer(final ChannelBuffer buffer, boolean isLast) throws Exception {

        final ChannelBuffer observedBytes = buffer.slice();
        final String observed = observedBytes.toString(charset);

        final NamedGroupMatcher matcher = pattern.matcher(observed);

        // TODO: Need to deal with anchoring
        boolean allInputMatched = matcher.matches();
        boolean prefixMatched = allInputMatched || matcher.lookingAt();
        boolean noMatchMayMatchLater = !prefixMatched && matcher.hitEnd();

        // We keep looking while we match or while we don't match but it is still possible to match
        if ((allInputMatched || !isLast) && noMatchMayMatchLater) {
            return null;
        }

        // If we never matched we fail.
        if (!prefixMatched) {
            throw new ScriptProgressException(getRegionInfo(), format("\"%s\"", observed));
        }

        captureGroups(matcher);

        // skip the bytes we actually matched
        buffer.skipBytes(matcher.end());

        return buffer;
    }

    private void captureGroups(NamedGroupMatcher matcher) {
        for (String captureName : matcher.groupNames()) {
            String captured = matcher.group(captureName);
            // TODO: Remove when JUEL sync bug is fixed https://github.com/k3po/k3po/issues/147
            synchronized (environment) {
                environment.getELResolver().setValue(environment, null, captureName, captured);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(format("Setting value for ${%s} to %s", captureName, captured));
            }
        }
    }
}
