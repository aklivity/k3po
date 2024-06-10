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

import io.aklivity.k3po.runtime.lang.internal.ast.AstBoundNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstCloseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstClosedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstDisconnectNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstDisconnectedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstOpenedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadValueNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstScriptNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstStreamNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstUnbindNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstUnboundNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteValueNode;

public abstract class AstStreamNodeBuilder<T extends AstStreamNode> extends
        AbstractAstStreamNodeBuilder<T, AstScriptNodeBuilder> {

    public AstStreamNodeBuilder(T node, AstScriptNodeBuilder builder) {
        super(node, builder);
    }

    public abstract AbstractAstStreamableNodeBuilder<AstOpenedNode, ? extends AstStreamNodeBuilder<T>> addOpenedEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstBoundNode, ? extends AstStreamNodeBuilder<T>> addBoundEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstConnectedNode, ? extends AstStreamNodeBuilder<T>> addConnectedEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstReadValueNode, ? extends AstStreamNodeBuilder<T>> addReadEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstDisconnectedNode, ? extends AstStreamNodeBuilder<T>>
            addDisconnectedEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstUnboundNode, ? extends AstStreamNodeBuilder<T>> addUnboundEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstClosedNode, ? extends AstStreamNodeBuilder<T>> addClosedEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstWriteValueNode, ? extends AstStreamNodeBuilder<T>> addWriteCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstDisconnectNode, ? extends AstStreamNodeBuilder<T>>
            addDisconnectCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstUnbindNode, ? extends AstStreamNodeBuilder<T>> addUnbindCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstCloseNode, ? extends AstStreamNodeBuilder<T>> addCloseCommand();

    @Override
    public final AstScriptNodeBuilder done() {
        AstScriptNode scriptNode = result.node;
        scriptNode.getStreams().add(node);
        return result;
    }
}
