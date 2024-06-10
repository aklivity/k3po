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
import javax.el.ValueExpression;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.junit.Before;
import org.junit.Test;

import io.aklivity.k3po.runtime.driver.internal.behavior.ScriptProgressException;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.MessageDecoder;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.ReadExpressionDecoder;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionFactoryUtils;

public class ReadExpressionDecoderTest {

    private ExpressionFactory expressionFactory;
    private ExpressionContext environment;

    @Before
    public void setUp() {
        ChannelPipeline pipeline = pipeline(new SimpleChannelHandler());
        ChannelFactory channelFactory = new DefaultLocalClientChannelFactory();
        channelFactory.newChannel(pipeline);
        pipeline.getContext(SimpleChannelHandler.class);
        expressionFactory = ExpressionFactoryUtils.newExpressionFactory();
        environment = new ExpressionContext();
    }

    @Test
    public void completeMatchOK() throws Exception {
        ValueExpression expression = expressionFactory.createValueExpression(environment, "${variable}", byte[].class);
        MessageDecoder decoder = new ReadExpressionDecoder(expression, environment);

        environment.getELResolver().setValue(environment, null, "variable", new byte[]{0x01, 0x02, 0x03});

        ChannelBuffer remainingBuffer = decoder.decode(copiedBuffer(new byte[]{0x01, 0x02, 0x03}));
        assertNotNull(remainingBuffer);
        assertEquals(0, remainingBuffer.readableBytes());
    }

    @Test(
            expected = ScriptProgressException.class)
    public void noMatchOK() throws Exception {
        ValueExpression expression = expressionFactory.createValueExpression(environment, "${variable}", byte[].class);
        MessageDecoder decoder = new ReadExpressionDecoder(expression, environment);

        environment.getELResolver().setValue(environment, null, "variable", new byte[]{0x01, 0x02, 0x03});

        decoder.decode(copiedBuffer(new byte[]{0x01, 0x02, 0x04}));
    }

    @Test
    public void fragmentedMatchOK() throws Exception {
        ValueExpression expression = expressionFactory.createValueExpression(environment, "${variable}", byte[].class);
        MessageDecoder decoder = new ReadExpressionDecoder(expression, environment);

        environment.getELResolver().setValue(environment, null, "variable", new byte[]{0x01, 0x02, 0x03});

        ChannelBuffer remainingBuffer = decoder.decode(copiedBuffer(new byte[]{0x01, 0x02}));
        assertNull(remainingBuffer);

        remainingBuffer = decoder.decode(copiedBuffer(new byte[]{0x03}));
        assertNotNull(remainingBuffer);
        assertEquals(0, remainingBuffer.readableBytes());
    }

    @Test(
            expected = ScriptProgressException.class)
    public void onlyPartialMatchOK() throws Exception {
        ValueExpression expression = expressionFactory.createValueExpression(environment, "${variable}", byte[].class);
        MessageDecoder decoder = new ReadExpressionDecoder(expression, environment);

        environment.getELResolver().setValue(environment, null, "variable", new byte[]{0x01, 0x02, 0x03});

        ChannelBuffer remainingBuffer = decoder.decode(copiedBuffer(new byte[]{0x01, 0x02}));
        assertNull(remainingBuffer);

        decoder.decode(copiedBuffer(new byte[]{0x04}));
    }

    @Test
    public void completeMatchWithBytesLeftOverOK() throws Exception {
        ValueExpression expression = expressionFactory.createValueExpression(environment, "${variable}", byte[].class);
        MessageDecoder decoder = new ReadExpressionDecoder(expression, environment);

        environment.getELResolver().setValue(environment, null, "variable", new byte[]{0x01, 0x02, 0x03});

        ChannelBuffer remainingBuffer = decoder.decode(copiedBuffer(new byte[]{0x01, 0x02, 0x03, 0x04}));
        assertNotNull(remainingBuffer);
        assertEquals(copiedBuffer(new byte[]{0x04}), remainingBuffer);
    }
}
