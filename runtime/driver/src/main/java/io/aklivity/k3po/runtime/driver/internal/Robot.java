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
package io.aklivity.k3po.runtime.driver.internal;

import static io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.BootstrapFactory.newBootstrapFactory;
import static io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddressFactory.newChannelAddressFactory;
import static io.aklivity.k3po.runtime.lang.internal.RegionInfo.newSequential;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.channel.Channels.pipelineFactory;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import io.aklivity.k3po.runtime.driver.internal.behavior.Barrier;
import io.aklivity.k3po.runtime.driver.internal.behavior.Configuration;
import io.aklivity.k3po.runtime.driver.internal.behavior.ScriptProgress;
import io.aklivity.k3po.runtime.driver.internal.behavior.ScriptProgressException;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.CompletionHandler;
import io.aklivity.k3po.runtime.driver.internal.behavior.parser.Parser;
import io.aklivity.k3po.runtime.driver.internal.behavior.parser.ScriptValidator;
import io.aklivity.k3po.runtime.driver.internal.behavior.visitor.GenerateConfigurationVisitor;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.BootstrapFactory;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.ClientBootstrap;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.ServerBootstrap;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.udp.UdpServerChannel;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddressFactory;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.CompositeChannelFuture;
import io.aklivity.k3po.runtime.driver.internal.resolver.ClientBootstrapResolver;
import io.aklivity.k3po.runtime.driver.internal.resolver.ServerBootstrapResolver;
import io.aklivity.k3po.runtime.lang.internal.RegionInfo;
import io.aklivity.k3po.runtime.lang.internal.ast.AstScriptNode;
import io.aklivity.k3po.runtime.lang.internal.parser.ScriptParser;

public class Robot {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(Robot.class);

    private final List<ChannelFuture> bindFutures = new ArrayList<>();
    private final List<ChannelFuture> connectFutures = new ArrayList<>();

    private final Channel channel = new DefaultLocalClientChannelFactory().newChannel(pipeline(new SimpleChannelHandler()));
    private final ChannelFuture startedFuture = Channels.future(channel);
    private final ChannelFuture abortedFuture = Channels.future(channel);
    private final ChannelFuture finishedFuture = Channels.future(channel);
    private final ChannelFuture disposedFuture = Channels.future(channel);

    private final DefaultChannelGroup closeableChannels = new DefaultChannelGroup();

    private Configuration configuration;
    private ChannelFuture preparedFuture;

    private final ChannelAddressFactory addressFactory;
    private final BootstrapFactory bootstrapFactory;

    private ScriptProgress progress;

    private final ChannelHandler closeOnExceptionHandler = new CloseOnExceptionHandler();

    private final ConcurrentMap<String, Barrier> barriersByName = new ConcurrentHashMap<String, Barrier>();

    public Robot() {
        this.addressFactory = newChannelAddressFactory();
        this.bootstrapFactory =
                newBootstrapFactory(Collections.<Class<?>, Object>singletonMap(ChannelAddressFactory.class, addressFactory));

        ChannelFutureListener stopConfigurationListener = createStopConfigurationListener();
        this.abortedFuture.addListener(stopConfigurationListener);
        this.finishedFuture.addListener(stopConfigurationListener);
    }

    public ChannelFuture getPreparedFuture() {
        return preparedFuture;
    }

    public ChannelFuture getStartedFuture() {
        return startedFuture;
    }

    public ChannelFuture getDisposedFuture() {
        return disposedFuture;
    }

    public ChannelFuture prepare(String expectedScript) throws Exception {

        if (preparedFuture != null) {
            throw new IllegalStateException("Script already prepared");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Expected script:\n" + expectedScript);
        }

        final ScriptParser parser = new Parser();
        AstScriptNode scriptAST = parser.parse(expectedScript);

        final ScriptValidator validator = new ScriptValidator();
        validator.validate(scriptAST);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Parsed script:\n" + scriptAST);
        }

        RegionInfo scriptInfo = scriptAST.getRegionInfo();
        progress = new ScriptProgress(scriptInfo, expectedScript);

        final GenerateConfigurationVisitor visitor = new GenerateConfigurationVisitor(bootstrapFactory, addressFactory);
        configuration = scriptAST.accept(visitor, new GenerateConfigurationVisitor.State(barriersByName));

        preparedFuture = prepareConfiguration();

        return preparedFuture;
    }

    // ONLY used for testing, TODO, remove and use TestSpecification instead
    ChannelFuture prepareAndStart(String script) throws Exception {
        ChannelFuture preparedFuture = prepare(script);
        preparedFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                start();
            }
        });
        return startedFuture;
    }

    public ChannelFuture start() throws Exception {

        if (preparedFuture == null || !preparedFuture.isDone()) {
            throw new IllegalStateException("Script has not been prepared or is still preparing");
        } else if (startedFuture.isDone()) {
            throw new IllegalStateException("Script has already been started");
        }

        // ensure prepare has completed before start can progress
        preparedFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                try {
                    startConfiguration();
                    startedFuture.setSuccess();
                } catch (Exception ex) {
                    startedFuture.setFailure(ex);
                }
            }
        });

        return startedFuture;
    }

    public ChannelFuture abort() {

        abortedFuture.setSuccess();

        return finishedFuture;
    }

    public ChannelFuture finish() {

        return finishedFuture;
    }

    public String getObservedScript() {
        return (progress != null) ? progress.getObservedScript() : null;
    }

    public ChannelFuture dispose() {
        if (preparedFuture == null) {
            // no need to clean up if never started
            
            // except the bootstrap factory
            bootstrapFactory.shutdown();
            bootstrapFactory.releaseExternalResources();

            disposedFuture.setSuccess();
        } else if (!disposedFuture.isDone()) {
            ChannelFuture future = abort();
            future.addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {

                    // avoid I/O deadlock checker
                    new Thread(new Runnable() {
                        public void run() {
                            closeableChannels.close().awaitUninterruptibly(30, SECONDS);
                            try {
                                bootstrapFactory.shutdown();
                                bootstrapFactory.releaseExternalResources();

                                for (AutoCloseable resource : configuration.getResources()) {
                                    try {
                                        resource.close();
                                    } catch (Exception e) {
                                        // ignore
                                    }
                                }

                                disposedFuture.setSuccess();
                            } catch (Exception e) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.error("Caught exception releasing resources", e);
                                }
                                disposedFuture.setFailure(e);
                            }
                        }
                    }).start();
                }
            });
        }
        return disposedFuture;
    }

    private ChannelFuture prepareConfiguration() throws Exception {

        List<ChannelFuture> completionFutures = new ArrayList<>();
        ChannelFutureListener streamCompletionListener = createStreamCompletionListener();
        for (ChannelPipeline pipeline : configuration.getClientAndServerPipelines()) {
            CompletionHandler completionHandler = pipeline.get(CompletionHandler.class);
            ChannelFuture completionFuture = completionHandler.getHandlerFuture();
            completionFutures.add(completionFuture);
            completionFuture.addListener(streamCompletionListener);
        }

        ChannelFuture executionFuture = new CompositeChannelFuture<>(channel, completionFutures);
        ChannelFutureListener executionListener = createScriptCompletionListener();
        executionFuture.addListener(executionListener);

        return prepareServers();
    }

    private ChannelFuture prepareServers() throws Exception {

        /* Accept's ... Robot acting as a server */
        for (ServerBootstrapResolver serverResolver : configuration.getServerResolvers()) {
            ServerBootstrap server = serverResolver.resolve();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Binding to address " + server.getOption("localAddress"));
            }

            /* Keep track of the client channels */
            server.setParentHandler(new SimpleChannelHandler() {

                @Override
                public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
                {
                    super.channelBound(ctx, e);

                    unbindLastStreamIfNotUdp(serverResolver, e.getChannel());
                }

                @Override
                public void childChannelOpen(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
                    closeableChannels.add(e.getChildChannel());

                    unbindLastStreamIfNotUdp(serverResolver, e.getChannel());
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
                    Channel channel = ctx.getChannel();
                    channel.close();
                }

                private void unbindLastStreamIfNotUdp(ServerBootstrapResolver serverResolver, Channel server)
                {
                    if (!serverResolver.canAccept() && !(server instanceof UdpServerChannel)) {
                        server.unbind();
                    }
                }
            });

            // Bind Asynchronously
            ChannelFuture bindFuture = server.bindAsync();

            // Add to out serverChannel Group
            closeableChannels.add(bindFuture.getChannel());

            // Add to our list of bindFutures so we can cancel them later on a possible abort
            bindFutures.add(bindFuture);

            // Listen for the bindFuture.
            RegionInfo regionInfo = (RegionInfo) server.getOption("regionInfo");
            bindFuture.addListener(createBindCompleteListener(regionInfo, serverResolver.getNotifyBarrier()));
        }

        return new CompositeChannelFuture<>(channel, bindFutures);
    }

    private void startConfiguration() throws Exception {
        /* Connect to any clients */
        for (final ClientBootstrapResolver clientResolver : configuration.getClientResolvers()) {
            Barrier awaitBarrier = clientResolver.getAwaitBarrier();
            if (awaitBarrier != null) {
                awaitBarrier.getFuture().addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        connectClient(clientResolver);
                    }
                });
            } else {
                connectClient(clientResolver);
            }
        }
    }

    private void connectClient(ClientBootstrapResolver clientResolver) throws Exception {
        ClientBootstrap client = clientResolver.resolve();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[id:           ] connect " + client.getOption("remoteAddress"));
        }

        ChannelFuture connectFuture = client.connect();
        connectFutures.add(connectFuture);
        connectFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    closeableChannels.add(connectFuture.getChannel());
                }
            }
        });
    }

    private void stopConfiguration() throws Exception {

        if (configuration == null) {
            // abort received but script not prepared, therefore entire script failed
            if (progress == null) {
                progress = new ScriptProgress(newSequential(0, 0), "");
            }
            RegionInfo scriptInfo = progress.getScriptInfo();
            progress.addScriptFailure(scriptInfo);
        } else {
            // stopping the configuration will implicitly trigger the script complete listener
            // to handle incomplete script that is being aborted by canceling the finish future

            // clear out the pipelines for new connections to avoid impacting the observed script
            for (ServerBootstrapResolver serverResolver : configuration.getServerResolvers()) {
                try {
                    ServerBootstrap server = serverResolver.resolve();
                    server.setPipelineFactory(pipelineFactory(pipeline(closeOnExceptionHandler)));
                } catch (RuntimeException e) {
                    LOGGER.warn("Exception caught while trying to stop server pipelies", e);
                }
            }
            for (ClientBootstrapResolver clientResolver : configuration.getClientResolvers()) {
                try {
                    ClientBootstrap client = clientResolver.resolve();
                    client.setPipelineFactory(pipelineFactory(pipeline(closeOnExceptionHandler)));
                } catch (RuntimeException e) {
                    LOGGER.warn("Exception caught while trying to stop client pipelies", e);
                }
            }

            // remove each handler from the configuration pipelines
            // this will trigger failures for any handlers on a pipeline for an incomplete stream
            // including pipelines not yet associated with any channel
            for (ChannelPipeline pipeline : configuration.getClientAndServerPipelines()) {
                stopStream(pipeline);
            }

            // cancel any pending binds and connects
            for (ChannelFuture bindFuture : bindFutures) {
                bindFuture.cancel();
            }

            for (ChannelFuture connectFuture : connectFutures) {
                if (connectFuture.cancel()) {
                    LOGGER.debug("Cancelled connect future: " + connectFuture.getChannel().getRemoteAddress());
                }
            }
        }
    }

    private void stopStream(final ChannelPipeline pipeline) {
        if (pipeline.isAttached()) {

            // avoid race between pipeline clean up and channel events on same pipeline
            // by executing the pipeline clean up on the I/O worker thread
            pipeline.execute(new Runnable() {
                @Override
                public void run() {
                    stopStreamAligned(pipeline);
                }

            });
        } else {
            // no race if not attached
            stopStreamAligned(pipeline);
        }
    }

    private void stopStreamAligned(final ChannelPipeline pipeline) {

        LOGGER.debug("Stopping pipeline");

        for (ChannelHandler handler : pipeline.toMap().values()) {

            if (LOGGER.isDebugEnabled()) {
                Channel channel = pipeline.getChannel();
                int id = (channel != null) ? channel.getId() : 0;
                LOGGER.debug(format("[id: 0x%08x] %s", id, handler));
            }

            // note: removing this handler can trigger script completion
            // which in turn can re-attempt to stop this pipeline
            pipeline.remove(handler);
        }

        // non-empty pipeline required to avoid warnings
        if (pipeline.getContext(closeOnExceptionHandler) == null) {
            pipeline.addLast("closeOnException", closeOnExceptionHandler);
        }
    }

    private ChannelFutureListener createBindCompleteListener(final RegionInfo regionInfo, final Barrier notifyBarrier) {
        return new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture bindFuture) throws Exception {

                Channel boundChannel = bindFuture.getChannel();
                SocketAddress localAddress = boundChannel.getLocalAddress();
                if (bindFuture.isSuccess()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Successfully bound to " + localAddress);
                    }

                    if (notifyBarrier != null) {
                        ChannelFuture barrierFuture = notifyBarrier.getFuture();
                        barrierFuture.setSuccess();
                    }
                } else {
                    Throwable cause = bindFuture.getCause();
                    String message = format("accept failed: %s", cause.getMessage());
                    progress.addScriptFailure(regionInfo, message);

                    // fail each pipeline that required this bind to succeed
                    List<ChannelPipeline> acceptedPipelines = configuration.getServerPipelines(regionInfo);
                    for (ChannelPipeline acceptedPipeline : acceptedPipelines) {
                        stopStream(acceptedPipeline);
                    }
                }

            }
        };
    }

    private ChannelFutureListener createStreamCompletionListener() {
        return new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture completionFuture) throws Exception {
                if (!completionFuture.isSuccess()) {
                    Throwable cause = completionFuture.getCause();
                    if (cause instanceof ScriptProgressException) {
                        ScriptProgressException exception = (ScriptProgressException) cause;
                        progress.addScriptFailure(exception.getRegionInfo(), exception.getMessage());
                    } else {
                        LOGGER.warn("Unexpected exception", cause);
                    }
                }
            }
        };
    }

    private ChannelFutureListener createScriptCompletionListener() {
        ChannelFutureListener executionListener = new ChannelFutureListener() {

            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {

                if (LOGGER.isDebugEnabled()) {
                    // detect observed script
                    String observedScript = progress.getObservedScript();
                    LOGGER.debug("Observed script:\n" + observedScript);
                }

                if (abortedFuture.isDone()) {
                    // abort complete, trigger finished future
                    finishedFuture.setSuccess();
                } else {
                    // execution complete, trigger finished future
                    finishedFuture.setSuccess();
                }
            }

        };

        return executionListener;
    }

    private ChannelFutureListener createStopConfigurationListener() {
        return new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                stopConfiguration();
            }
        };
    }

    @Sharable
    private static final class CloseOnExceptionHandler extends SimpleChannelHandler {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            // avoid stack overflow when exception happens on close
            if (TRUE != ctx.getAttachment()) {
                ctx.setAttachment(TRUE);
                // close channel and avoid warning logged by default exceptionCaught implementation
                Channel channel = ctx.getChannel();
                channel.close();
            } else {
                // log exception during close
                super.exceptionCaught(ctx, e);
            }
        }

        @Override
        public String toString() {
            return "close-on-exception";
        }
    }

    public Map<String, Barrier> getBarriersByName() {
        return barriersByName;
    }

    public void notifyBarrier(String barrierName) throws Exception {
        final Barrier barrier = barriersByName.get(barrierName);
        if (barrier == null) {
            throw new Exception("Can not notify a barrier that does not exist in the script: " + barrierName);
        }
        barrier.getFuture().setSuccess();
    }

    public ChannelFuture awaitBarrier(String barrierName) throws Exception {
        final Barrier barrier = barriersByName.get(barrierName);
        if (barrier == null) {
            throw new Exception("Can not notify a barrier that does not exist in the script: " + barrierName);
        }
        return barrier.getFuture();
    }

}
