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
package io.aklivity.k3po.runtime.driver.internal.resolver;

import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;

import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import io.aklivity.k3po.runtime.driver.internal.behavior.Barrier;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.BootstrapFactory;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.ClientBootstrap;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.ServerBootstrap;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddress;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddressFactory;
import io.aklivity.k3po.runtime.lang.internal.RegionInfo;

/**
 * The class is used to defer the initialization of {@link ServerBootstrap}.
 */
public class ClientBootstrapResolver {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(ClientBootstrapResolver.class);

    private final BootstrapFactory bootstrapFactory;
    private final ChannelAddressFactory addressFactory;
    private final ChannelPipelineFactory pipelineFactory;
    private final Supplier<URI> locationResolver;
    private final Barrier awaitBarrier;
    private final OptionsResolver optionsResolver;

    private ClientBootstrap bootstrap;

    public ClientBootstrapResolver(BootstrapFactory bootstrapFactory, ChannelAddressFactory addressFactory,
            ChannelPipelineFactory pipelineFactory, Supplier<URI> locationResolver,
            OptionsResolver optionsResolver, Barrier awaitBarrier) {
        this.bootstrapFactory = bootstrapFactory;
        this.addressFactory = addressFactory;
        this.pipelineFactory = pipelineFactory;
        this.locationResolver = locationResolver;
        this.optionsResolver = optionsResolver;
        this.awaitBarrier = awaitBarrier;
    }

    public ClientBootstrap resolve() throws Exception {
        if (bootstrap == null) {
            URI connectURI = locationResolver.get();
            Map<String, Object> connectOptions = optionsResolver.resolve();
            ChannelAddress remoteAddress = addressFactory.newChannelAddress(connectURI, connectOptions);
            LOGGER.debug("Initializing client Bootstrap connecting to remoteAddress " + remoteAddress);
            ClientBootstrap clientBootstrapCandidate = bootstrapFactory.newClientBootstrap(connectURI.getScheme());
            clientBootstrapCandidate.setPipelineFactory(pipelineFactory);
            clientBootstrapCandidate.setOptions(connectOptions);
            clientBootstrapCandidate.setOption("remoteAddress", remoteAddress);
            bootstrap = clientBootstrapCandidate;
        }
        return bootstrap;
    }

    public Barrier getAwaitBarrier() {
        return this.awaitBarrier;
    }
}
