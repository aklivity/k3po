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
package io.aklivity.k3po.runtime.driver.internal.netty.channel.udp;

import org.jboss.netty.channel.ChannelException;

import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddress;
import io.aklivity.k3po.runtime.driver.internal.netty.channel.ChannelAddressFactorySpi;

import java.net.URI;
import java.util.Map;

public class UdpChannelAddressFactorySpi extends ChannelAddressFactorySpi {

    @Override
    public String getSchemeName() {
        return "udp";
    }

    @Override
    protected ChannelAddress newChannelAddress0(URI location, ChannelAddress transport, Map<String, Object> options) {

        String host = location.getHost();
        int port = location.getPort();
        String path = location.getPath();

        if (host == null) {
            throw new ChannelException(String.format("%s host missing", getSchemeName()));
        }

        if (port == -1) {
            throw new ChannelException(String.format("%s port missing", getSchemeName()));
        }

        if (path != null && !path.isEmpty()) {
            throw new ChannelException(String.format("%s path \"%s\" unexpected", getSchemeName(), path));
        }

        Long timeout = (Long) options.get("timeout");
        long timeoutValue = timeout == null ? 0 : timeout;

        return new UdpChannelAddress(location, transport, timeoutValue);
    }

}
