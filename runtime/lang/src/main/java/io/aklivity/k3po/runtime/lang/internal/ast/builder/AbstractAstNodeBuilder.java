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

import io.aklivity.k3po.runtime.lang.internal.ast.AstNode;

public abstract class AbstractAstNodeBuilder<N extends AstNode, R> {

    protected final N node;
    protected final R result;

    protected AbstractAstNodeBuilder(N node, R result) {
        this.node = node;
        this.result = result;
    }

    public abstract R done();

    protected <T extends AstNode, B extends AbstractAstNodeBuilder<? extends T, ?>> T node(B builder) {
        return builder.node;
    }
}
