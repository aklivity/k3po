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

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;
import static org.jboss.netty.channel.Channels.pipeline;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.MessageDecoder;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.ReadLongLengthBytesDecoder;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionFactoryUtils;

public class ReadLongLengthBytesDecoderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ExpressionContext environment;
    private ExpressionFactory expressionFactory;

    @Before
    public void setUp() {
        ChannelPipeline pipeline = pipeline(new SimpleChannelHandler());
        ChannelFactory channelFactory = new DefaultLocalClientChannelFactory();
        channelFactory.newChannel(pipeline);
        pipeline.getContext(SimpleChannelHandler.class);
        environment = new ExpressionContext();
        expressionFactory = ExpressionFactoryUtils.newExpressionFactory();
    }

    @Test
    public void completeMatchWithCaptureOK() throws Exception {
        MessageDecoder decoder = new ReadLongLengthBytesDecoder(environment, "var");
        ChannelBuffer remainingBuffer = decoder.decode(copiedBuffer(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05}));
        assertNotNull(remainingBuffer);
        assertEquals(0, remainingBuffer.readableBytes());
        ValueExpression expression = expressionFactory.createValueExpression(environment, "${var}", Long.class);
        Long actual = (Long) expression.getValue(environment);
        assertEquals(new Long(5), actual);
    }

    @Test
    public void noMatchWithCaptureOK() throws Exception {
        MessageDecoder decoder = new ReadLongLengthBytesDecoder(environment, "var");
        ChannelBuffer remainingBuffer = decoder.decode(copiedBuffer(new byte[]{0x00, 0x00, 0x00}));

        assertNull(remainingBuffer);
        ValueExpression expression = expressionFactory.createValueExpression(environment, "${var}", Long.class);

        thrown.expect(PropertyNotFoundException.class);
        expression.getValue(environment);
    }

    @Test
    public void fragmentedMatchWithCaptureOK() throws Exception {
        MessageDecoder decoder = new ReadLongLengthBytesDecoder(environment, "var");
        ChannelBuffer remainingBuffer = decoder.decode(copiedBuffer(new byte[]{0x00, 0x00}));
        assertNull(remainingBuffer);
        ValueExpression expression = expressionFactory.createValueExpression(environment, "${var}", Long.class);

        try {
            expression.getValue(environment);
        } catch (PropertyNotFoundException p) {
            // OK
        }

        remainingBuffer = decoder.decode(copiedBuffer(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x05}));
        assertNotNull(remainingBuffer);
        assertEquals(0, remainingBuffer.readableBytes());

        Long actual = (Long) expression.getValue(environment);
        assertEquals(new Long(5), actual);
    }

    @Test
    public void completeMatchWithBytesLeftOverWithCapturerOK() throws Exception {
        MessageDecoder decoder = new ReadLongLengthBytesDecoder(environment, "var");
        ChannelBuffer remainingBuffer =
                decoder.decode(copiedBuffer(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05, 0x05}));
        assertNotNull(remainingBuffer);
        assertEquals(copiedBuffer(new byte[]{0x05}), remainingBuffer);
        ValueExpression expression = expressionFactory.createValueExpression(environment, "${var}", Long.class);
        Long actual = (Long) expression.getValue(environment);
        assertEquals(new Long(5), actual);
    }
}
