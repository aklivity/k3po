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

import io.aklivity.k3po.runtime.lang.internal.ast.AstReadOptionNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstStreamNode;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstExpressionValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralBytesValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralIntegerValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralLongValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralTextValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralURIValue;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;
import io.aklivity.k3po.runtime.lang.types.TypeInfo;

public class AstReadOptionNodeBuilder extends AbstractAstStreamableNodeBuilder<AstReadOptionNode, AstReadOptionNode> {

    public AstReadOptionNodeBuilder() {
        this(new AstReadOptionNode());
    }

    public AstReadOptionNodeBuilder setOptionType(TypeInfo<?> optionType) {
        node.setOptionType(optionType);
        return this;
    }

    public AstReadOptionNodeBuilder setOptionName(String optionName) {
        node.setOptionName(optionName);
        return this;
    }

    public AstReadOptionNodeBuilder setOptionValue(URI optionValue) {
        node.setOptionValue(new AstLiteralURIValue(optionValue));
        return this;
    }

    public AstReadOptionNodeBuilder setOptionValue(String optionValue) {
        node.setOptionValue(new AstLiteralTextValue(optionValue));
        return this;
    }

    public AstReadOptionNodeBuilder setOptionValue(byte[] optionValue) {
        node.setOptionValue(new AstLiteralBytesValue(optionValue));
        return this;
    }

    public AstReadOptionNodeBuilder setOptionValue(int optionValue) {
        node.setOptionValue(new AstLiteralIntegerValue(optionValue));
        return this;
    }

    public AstReadOptionNodeBuilder setOptionValue(long optionValue) {
        node.setOptionValue(new AstLiteralLongValue(optionValue));
        return this;
    }

    public AstReadOptionNodeBuilder setOptionValue(ValueExpression expression, ExpressionContext environment) {
        node.setOptionValue(new AstExpressionValue<>(expression, environment));
        return this;
    }

    @Override
    public AstReadOptionNode done() {
        return result;
    }

    private AstReadOptionNodeBuilder(AstReadOptionNode node) {
        super(node, node);
    }

    public static class StreamNested<R extends AbstractAstNodeBuilder<? extends AstStreamNode, ?>> extends
            AbstractAstStreamableNodeBuilder<AstReadOptionNode, R> {

        public StreamNested(R builder) {
            super(new AstReadOptionNode(), builder);
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
