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

import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectAbortedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstStreamNode;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstExactBytesMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstExactTextMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstExpressionMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstFixedLengthBytesMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstRegexMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstVariableLengthBytesMatcher;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;
import io.aklivity.k3po.runtime.lang.internal.regex.NamedGroupPattern;
import io.aklivity.k3po.runtime.lang.types.StructuredTypeInfo;

public class AstConnectAbortedNodeBuilder extends AbstractAstStreamableNodeBuilder<AstConnectAbortedNode, AstConnectAbortedNode> {

    public AstConnectAbortedNodeBuilder() {
        this(new AstConnectAbortedNode());
    }

    @Override
    public AstConnectAbortedNode done() {
        return result;
    }

    public AstConnectAbortedNodeBuilder setType(StructuredTypeInfo type) {
        node.setType(type);
        return this;
    }

    public AstConnectAbortedNodeBuilder setMatcherFixedLengthBytes(String name, int valueLength) {
        node.setMatcher(name, new AstFixedLengthBytesMatcher(valueLength));
        return this;
    }

    public AstConnectAbortedNodeBuilder setMatcherExactText(String name, String valueExactText) {
        node.setMatcher(name, new AstExactTextMatcher(valueExactText));
        return this;
    }

    public AstConnectAbortedNodeBuilder setMatcherExactBytes(String name, byte[] valueBytes) {
        node.setMatcher(name, new AstExactBytesMatcher(valueBytes));
        return this;
    }

    public AstConnectAbortedNodeBuilder setMatcherExpression(String name, ValueExpression valueValueExpression,
        ExpressionContext environment) {
        node.setMatcher(name, new AstExpressionMatcher(valueValueExpression, environment));
        return this;
    }

    public AstConnectAbortedNodeBuilder setMatcherRegex(String name, NamedGroupPattern valuePattern, ExpressionContext environment) {
        node.setMatcher(name, new AstRegexMatcher(valuePattern, environment));
        return this;
    }

    public AstConnectAbortedNodeBuilder setMatcherVariableLengthBytes(String name, ValueExpression valueLength,
        ExpressionContext environment) {
        node.setMatcher(name, new AstVariableLengthBytesMatcher(valueLength, environment));
        return this;
    }

    public AstConnectAbortedNodeBuilder addMatcherFixedLengthBytes(int valueLength) {
        node.addMatcher(new AstFixedLengthBytesMatcher(valueLength));
        return this;
    }

    public AstConnectAbortedNodeBuilder addMatcherExactText(String valueExactText) {
        node.addMatcher(new AstExactTextMatcher(valueExactText));
        return this;
    }

    public AstConnectAbortedNodeBuilder addMatcherExactBytes(byte[] valueBytes) {
        node.addMatcher(new AstExactBytesMatcher(valueBytes));
        return this;
    }

    public AstConnectAbortedNodeBuilder addMatcherExpression(ValueExpression valueValueExpression, ExpressionContext environment) {
        node.addMatcher(new AstExpressionMatcher(valueValueExpression, environment));
        return this;
    }

    public AstConnectAbortedNodeBuilder addMatcherRegex(NamedGroupPattern valuePattern, ExpressionContext environment) {
        node.addMatcher(new AstRegexMatcher(valuePattern, environment));
        return this;
    }

    public AstConnectAbortedNodeBuilder addMatcherVariableLengthBytes(ValueExpression valueLength, ExpressionContext environment) {
        node.addMatcher(new AstVariableLengthBytesMatcher(valueLength, environment));
        return this;
    }

    private AstConnectAbortedNodeBuilder(AstConnectAbortedNode node) {
        super(node, node);
    }

    public static class StreamNested<R extends AbstractAstNodeBuilder<? extends AstStreamNode, ?>> extends
            AbstractAstStreamableNodeBuilder<AstConnectAbortedNode, R> {

        public StreamNested(R builder) {
            super(new AstConnectAbortedNode(), builder);
        }

        public StreamNested<R> setType(StructuredTypeInfo type) {
            node.setType(type);
            return this;
        }

        public StreamNested<R> setMatcherFixedLengthBytes(String name, int valueLength) {
            node.setMatcher(name, new AstFixedLengthBytesMatcher(valueLength));
            return this;
        }

        public StreamNested<R> setMatcherExactText(String name, String valueExactText) {
            node.setMatcher(name, new AstExactTextMatcher(valueExactText));
            return this;
        }

        public StreamNested<R> setMatcherExactBytes(String name, byte[] valueBytes) {
            node.setMatcher(name, new AstExactBytesMatcher(valueBytes));
            return this;
        }

        public StreamNested<R> setMatcherExpression(String name, ValueExpression valueValueExpression,
            ExpressionContext environment) {
            node.setMatcher(name, new AstExpressionMatcher(valueValueExpression, environment));
            return this;
        }

        public StreamNested<R> setMatcherRegex(String name, NamedGroupPattern valuePattern, ExpressionContext environment) {
            node.setMatcher(name, new AstRegexMatcher(valuePattern, environment));
            return this;
        }

        public StreamNested<R> setMatcherVariableLengthBytes(String name, ValueExpression valueLength,
            ExpressionContext environment) {
            node.setMatcher(name, new AstVariableLengthBytesMatcher(valueLength, environment));
            return this;
        }

        public StreamNested<R> addMatcherFixedLengthBytes(int valueLength) {
            node.addMatcher(new AstFixedLengthBytesMatcher(valueLength));
            return this;
        }

        public StreamNested<R> addMatcherExactText(String valueExactText) {
            node.addMatcher(new AstExactTextMatcher(valueExactText));
            return this;
        }

        public StreamNested<R> addMatcherExactBytes(byte[] valueBytes) {
            node.addMatcher(new AstExactBytesMatcher(valueBytes));
            return this;
        }

        public StreamNested<R> addMatcherExpression(ValueExpression valueValueExpression, ExpressionContext environment) {
            node.addMatcher(new AstExpressionMatcher(valueValueExpression, environment));
            return this;
        }

        public StreamNested<R> addMatcherRegex(NamedGroupPattern valuePattern, ExpressionContext environment) {
            node.addMatcher(new AstRegexMatcher(valuePattern, environment));
            return this;
        }

        public StreamNested<R> addMatcherVariableLengthBytes(ValueExpression valueLength, ExpressionContext environment) {
            node.addMatcher(new AstVariableLengthBytesMatcher(valueLength, environment));
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
