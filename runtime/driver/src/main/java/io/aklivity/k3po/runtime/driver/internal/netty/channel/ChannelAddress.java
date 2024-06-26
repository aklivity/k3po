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
package io.aklivity.k3po.runtime.driver.internal.netty.channel;

import java.net.SocketAddress;
import java.net.URI;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;

import org.jboss.netty.channel.ChannelException;

public class ChannelAddress extends SocketAddress {

    private static final long serialVersionUID = 1L;

    public static final Comparator<ChannelAddress> ADDRESS_COMPARATOR = new Comparator<ChannelAddress>() {
        @Override
        public int compare(ChannelAddress o1, ChannelAddress o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }
            int status = o1.getLocation().compareTo(o2.getLocation());
            if (status == 0) {
                status = this.compare(o1.getTransport(), o2.getTransport());
            }
            return status;
        }
    };


    private final URI location;
    private final boolean ephemeral;

    private final ChannelAddress transport;

    public ChannelAddress(URI location) {
        this(location, null);
    }

    public ChannelAddress(URI location, boolean ephemeral) {
        this(location, null, ephemeral);
    }

    public ChannelAddress(URI location, ChannelAddress transport) {
        this(location, transport, false);
    }

    public ChannelAddress(URI location, ChannelAddress transport, boolean ephemeral) {
        if (location == null) {
            throw new NullPointerException("location");
        }

        this.location = location;
        this.transport = transport;
        this.ephemeral = ephemeral;
    }

    public URI getLocation() {
        return location;
    }

    public ChannelAddress getTransport() {
        return transport;
    }

    public ChannelAddress newEphemeralAddress() {
        return createEphemeralAddress(this::newEphemeralAddress);
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ChannelAddress)) {
            return false;
        }

        if (this == o) {
            return true;
        }

        ChannelAddress that = (ChannelAddress) o;
        if (this.ephemeral ^ that.ephemeral) {
            return false;
        }

        return Objects.equals(this.location, that.location) &&
                Objects.equals(this.transport, that.transport);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(location);
        if (transport != null) {
            sb.append(" @ ").append(transport);
        }
        return sb.toString();
    }

    protected boolean isEphemeral() {
        return ephemeral;
    }

    protected final <T extends ChannelAddress> T createEphemeralAddress(BiFunction<URI, ChannelAddress, T> factory) {
        if (ephemeral) {
            throw new ChannelException("Channel address is already ephemeral");
        }

        T channel = factory.apply(location, transport);

        if (!channel.isEphemeral()) {
            throw new ChannelException("Ephemeral channel address is not ephemeral");
        }

        return channel;
    }

    private ChannelAddress newEphemeralAddress(URI location, ChannelAddress transport) {
        return new ChannelAddress(location, transport, true);
    }
}
