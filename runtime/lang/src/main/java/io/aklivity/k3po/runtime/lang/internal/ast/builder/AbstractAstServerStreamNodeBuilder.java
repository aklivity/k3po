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
import io.aklivity.k3po.runtime.lang.internal.ast.AstChildClosedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstChildOpenedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstClosedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstOpenedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAwaitNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadNotifyNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstStreamNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstUnboundNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAwaitNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteNotifyNode;

public abstract class AbstractAstServerStreamNodeBuilder<T extends AstStreamNode, R> extends AbstractAstNodeBuilder<T, R> {

    public AbstractAstServerStreamNodeBuilder(T node, R result) {
        super(node, result);
    }

    public abstract AbstractAstNodeBuilder<AstOpenedNode, ? extends AbstractAstNodeBuilder<T, ?>> addOpenedEvent();

    public abstract AbstractAstNodeBuilder<AstBoundNode, ? extends AbstractAstNodeBuilder<T, ?>> addBoundEvent();

    public abstract AbstractAstNodeBuilder<AstChildOpenedNode, ? extends AbstractAstNodeBuilder<T, ?>> addChildOpenedEvent();

    public abstract AbstractAstNodeBuilder<AstChildClosedNode, ? extends AbstractAstNodeBuilder<T, ?>> addChildClosedEvent();

    public abstract AbstractAstNodeBuilder<AstUnboundNode, ? extends AbstractAstNodeBuilder<T, ?>> addUnboundEvent();

    public abstract AbstractAstNodeBuilder<AstClosedNode, ? extends AbstractAstNodeBuilder<T, ?>> addClosedEvent();

    public abstract AbstractAstNodeBuilder<AstReadAwaitNode, ? extends AbstractAstNodeBuilder<T, R>> addReadAwaitBarrier();

    public abstract AbstractAstNodeBuilder<AstReadNotifyNode, ? extends AbstractAstNodeBuilder<T, R>> addReadNotifyBarrier();

    public abstract AbstractAstNodeBuilder<AstWriteAwaitNode, ? extends AbstractAstNodeBuilder<T, R>> addWriteAwaitBarrier();

    public abstract AbstractAstNodeBuilder<AstWriteNotifyNode, ? extends AbstractAstNodeBuilder<T, R>> addWriteNotifyBarrier();
}
