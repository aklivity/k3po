/*
 * Copyright 2014, Kaazing Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaazing.k3po.lang.ast.builder;

import javax.el.ValueExpression;

import org.kaazing.k3po.lang.ast.AstPropertyNode;
import org.kaazing.k3po.lang.ast.AstScriptNode;
import org.kaazing.k3po.lang.ast.value.AstExpressionValue;
import org.kaazing.k3po.lang.ast.value.AstLiteralBytesValue;
import org.kaazing.k3po.lang.ast.value.AstLiteralTextValue;

public class AstPropertyNodeBuilder extends AbstractAstNodeBuilder<AstPropertyNode, AstPropertyNode> {

    public AstPropertyNodeBuilder() {
        this(new AstPropertyNode());
    }

    public AstPropertyNodeBuilder setPropertyName(String propertyName) {
        node.setPropertyName(propertyName);
        return this;
    }

    public AstPropertyNodeBuilder setPropertyValue(String exactText) {
        node.setPropertyValue(new AstLiteralTextValue(exactText));
        return this;
    }

    public AstPropertyNodeBuilder setPropertyValue(byte[] exactBytes) {
        node.setPropertyValue(new AstLiteralBytesValue(exactBytes));
        return this;
    }

    public AstPropertyNodeBuilder setPropertyValue(ValueExpression expression) {
        node.setPropertyValue(new AstExpressionValue(expression));
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

        public ScriptNested<R> setPropertyValue(String exactText) {
            node.setPropertyValue(new AstLiteralTextValue(exactText));
            return this;
        }

        public ScriptNested<R> setOptionValue(byte[] exactBytes) {
            node.setPropertyValue(new AstLiteralBytesValue(exactBytes));
            return this;
        }

        public ScriptNested<R> setOptionValue(ValueExpression expression) {
            node.setPropertyValue(new AstExpressionValue(expression));
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