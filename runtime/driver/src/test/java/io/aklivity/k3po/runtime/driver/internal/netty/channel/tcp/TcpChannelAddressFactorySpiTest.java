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
package io.aklivity.k3po.runtime.driver.internal.netty.channel.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;

import org.junit.Test;

import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddress;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddressFactorySpi;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.tcp.TcpChannelAddressFactorySpi;

public class TcpChannelAddressFactorySpiTest {

    @Test
    public void shouldLoadServiceImplementation() {

        for (ChannelAddressFactorySpi channelAddressFactorySpi : ServiceLoader.load(ChannelAddressFactorySpi.class)) {
            if (channelAddressFactorySpi instanceof TcpChannelAddressFactorySpi) {
                return;
            }
        }

        fail();
    }

    @Test
    public void shouldCreateChannelAddressWithoutTransport() throws Exception {

        TcpChannelAddressFactorySpi channelAddressFactorySpi = new TcpChannelAddressFactorySpi();
        Map<String, Object> options = Collections.emptyMap();
        ChannelAddress channelAddress = channelAddressFactorySpi.newChannelAddress(URI.create("tcp://127.0.0.1:8000"),
                options);

        assertEquals(new ChannelAddress(URI.create("tcp://127.0.0.1:8000")), channelAddress);
    }
}
