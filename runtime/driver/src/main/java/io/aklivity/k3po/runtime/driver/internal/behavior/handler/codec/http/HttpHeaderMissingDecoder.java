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
package io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.http;

import static java.lang.String.format;

import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpHeaders;

import io.aklivity.k3po.runtime.driver.internal.behavior.ScriptProgressException;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.AbstractConfigDecoder;
import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.http.HttpChannelConfig;

public class HttpHeaderMissingDecoder extends AbstractConfigDecoder {

    private String name;

    public HttpHeaderMissingDecoder(String name) {
        this.name = name;
    }

    @Override
    public boolean decode(Channel channel) throws Exception {
        HttpChannelConfig httpConfig = (HttpChannelConfig) channel.getConfig();
        HttpHeaders headers = httpConfig.getReadHeaders();
        List<String> headerValues = headers.getAll(name);
        if (!headerValues.isEmpty()) {
            throw new ScriptProgressException(getRegionInfo(), format("HTTP header not missing: %s", name));
        }
        return true;
    }

    @Override
    public String toString() {
        return format("http:header %s missing", name);
    }

}
