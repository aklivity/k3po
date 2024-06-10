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
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteConfigNode;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstExpressionValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralBytesValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralTextValue;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;
import io.aklivity.k3po.runtime.lang.types.StructuredTypeInfo;

public class AstWriteConfigNodeBuilder extends AbstractAstStreamableNodeBuilder<AstWriteConfigNode, AstWriteConfigNode> {

    public AstWriteConfigNodeBuilder() {
        this(new AstWriteConfigNode());
    }

    private AstWriteConfigNodeBuilder(AstWriteConfigNode node) {
        super(node, node);
    }

    @Override
    public AstWriteConfigNode done() {
        return result;
    }

    public AstWriteConfigNodeBuilder setType(StructuredTypeInfo type) {
        node.setType(type);
        return this;
    }

    public AstWriteConfigNodeBuilder setValue(String name, String value) {
        node.setValue(name, new AstLiteralTextValue(value));
        return this;
    }

    public AstWriteConfigNodeBuilder setValue(String name, byte[] value) {
        node.setValue(name, new AstLiteralBytesValue(value));
        return this;
    }

    public AstWriteConfigNodeBuilder setValue(String name, ValueExpression value, ExpressionContext environment) {
        node.setValue(name, new AstExpressionValue<>(value, environment));
        return this;
    }

    public AstWriteConfigNodeBuilder addValue(String value) {
        node.addValue(new AstLiteralTextValue(value));
        return this;
    }

    public AstWriteConfigNodeBuilder addValue(byte[] value) {
        node.addValue(new AstLiteralBytesValue(value));
        return this;
    }

    public AstWriteConfigNodeBuilder addValue(ValueExpression value, ExpressionContext environment) {
        node.addValue(new AstExpressionValue<>(value, environment));
        return this;
    }

    public static class StreamNested<R extends AbstractAstNodeBuilder<? extends AstStreamNode, ?>> extends
            AbstractAstStreamableNodeBuilder<AstWriteConfigNode, R> {

        public StreamNested(R builder) {
            super(new AstWriteConfigNode(), builder);
        }

        public StreamNested<R> setType(StructuredTypeInfo type) {
            node.setType(type);
            return this;
        }

        public StreamNested<R> setValue(String name, String value) {
            node.setValue(name, new AstLiteralTextValue(value));
            return this;
        }

        public StreamNested<R> setValue(String name, byte[] value) {
            node.setValue(name, new AstLiteralBytesValue(value));
            return this;
        }

        public StreamNested<R> setValue(String name, ValueExpression value, ExpressionContext environment) {
            node.setValue(name, new AstExpressionValue<>(value, environment));
            return this;
        }

        public StreamNested<R> addValue(String value) {
            node.addValue(new AstLiteralTextValue(value));
            return this;
        }

        public StreamNested<R> addValue(byte[] valueBytes) {
            node.addValue(new AstLiteralBytesValue(valueBytes));
            return this;
        }

        public StreamNested<R> addValue(ValueExpression value, ExpressionContext environment) {
            node.addValue(new AstExpressionValue<>(value, environment));
            return this;
        }

        @Override
        public R done() {
            AstStreamNode streamNode = node(result);
            streamNode.getStreamables().add(node);
            return result;
        }
    }
}
