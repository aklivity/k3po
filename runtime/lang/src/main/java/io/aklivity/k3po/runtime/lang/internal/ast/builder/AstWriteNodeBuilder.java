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

import javax.el.ValueExpression;

import io.aklivity.k3po.runtime.lang.internal.ast.AstStreamNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteValueNode;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstExpressionValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralBytesValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralIntegerValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralLongValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralShortValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralTextValue;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;

public class AstWriteNodeBuilder extends AbstractAstStreamableNodeBuilder<AstWriteValueNode, AstWriteValueNode> {

    public AstWriteNodeBuilder() {
        this(new AstWriteValueNode());
    }

    public AstWriteNodeBuilder addExactBytes(byte[] exactBytes) {
        node.addValue(new AstLiteralBytesValue(exactBytes));
        return this;
    }

    public AstWriteNodeBuilder addExactText(String exactText) {
        node.addValue(new AstLiteralTextValue(exactText));
        return this;
    }

    public AstWriteNodeBuilder addLong(Long value) {
        node.addValue(new AstLiteralLongValue(value));
        return this;
    }

    public AstWriteNodeBuilder addInteger(Integer value) {
        node.addValue(new AstLiteralIntegerValue(value));
        return this;
    }

    public AstWriteNodeBuilder addShort(Short value)
    {
        node.addValue(new AstLiteralShortValue(value));
        return this;
    }

    public AstWriteNodeBuilder addExpression(ValueExpression value, ExpressionContext environment) {
        node.addValue(new AstExpressionValue<>(value, environment));
        return this;
    }

    @Override
    public AstWriteValueNode done() {
        return result;
    }

    private AstWriteNodeBuilder(AstWriteValueNode node) {
        super(node, node);
    }

    public static class StreamNested<R extends AbstractAstNodeBuilder<? extends AstStreamNode, ?>> extends
            AbstractAstStreamableNodeBuilder<AstWriteValueNode, R> {

        public StreamNested(R builder) {
            super(new AstWriteValueNode(), builder);
        }

        public StreamNested<R> addExactBytes(byte[] exactBytes) {
            node.addValue(new AstLiteralBytesValue(exactBytes));
            return this;
        }

        public StreamNested<R> addExactText(String exactText) {
            node.addValue(new AstLiteralTextValue(exactText));
            return this;
        }

        public StreamNested<R> addLong(Long value) {
            node.addValue(new AstLiteralLongValue(value));
            return this;
        }

        public StreamNested<R> addInteger(Integer value) {
            node.addValue(new AstLiteralIntegerValue(value));
            return this;
        }

        public StreamNested<R> addShort(Short value)
        {
            node.addValue(new AstLiteralShortValue(value));
            return this;
        }

        public StreamNested<R> addExpression(ValueExpression value, ExpressionContext environment) {
            node.addValue(new AstExpressionValue<>(value, environment));
            return this;
        }

        @Override
        public R done() {
            AstStreamNode streamNode = result.node;
            streamNode.getStreamables().add(node);
            return result;
        }

    }
}
