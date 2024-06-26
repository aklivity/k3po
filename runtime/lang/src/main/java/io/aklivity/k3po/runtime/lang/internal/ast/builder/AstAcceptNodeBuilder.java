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
package io.aklivity.k3po.runtime.lang.internal.ast.builder;

import java.net.URI;

import io.aklivity.k3po.runtime.lang.internal.ast.AstAcceptNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstScriptNode;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstValue;
import io.aklivity.k3po.runtime.lang.types.TypeInfo;

public final class AstAcceptNodeBuilder extends AbstractAstAcceptNodeBuilder<AstAcceptNode> {

    public AstAcceptNodeBuilder() {
        this(new AstAcceptNode());
    }

    public AstAcceptNodeBuilder setLocation(AstValue<URI> location) {
        node.setLocation(location);
        return this;
    }

    public AstAcceptNodeBuilder setAcceptName(String acceptName) {
        node.setAcceptName(acceptName);
        return this;
    }

    public <T> AstAcceptNodeBuilder setOption(TypeInfo<T> option, AstValue<T> optionValue) {
        node.getOptions().put(option.getName(), optionValue);
        return this;
    }

    public AstAcceptNodeBuilder setOption(String optionName, AstValue<?> optionValue) {
        node.getOptions().put(optionName, optionValue);
        return this;
    }

    @Override
    public AstOpenedNodeBuilder.StreamNested<AstAcceptNodeBuilder> addOpenedEvent() {
        return new AstOpenedNodeBuilder.StreamNested<>(this);
    }

    @Override
    public AstBoundNodeBuilder.StreamNested<AstAcceptNodeBuilder> addBoundEvent() {
        return new AstBoundNodeBuilder.StreamNested<>(this);
    }

    @Override
    public AstChildOpenedNodeBuilder.StreamNested<AstAcceptNodeBuilder> addChildOpenedEvent() {
        return new AstChildOpenedNodeBuilder.StreamNested<>(this);
    }

    @Override
    public AstChildClosedNodeBuilder.StreamNested<AstAcceptNodeBuilder> addChildClosedEvent() {
        return new AstChildClosedNodeBuilder.StreamNested<>(this);
    }

    @Override
    public AstUnboundNodeBuilder.StreamNested<AstAcceptNodeBuilder> addUnboundEvent() {
        return new AstUnboundNodeBuilder.StreamNested<>(this);
    }

    @Override
    public AstClosedNodeBuilder.StreamNested<AstAcceptNodeBuilder> addClosedEvent() {
        return new AstClosedNodeBuilder.StreamNested<>(this);
    }

    @Override
    public AstReadAwaitNodeBuilder.StreamNested<AstAcceptNodeBuilder> addReadAwaitBarrier() {
        return new AstReadAwaitNodeBuilder.StreamNested<>(this);
    }

    @Override
    public AstReadNotifyNodeBuilder.StreamNested<AstAcceptNodeBuilder> addReadNotifyBarrier() {
        return new AstReadNotifyNodeBuilder.StreamNested<>(this);
    }

    @Override
    public AstWriteAwaitNodeBuilder.StreamNested<AstAcceptNodeBuilder> addWriteAwaitBarrier() {
        return new AstWriteAwaitNodeBuilder.StreamNested<>(this);
    }

    @Override
    public AstWriteNotifyNodeBuilder.StreamNested<AstAcceptNodeBuilder> addWriteNotifyBarrier() {
        return new AstWriteNotifyNodeBuilder.StreamNested<>(this);
    }

    @Override
    public AstAcceptNode done() {
        return result;
    }

    private AstAcceptNodeBuilder(AstAcceptNode node) {
        super(node, node);
    }

    public static final class ScriptNested<R extends AbstractAstNodeBuilder<? extends AstScriptNode, ?>> extends
            AbstractAstAcceptNodeBuilder<R> {

        public ScriptNested(R builder) {
            super(new AstAcceptNode(), builder);
        }

        public ScriptNested<R> setLocation(AstValue<URI> location) {
            node.setLocation(location);
            return this;
        }

        public ScriptNested<R> setAcceptName(String acceptName) {
            node.setAcceptName(acceptName);
            return this;
        }

        public <T> ScriptNested<R> setOption(TypeInfo<T> option, AstValue<T> optionValue) {
            node.getOptions().put(option.getName(), optionValue);
            return this;
        }

        public <T> ScriptNested<R> setOption(String optionName, AstValue<?> optionValue) {
            node.getOptions().put(optionName, optionValue);
            return this;
        }

        public AstAcceptedNodeBuilder.AcceptNested<ScriptNested<R>> addAcceptedStream() {
            return new AstAcceptedNodeBuilder.AcceptNested<>(this);
        }

        @Override
        public AstOpenedNodeBuilder.StreamNested<ScriptNested<R>> addOpenedEvent() {
            return new AstOpenedNodeBuilder.StreamNested<>(this);
        }

        @Override
        public AstBoundNodeBuilder.StreamNested<ScriptNested<R>> addBoundEvent() {
            return new AstBoundNodeBuilder.StreamNested<>(this);
        }

        @Override
        public AstChildOpenedNodeBuilder.StreamNested<ScriptNested<R>> addChildOpenedEvent() {
            return new AstChildOpenedNodeBuilder.StreamNested<>(this);
        }

        @Override
        public AstChildClosedNodeBuilder.StreamNested<ScriptNested<R>> addChildClosedEvent() {
            return new AstChildClosedNodeBuilder.StreamNested<>(this);
        }

        @Override
        public AstUnboundNodeBuilder.StreamNested<ScriptNested<R>> addUnboundEvent() {
            return new AstUnboundNodeBuilder.StreamNested<>(this);
        }

        @Override
        public AstClosedNodeBuilder.StreamNested<ScriptNested<R>> addClosedEvent() {
            return new AstClosedNodeBuilder.StreamNested<>(this);
        }

        @Override
        public AstReadAwaitNodeBuilder.StreamNested<ScriptNested<R>> addReadAwaitBarrier() {
            return new AstReadAwaitNodeBuilder.StreamNested<>(this);
        }

        @Override
        public AstReadNotifyNodeBuilder.StreamNested<ScriptNested<R>> addReadNotifyBarrier() {
            return new AstReadNotifyNodeBuilder.StreamNested<>(this);
        }

        @Override
        public AstWriteAwaitNodeBuilder.StreamNested<ScriptNested<R>> addWriteAwaitBarrier() {
            return new AstWriteAwaitNodeBuilder.StreamNested<>(this);
        }

        @Override
        public AstWriteNotifyNodeBuilder.StreamNested<ScriptNested<R>> addWriteNotifyBarrier() {
            return new AstWriteNotifyNodeBuilder.StreamNested<>(this);
        }

        @Override
        public R done() {
            AstScriptNode scriptNode = result.node;
            scriptNode.getStreams().add(node);
            return result;
        }
    }

}
