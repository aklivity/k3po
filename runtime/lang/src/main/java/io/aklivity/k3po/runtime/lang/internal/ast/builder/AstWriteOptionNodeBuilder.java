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

import javax.el.ValueExpression;

import io.aklivity.k3po.runtime.lang.internal.ast.AstStreamNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteOptionNode;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstExpressionValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralBytesValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralIntegerValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralLongValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralTextValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralURIValue;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;
import io.aklivity.k3po.runtime.lang.types.TypeInfo;

public class AstWriteOptionNodeBuilder extends AbstractAstStreamableNodeBuilder<AstWriteOptionNode, AstWriteOptionNode> {

    public AstWriteOptionNodeBuilder() {
        this(new AstWriteOptionNode());
    }

    public AstWriteOptionNodeBuilder setOptionType(TypeInfo<?> optionType) {
        node.setOptionType(optionType);
        return this;
    }

    public AstWriteOptionNodeBuilder setOptionName(String optionName) {
        node.setOptionName(optionName);
        return this;
    }

    public AstWriteOptionNodeBuilder setOptionValue(URI optionValue) {
        node.setOptionValue(new AstLiteralURIValue(optionValue));
        return this;
    }

    public AstWriteOptionNodeBuilder setOptionValue(String optionValue) {
        node.setOptionValue(new AstLiteralTextValue(optionValue));
        return this;
    }

    public AstWriteOptionNodeBuilder setOptionValue(byte[] optionValue) {
        node.setOptionValue(new AstLiteralBytesValue(optionValue));
        return this;
    }

    public AstWriteOptionNodeBuilder setOptionValue(int optionValue) {
        node.setOptionValue(new AstLiteralIntegerValue(optionValue));
        return this;
    }

    public AstWriteOptionNodeBuilder setOptionValue(long optionValue) {
        node.setOptionValue(new AstLiteralLongValue(optionValue));
        return this;
    }

    public AstWriteOptionNodeBuilder setOptionValue(ValueExpression expression, ExpressionContext environment) {
        node.setOptionValue(new AstExpressionValue<>(expression, environment));
        return this;
    }

    @Override
    public AstWriteOptionNode done() {
        return result;
    }

    private AstWriteOptionNodeBuilder(AstWriteOptionNode node) {
        super(node, node);
    }

    public static class StreamNested<R extends AbstractAstNodeBuilder<? extends AstStreamNode, ?>> extends
            AbstractAstStreamableNodeBuilder<AstWriteOptionNode, R> {

        public StreamNested(R builder) {
            super(new AstWriteOptionNode(), builder);
        }

        public StreamNested<R> setOptionType(TypeInfo<?> optionType) {
            node.setOptionType(optionType);
            return this;
        }

        public StreamNested<R> setOptionName(String optionName) {
            node.setOptionName(optionName);
            return this;
        }

        public StreamNested<R> setOptionValue(URI optionValue) {
            node.setOptionValue(new AstLiteralURIValue(optionValue));
            return this;
        }

        public StreamNested<R> setOptionValue(String optionValue) {
            node.setOptionValue(new AstLiteralTextValue(optionValue));
            return this;
        }

        public StreamNested<R> setOptionValue(byte[] optionValue) {
            node.setOptionValue(new AstLiteralBytesValue(optionValue));
            return this;
        }

        public StreamNested<R> setOptionValue(int optionValue) {
            node.setOptionValue(new AstLiteralIntegerValue(optionValue));
            return this;
        }

        public StreamNested<R> setOptionValue(long optionValue) {
            node.setOptionValue(new AstLiteralLongValue(optionValue));
            return this;
        }

        public StreamNested<R> setOptionValue(ValueExpression expression, ExpressionContext environment) {
            node.setOptionValue(new AstExpressionValue<>(expression, environment));
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
