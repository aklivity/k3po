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
package io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec;

import org.jboss.netty.buffer.ChannelBuffer;

public abstract class Masker {

    public static final Masker IDENTITY_MASKER = new Masker() {

        @Override
        public ChannelBuffer applyMask(ChannelBuffer buffer) {
            return buffer;
        }

        @Override
        public ChannelBuffer undoMask(ChannelBuffer buffer) {
            return buffer;
        }

        @Override
        public String toString() {
            return "IDENTITY_MASKER";
        }

    };

    public abstract ChannelBuffer applyMask(ChannelBuffer buffer);

    public abstract ChannelBuffer undoMask(ChannelBuffer buffer);
}
