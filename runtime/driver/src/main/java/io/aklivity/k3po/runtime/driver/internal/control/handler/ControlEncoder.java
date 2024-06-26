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
package io.aklivity.k3po.runtime.driver.internal.control.handler;

import static java.lang.String.format;
import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;
import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;
import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;
import static org.jboss.netty.util.CharsetUtil.UTF_8;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import io.aklivity.k3po.runtime.driver.internal.control.ControlMessage;
import io.aklivity.k3po.runtime.driver.internal.control.ErrorMessage;
import io.aklivity.k3po.runtime.driver.internal.control.FinishedMessage;
import io.aklivity.k3po.runtime.driver.internal.control.NotifiedMessage;
import io.aklivity.k3po.runtime.driver.internal.control.NotifyMessage;
import io.aklivity.k3po.runtime.driver.internal.control.PreparedMessage;
import io.aklivity.k3po.runtime.driver.internal.control.StartedMessage;
import io.aklivity.k3po.runtime.driver.internal.control.ControlMessage.Kind;

public class ControlEncoder extends OneToOneEncoder {

    private static final byte LF = (byte) 0x0a;

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object message) throws Exception {

        if (message instanceof ControlMessage) {
            ControlMessage controlMessage = (ControlMessage) message;

            switch (controlMessage.getKind()) {
            case PREPARED:
                return encodePreparedMessage(ctx, channel, (PreparedMessage) controlMessage);
            case STARTED:
                return encodeStartedMessage(ctx, channel, (StartedMessage) controlMessage);
            case ERROR:
                return encodeErrorMessage(ctx, channel, (ErrorMessage) controlMessage);
            case FINISHED:
                return encodeFinishedMessage(ctx, channel, (FinishedMessage) controlMessage);
            case NOTIFY:
                return encodeNotifyMessage(ctx, channel, (NotifyMessage) controlMessage);
            case NOTIFIED:
                return encodedNotifiedMessage(ctx, channel, (NotifiedMessage) controlMessage);
            default:
                break;
            }
        }

        // unknown message
        return message;
    }

    private Object encodePreparedMessage(ChannelHandlerContext ctx, Channel channel, PreparedMessage preparedMessage) {

        Kind kind = preparedMessage.getKind();
        String script = preparedMessage.getScript();

        ChannelBuffer buf = dynamicBuffer(channel.getConfig().getBufferFactory());
        encodeInitial(kind, buf);
        for (String barrier : preparedMessage.getBarriers()) {
            // ~ denote injected barriers, which need not be shared with test framework
            if (!barrier.startsWith("~")) {
                encodeHeader("barrier", barrier, buf);
            }
        }
        return encodeContent(script, buf);
    }

    private Object encodeStartedMessage(ChannelHandlerContext ctx, Channel channel, StartedMessage startedMessage) {

        Kind kind = startedMessage.getKind();

        ChannelBuffer buf = dynamicBuffer(channel.getConfig().getBufferFactory());
        encodeInitial(kind, buf);
        return encodeNoContent(buf);
    }

    private Object encodeErrorMessage(ChannelHandlerContext ctx, Channel channel, ErrorMessage errorMessage) {
        Kind kind = errorMessage.getKind();
        String summary = errorMessage.getSummary();
        String description = errorMessage.getDescription();

        ChannelBuffer buf = dynamicBuffer(channel.getConfig().getBufferFactory());
        encodeInitial(kind, buf);
        encodeHeader("summary", summary, buf);
        return encodeContent(description, buf);
    }

    private Object encodeFinishedMessage(ChannelHandlerContext ctx, Channel channel, FinishedMessage finishedMessage) {
        Kind kind = finishedMessage.getKind();
        String script = finishedMessage.getScript();

        ChannelBuffer buf = dynamicBuffer(channel.getConfig().getBufferFactory());
        encodeInitial(kind, buf);
        for (String barrier : finishedMessage.getCompletedBarriers()) {
            // ~ denote injected barriers, which need not be shared with test framework
            if (!barrier.startsWith("~")) {
                encodeHeader("notified", barrier, buf);
            }
        }
        for (String barrier : finishedMessage.getIncompleteBarriers()) {
            // ~ denote injected barriers, which need not be shared with test framework
            if (!barrier.startsWith("~")) {
                encodeHeader("awaiting", barrier, buf);
            }
        }
        return encodeContent(script, buf);
    }

    private Object encodeNotifyMessage(ChannelHandlerContext ctx, Channel channel, NotifyMessage notifyMessage) {
        Kind kind = notifyMessage.getKind();
        String barrier = notifyMessage.getBarrier();

        ChannelBuffer buf = dynamicBuffer(channel.getConfig().getBufferFactory());
        encodeInitial(kind, buf);
        encodeHeader("barrier", barrier, buf);
        return encodeNoContent(buf);
    }

    private Object encodedNotifiedMessage(ChannelHandlerContext ctx, Channel channel, NotifiedMessage notifiedMessage) {
        Kind kind = notifiedMessage.getKind();
        String barrier = notifiedMessage.getBarrier();

        ChannelBuffer buf = dynamicBuffer(channel.getConfig().getBufferFactory());
        encodeInitial(kind, buf);
        encodeHeader("barrier", barrier, buf);
        return encodeNoContent(buf);
    }

    private static void encodeInitial(Kind kind, ChannelBuffer buf) {
        buf.writeBytes(copiedBuffer(kind.toString(), UTF_8));
        buf.writeByte(LF);
    }

    private static void encodeHeader(String bufName, Object bufValue, ChannelBuffer buf) {
        if (bufValue != null) {
            buf.writeBytes(copiedBuffer(format("%s:%s", bufName, bufValue), UTF_8));
            buf.writeByte(LF);
        }
    }

    private static ChannelBuffer encodeNoContent(ChannelBuffer buf) {
        buf.writeByte(LF);
        return buf;
    }

    private static ChannelBuffer encodeContent(String content, ChannelBuffer buf) {
        if (content == null) {
            // note: missing content not same as empty content
            return encodeNoContent(buf);
        }
        else {
            ChannelBuffer contentBuf = copiedBuffer(content, UTF_8);
            encodeHeader("content-length", contentBuf.readableBytes(), buf);
            buf.writeByte(LF);
            return wrappedBuffer(buf, contentBuf);
        }

    }

}
