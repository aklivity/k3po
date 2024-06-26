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
package io.aklivity.k3po.runtime.driver.internal.behavior.handler.event;

import static io.aklivity.k3po.runtime.driver.internal.behavior.handler.event.AbstractEventHandler.ChannelEventKind.READ_ADVISED;
import static java.util.Collections.singletonList;
import static java.util.EnumSet.of;

import java.util.List;
import java.util.Objects;

import javax.el.ELException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;

import io.aklivity.k3po.runtime.driver.internal.behavior.ScriptProgressException;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.ChannelDecoder;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.ReadAdviseEvent;

public class ReadAdvisedHandler extends AbstractEventHandler {

    private final Object value;
    private final List<ChannelDecoder> decoders;

    public ReadAdvisedHandler(Object value, ChannelDecoder decoder) {
        this(value, singletonList(decoder));
    }

    public ReadAdvisedHandler(Object value, List<ChannelDecoder> decoders) {
        super(of(READ_ADVISED));
        this.value = value;
        this.decoders = decoders;
    }

    @Override
    public void inputAdvised(ChannelHandlerContext ctx, ReadAdviseEvent e) {

        ChannelFuture handlerFuture = getHandlerFuture();
        assert handlerFuture != null;

        outer:
        try {
            if (!Objects.equals(value, e.getValue()))
            {
                handlerFuture.setFailure(new ScriptProgressException(getRegionInfo(), String.valueOf(e.getValue())));
                break outer;
            }

            Channel channel = ctx.getChannel();
            for (ChannelDecoder decoder : decoders) {
                boolean decoded = decoder.decode(channel);
                if (!decoded)
                {
                    handlerFuture.setFailure(new ScriptProgressException(getRegionInfo(), "decode failed"));
                    break outer;
                }
            }
            handlerFuture.setSuccess();
        }
        catch (ELException ele) {
            ScriptProgressException exception = new ScriptProgressException(getRegionInfo(), ele.getMessage());
            exception.initCause(ele);
            handlerFuture.setFailure(exception);
        }
        catch (Exception ex) {
            handlerFuture.setFailure(ex);
        }
    }

    @Override
    protected StringBuilder describe(StringBuilder sb) {
        return sb.append(String.format("read advised %s %s", value, decoders));
    }
}
