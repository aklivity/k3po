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
package io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.http;

import static io.aklivity.k3po.runtime.driver.internal.channel.Channels.remoteAddress;
import static io.aklivity.k3po.runtime.driver.internal.netty.channel.Channels.abortOutput;
import static io.aklivity.k3po.runtime.driver.internal.netty.channel.Channels.fireInputAborted;
import static io.aklivity.k3po.runtime.driver.internal.netty.channel.Channels.fireInputShutdown;
import static java.lang.String.format;
import static org.jboss.netty.channel.Channels.fireChannelBound;
import static org.jboss.netty.channel.Channels.fireChannelClosed;
import static org.jboss.netty.channel.Channels.fireChannelConnected;
import static org.jboss.netty.channel.Channels.fireChannelDisconnected;
import static org.jboss.netty.channel.Channels.fireChannelOpen;
import static org.jboss.netty.channel.Channels.fireChannelUnbound;
import static org.jboss.netty.channel.Channels.fireExceptionCaught;
import static org.jboss.netty.channel.Channels.fireMessageReceived;
import static org.jboss.netty.channel.Channels.future;
import static org.jboss.netty.channel.Channels.write;
import static org.jboss.netty.handler.codec.http.HttpHeaders.getContentLength;
import static org.jboss.netty.handler.codec.http.HttpHeaders.getHost;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isContentLengthSet;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isTransferEncodingChunked;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.SWITCHING_PROTOCOLS;

import java.net.URI;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.http.QueryStringEncoder;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.channel.ChannelConfig;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.http.HttpChildChannel.HttpReadState;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddress;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.ShutdownInputEvent;

public class HttpChildChannelSource extends HttpChannelHandler {

    private final NavigableMap<ChannelAddress, HttpServerChannel> httpBindings;

    private volatile HttpChildChannel httpChildChannel;

    public HttpChildChannelSource(NavigableMap<ChannelAddress, HttpServerChannel> httpBindings) {
        this.httpBindings = httpBindings;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        HttpChildChannel httpChildChannel = this.httpChildChannel;
        if (httpChildChannel != null) {

            this.httpChildChannel = null;

            if (httpChildChannel.setReadClosed() || httpChildChannel.setWriteClosed()) {
                fireExceptionCaught(httpChildChannel, e.getCause());
                fireChannelClosed(httpChildChannel);
            }
        }

        Channel channel = ctx.getChannel();
        channel.close();
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        HttpChildChannel httpChildChannel = this.httpChildChannel;
        if (httpChildChannel != null) {

            this.httpChildChannel = null;

            switch (httpChildChannel.readState()) {
            case UPGRADED:
                if (httpChildChannel.setReadClosed()) {
                    fireChannelDisconnected(httpChildChannel);
                    fireChannelUnbound(httpChildChannel);
                    fireChannelClosed(httpChildChannel);
                }
                break;
            case CONTENT_COMPLETE:
                break;
            default:
                if (httpChildChannel.setReadAborted()) {
                    fireInputAborted(httpChildChannel);
                    httpChildChannel.setClosed();
                }
                break;
            }
        }
    }

    @Override
    public void inputShutdown(ChannelHandlerContext ctx, ShutdownInputEvent e) {
        HttpChildChannel httpChildChannel = this.httpChildChannel;
        if (httpChildChannel != null) {
            if (httpChildChannel.readState() == HttpReadState.UPGRADED)
            {
                fireInputShutdown(httpChildChannel);
            }
        }
        else {
            abortOutput(ctx, Channels.future(e.getChannel()));
        }
    }

    @Override
    protected void httpMessageReceived(ChannelHandlerContext ctx, MessageEvent e, HttpRequest httpRequest) throws Exception {
        HttpVersion version = httpRequest.getProtocolVersion();
        URI httpLocation = getEffectiveURI(httpRequest);
        if (httpLocation == null) {
            // see RFC-7230 section 5.4 Host
            HttpResponse httpResponse = new DefaultHttpResponse(version, BAD_REQUEST);
            ChannelFuture future = future(ctx.getChannel());
            write(ctx, future, httpResponse);
            return;
        }

        // channel's local address is resolved address so get the bind address from
        // server channel's attachment
        ChannelAddress transportCandidate = (ChannelAddress) ctx.getChannel().getParent().getAttachment();
        ChannelAddress candidate = new ChannelAddress(httpLocation, transportCandidate);

        Entry<ChannelAddress, HttpServerChannel> httpBinding = httpBindings.floorEntry(candidate);

        if (httpBinding == null) {
            HttpResponse httpResponse = new DefaultHttpResponse(version, NOT_FOUND);
            ChannelFuture future = future(ctx.getChannel());
            write(ctx, future, httpResponse);
            return;
        }

        HttpServerChannel parent = httpBinding.getValue();
        ChannelFactory factory = parent.getFactory();
        ChannelConfig config = parent.getConfig();
        ChannelPipelineFactory pipelineFactory = config.getPipelineFactory();
        ChannelPipeline pipeline = pipelineFactory.getPipeline();
        ChannelAddress httpLocalAddress = parent.getLocalAddress();

        Channel transport = ctx.getChannel();
        ChannelAddress remoteAddress = remoteAddress(transport);
        ChannelAddress httpRemoteAddress = new ChannelAddress(httpLocation, remoteAddress, true);

        HttpChildChannelSink sink = new HttpChildChannelSink(transport);
        HttpChildChannel httpChildChannel = new HttpChildChannel(parent, factory, pipeline, sink);
        HttpChannelConfig httpChildConfig = httpChildChannel.getConfig();
        httpChildConfig.setMethod(httpRequest.getMethod());
        httpChildConfig.setVersion(version);
        httpChildConfig.getReadHeaders().set(httpRequest.headers());
        httpChildConfig.setReadQuery(new QueryStringDecoder(httpRequest.getUri()));
        httpChildConfig.setWriteQuery(new QueryStringEncoder(httpRequest.getUri()));
        httpChildConfig.setStatus(HttpResponseStatus.OK);

        transport.getConfig().setBufferFactory(httpChildConfig.getBufferFactory());
        this.httpChildChannel = httpChildChannel;

        detectWriteTransportClosed(transport, httpChildChannel);

        ChannelBuffer content = httpRequest.getContent();

        // update read state before firing channel events
        if (isTransferEncodingChunked(httpRequest)) {
            httpChildChannel.readState(HttpReadState.CONTENT_CHUNKED);
        }
        else if (isContentLengthSet(httpRequest)) {
            long contentLength = getContentLength(httpRequest);
            contentLength -= content.readableBytes();
            if (contentLength > 0) {
                httpChildChannel.readState(HttpReadState.CONTENT_CHUNKED);
            }
            else {
                httpChildChannel.readState(HttpReadState.CONTENT_COMPLETE);
            }
        }
        else {
            // see RFC-7230 section 3.3
            // content indicated by presence of Content-Length or Transfer-Encoding
            httpChildChannel.readState(HttpReadState.CONTENT_COMPLETE);
        }

        fireChannelOpen(httpChildChannel);

        httpChildChannel.setLocalAddress(httpLocalAddress);
        httpChildChannel.setBound();
        fireChannelBound(httpChildChannel, httpLocalAddress);

        httpChildChannel.setRemoteAddress(httpRemoteAddress);
        httpChildChannel.setConnected();
        fireChannelConnected(httpChildChannel, httpRemoteAddress);

        if (content.readable()) {
            fireMessageReceived(httpChildChannel, content);
        }

        // note: status may be set in reaction to one of the above events, such as CONNECTED
        //       so defer status code check until this point
        if (httpChildConfig.getStatus().getCode() == SWITCHING_PROTOCOLS.getCode()) {
            httpChildChannel.readState(HttpReadState.UPGRADED);
        }

        switch (httpChildChannel.readState()) {
        case CONTENT_COMPLETE:
            fireInputShutdown(httpChildChannel);
            this.httpChildChannel = null;
            if (httpChildChannel.setReadClosed()) {
                fireChannelDisconnected(httpChildChannel);
                fireChannelUnbound(httpChildChannel);
                fireChannelClosed(httpChildChannel);
            }
            break;
        default:
            break;
        }
    }

    @Override
    protected void httpMessageReceived(ChannelHandlerContext ctx, MessageEvent e, HttpChunk chunk) throws Exception {
        ChannelBuffer content = chunk.getContent();
        if (content.readable()) {
            fireMessageReceived(httpChildChannel, content);
        }

        boolean last = chunk.isLast();
        if (last) {
            HttpHeaders trailingHeaders = ((HttpChunkTrailer) chunk).trailingHeaders();
            httpChildChannel.getConfig().getReadTrailers().add(trailingHeaders);
            HttpChildChannel httpChildChannel = this.httpChildChannel;
            httpChildChannel.readState(HttpReadState.CONTENT_COMPLETE);
            this.httpChildChannel = null;
            fireInputShutdown(httpChildChannel);
            if (httpChildChannel.setReadClosed()) {
                fireChannelDisconnected(httpChildChannel);
                fireChannelUnbound(httpChildChannel);
                fireChannelClosed(httpChildChannel);
            }
        }
    }

    @Override
    protected void httpMessageReceived(ChannelHandlerContext ctx, MessageEvent e, ChannelBuffer message) throws Exception {
        if (message.readable()) {
            // after 101 switching protocols
            fireMessageReceived(httpChildChannel, message);
        }
    }

    private static URI getEffectiveURI(HttpRequest httpRequest) {
        URI requestURI = URI.create(httpRequest.getUri());
        if (requestURI.isAbsolute()) {
            return requestURI;
        }

        String host = getHost(httpRequest);
        return (host != null) ? URI.create(format("http://%s%s", host, requestURI)) : null;
    }

    private void detectWriteTransportClosed(Channel transport, HttpChildChannel httpChildChannel) {

        Objects.requireNonNull(httpChildChannel);

        final ChannelFutureListener closeListener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                switch (httpChildChannel.writeState()) {
                case UPGRADED:
                case CONTENT_CLOSING:
                    if (httpChildChannel.setWriteClosed()) {
                        fireChannelDisconnected(httpChildChannel);
                        fireChannelUnbound(httpChildChannel);
                        fireChannelClosed(httpChildChannel);
                    }
                    break;
                case CONTENT_COMPLETE:
                    break;
                default:
                    if (httpChildChannel.setReadAborted()) {
                        fireInputAborted(httpChildChannel);
                        httpChildChannel.setClosed();
                    }
                    break;
                }
            }
        };

        transport.getCloseFuture().addListener(closeListener);

        httpChildChannel.getCloseFuture().addListener(new ChannelFutureListener()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                transport.getCloseFuture().removeListener(closeListener);
            }
        });
    }
}
