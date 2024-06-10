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

import io.aklivity.k3po.runtime.lang.internal.ast.AstPropertyNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstScriptNode;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstExpressionValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralBytesValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralIntegerValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralLongValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralTextValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralURIValue;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;

public class AstPropertyNodeBuilder extends AbstractAstNodeBuilder<AstPropertyNode, AstPropertyNode> {

    public AstPropertyNodeBuilder() {
        this(new AstPropertyNode());
    }

    public AstPropertyNodeBuilder setPropertyName(String propertyName) {
        node.setPropertyName(propertyName);
        return this;
    }

    public AstPropertyNodeBuilder setPropertyValue(String propertyValue) {
        node.setPropertyValue(new AstLiteralTextValue(propertyValue));
        return this;
    }

    public AstPropertyNodeBuilder setPropertyValue(byte[] propertyValue) {
        node.setPropertyValue(new AstLiteralBytesValue(propertyValue));
        return this;
    }

    public AstPropertyNodeBuilder setPropertyValue(int propertyValue) {
        node.setPropertyValue(new AstLiteralIntegerValue(propertyValue));
        return this;
    }

    public AstPropertyNodeBuilder setPropertyValue(long propertyValue) {
        node.setPropertyValue(new AstLiteralLongValue(propertyValue));
        return this;
    }

    public AstPropertyNodeBuilder setPropertyValue(URI propertyValue) {
        node.setPropertyValue(new AstLiteralURIValue(propertyValue));
        return this;
    }

    public AstPropertyNodeBuilder setPropertyValue(ValueExpression expression, ExpressionContext environment) {
        node.setPropertyValue(new AstExpressionValue<>(expression, environment));
        return this;
    }

    @Override
    public AstPropertyNode done() {
        return result;
    }

    private AstPropertyNodeBuilder(AstPropertyNode node) {
        super(node, node);
    }

    public static class ScriptNested<R extends AbstractAstNodeBuilder<? extends AstScriptNode, ?>> extends
            AbstractAstNodeBuilder<AstPropertyNode, R> {

        public ScriptNested(R builder) {
            super(new AstPropertyNode(), builder);
        }

        public ScriptNested<R> setOptionName(String propertyName) {
            node.setPropertyName(propertyName);
            return this;
        }

        public ScriptNested<R> setPropertyValue(String propertyValue) {
            node.setPropertyValue(new AstLiteralTextValue(propertyValue));
            return this;
        }

        public ScriptNested<R> setPropertyValue(byte[] propertyValue) {
            node.setPropertyValue(new AstLiteralBytesValue(propertyValue));
            return this;
        }

        public ScriptNested<R> setPropertyValue(int propertyValue) {
            node.setPropertyValue(new AstLiteralIntegerValue(propertyValue));
            return this;
        }

        public ScriptNested<R> setPropertyValue(long propertyValue) {
            node.setPropertyValue(new AstLiteralLongValue(propertyValue));
            return this;
        }

        public ScriptNested<R> setPropertyValue(URI propertyValue) {
            node.setPropertyValue(new AstLiteralURIValue(propertyValue));
            return this;
        }

        @Override
        public R done() {
            AstScriptNode scriptNode = result.node;
            scriptNode.getProperties().add(node);
            return result;
        }

    }
}
