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
package io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.channel;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jmock.Mockery;
import org.junit.Test;

import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.channel.DefaultServerChannelConfig;

public class DefaultServerChannelConfigTest {

    @Test
    public void shouldNotStoreDefaultChannelConfigOptionAsTransportOption() {
        Mockery context = new  Mockery();
        final ChannelPipelineFactory channelPipelineFactory = context.mock(ChannelPipelineFactory.class);
        Test1ChannelConfig config = new Test1ChannelConfig();
        config.setPipelineFactory(channelPipelineFactory);
        assertSame(channelPipelineFactory, config.getPipelineFactory());
        assertTrue(config.getTransportOptions().isEmpty());
    }

    @Test
    public void shouldNotStoreLocalOptionAsTransportOption() {
        Test2ChannelConfig config = new Test2ChannelConfig();
        final Object value = new Object();
        config.setOption("test2Option", value);
        assertSame(value, config.getTest2Option());
        assertTrue(config.getTransportOptions().isEmpty());
    }

    @Test
    public void shouldNotStoreInheritedOptionAsTransportOption() {
        Test2ChannelConfig config = new Test2ChannelConfig();
        final Object value = new Object();
        config.setOption("test1Option", value);
        assertSame(value, config.getTest1Option());
        assertTrue(config.getTransportOptions().isEmpty());
    }

    @Test
    public void shouldStoreAsTransportOption() {
        Test2ChannelConfig config = new Test2ChannelConfig();
        final Object value = new Object();
        config.setOption("transportOption", value);
        assertSame(value, config.getTransportOptions().get("transportOption"));
    }

    public class Test1ChannelConfig extends DefaultServerChannelConfig {
        private Object test1Option;

        @Override
        public boolean setOption0(String key, Object value) {
            if (super.setOption0(key, value)) {
                return true;
            }
            if (key.equals("test1Option")) {
                test1Option = value;
            } else {
                return false;
            }
            return true;
        }

        public Object getTest1Option() {
            return test1Option;
        }
    }

    public class Test2ChannelConfig extends Test1ChannelConfig {
        private Object test2Option;

        @Override
        public boolean setOption0(String key, Object value) {
            if (super.setOption0(key, value)) {
                return true;
            }
            if (key.equals("test2Option")) {
                test2Option = value;
            } else {
                return false;
            }
            return true;
        }

        public Object getTest2Option() {
            return test2Option;
        }
    }

}
