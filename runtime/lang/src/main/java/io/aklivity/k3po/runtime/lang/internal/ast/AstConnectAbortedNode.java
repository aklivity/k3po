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
package io.aklivity.k3po.runtime.lang.internal.ast;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstValueMatcher;
import io.aklivity.k3po.runtime.lang.types.StructuredTypeInfo;

public final class AstConnectAbortedNode extends AstEventNode {

    private StructuredTypeInfo type;
    private Collection<AstValueMatcher> matchers;
    private Map<String, AstValueMatcher> matchersByName;

    public AstConnectAbortedNode() {
        this.matchersByName = new LinkedHashMap<>();
        this.matchers = new LinkedList<>();
    }

    public void setType(StructuredTypeInfo type) {
        this.type = type;
    }

    public StructuredTypeInfo getType() {
        return type;
    }

    public void setMatcher(String name, AstValueMatcher matcher) {
        matchersByName.put(name, matcher);
    }

    public AstValueMatcher getMatcher(String name) {
        return matchersByName.get(name);
    }

    public void addMatcher(AstValueMatcher matcher) {
        matchers.add(matcher);
    }

    public Collection<AstValueMatcher> getMatchers() {
        return matchers;
    }

    @Override
    public <R, P> R accept(Visitor<R, P> visitor, P parameter) {
        return visitor.visit(this, parameter);
    }

    @Override
    protected int hashTo() {
        int hashCode = getClass().hashCode();

        if (type != null) {
            hashCode <<= 4;
            hashCode ^= type.hashCode();
        }
        if (matchersByName != null) {
            hashCode <<= 4;
            hashCode ^= matchersByName.hashCode();
        }

        return hashCode;
    }

    @Override
    protected boolean equalTo(AstRegion that) {
        return that instanceof AstConnectAbortedNode && equalTo((AstConnectAbortedNode) that);
    }

    protected boolean equalTo(AstConnectAbortedNode that) {
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.matchers, that.matchers) &&
                Objects.equals(this.matchersByName, that.matchersByName);
    }

    @Override
    protected void describe(StringBuilder buf) {
        super.describe(buf);
        buf.append("connect aborted");

        if (type != null) {
            buf.append(' ').append(type);
            for (Map.Entry<String, AstValueMatcher> entry : matchersByName.entrySet()) {
                buf.append(' ').append(entry.getKey()).append('=').append(entry.getValue());
            }
            for (AstValueMatcher matcher : matchers) {
                buf.append(' ').append(matcher);
            }
        }

        buf.append('\n');
    }
}
