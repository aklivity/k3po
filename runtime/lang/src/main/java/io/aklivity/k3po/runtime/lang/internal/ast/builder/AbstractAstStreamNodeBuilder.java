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
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAbortNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAbortedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAwaitNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadClosedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadConfigNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadNotifyNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadOptionNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadValueNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstStreamNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstUnbindNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstUnboundNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAbortNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAbortedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAwaitNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteCloseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteConfigNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteFlushNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteNotifyNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteOptionNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteValueNode;

public abstract class AbstractAstStreamNodeBuilder<T extends AstStreamNode, R> extends AbstractAstNodeBuilder<T, R> {

    public AbstractAstStreamNodeBuilder(T node, R result) {
        super(node, result);
    }

    public abstract AbstractAstStreamableNodeBuilder<AstOpenedNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addOpenedEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstBoundNode, ? extends AbstractAstStreamNodeBuilder<T, R>> addBoundEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstConnectedNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addConnectedEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstReadValueNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addReadEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstDisconnectedNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addDisconnectedEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstUnboundNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addUnboundEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstClosedNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addClosedEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstWriteValueNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addWriteCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstDisconnectNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addDisconnectCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstUnbindNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addUnbindCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstCloseNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addCloseCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstWriteAbortNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addWriteAbortCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstReadAbortedNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addReadAbortedEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstReadAbortNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addReadAbortCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstWriteAbortedNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addWriteAbortedEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstReadAwaitNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addReadAwaitBarrier();

    public abstract AbstractAstStreamableNodeBuilder<AstReadNotifyNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addReadNotifyBarrier();

    public abstract AbstractAstStreamableNodeBuilder<AstWriteAwaitNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addWriteAwaitBarrier();

    public abstract AbstractAstStreamableNodeBuilder<AstWriteNotifyNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
            addWriteNotifyBarrier();

    public abstract AbstractAstStreamableNodeBuilder<AstReadConfigNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
        addReadConfigEvent();

    public abstract AbstractAstStreamableNodeBuilder<AstWriteConfigNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
        addWriteConfigCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstWriteFlushNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
        addFlushCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstReadClosedNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
        addReadCloseCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstWriteCloseNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
        addWriteCloseCommand();

    public abstract AbstractAstStreamableNodeBuilder<AstReadOptionNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
        addReadOption();

    public abstract AbstractAstStreamableNodeBuilder<AstWriteOptionNode, ? extends AbstractAstStreamNodeBuilder<T, R>>
        addWriteOption();
}
