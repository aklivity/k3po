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
package io.aklivity.k3po.runtime.driver.internal.netty.bootstrap;

import static io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.BootstrapFactory.newBootstrapFactory;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.BootstrapFactory;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.ClientBootstrap;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddress;

public class ClientBootstrapRule implements TestRule {

    private final String transportName;
    private final BootstrapFactory bootstrapFactory;

    private ClientBootstrap bootstrap;

    public ClientBootstrapRule(String transportName) {
        this.transportName = transportName;
        this.bootstrapFactory = newBootstrapFactory();
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                bootstrap = bootstrapFactory.newClientBootstrap(transportName);
                try {
                    base.evaluate();
                } finally {
                    bootstrapFactory.releaseExternalResources();
                }
            }
        };
    }

    public ChannelFuture connect(ChannelAddress remoteAddress) {
        return bootstrap.connect(remoteAddress);
    }

    public void setPipeline(ChannelPipeline pipeline) {
        bootstrap.setPipeline(pipeline);
    }

    public void setOption(String key, Object value) {
        bootstrap.setOption(key, value);
    }

    public void getOption(String key) {
        bootstrap.getOption(key);
    }

    public void shutdown() {
        bootstrapFactory.shutdown();
    }

}
