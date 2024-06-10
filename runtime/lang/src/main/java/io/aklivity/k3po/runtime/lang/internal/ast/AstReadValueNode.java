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

import static io.aklivity.k3po.runtime.lang.internal.ast.util.AstUtil.equivalent;

import java.util.ArrayList;
import java.util.List;

import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstValueMatcher;

public class AstReadValueNode extends AstEventNode {

    private List<AstValueMatcher> matchers;

    public List<AstValueMatcher> getMatchers() {
        return matchers;
    }

    public void setMatchers(List<AstValueMatcher> matchers) {
        this.matchers = matchers;
    }

    public void addMatcher(AstValueMatcher matcher) {
        if (matchers == null) {
            matchers = new ArrayList<>();
        }
        matchers.add(matcher);
    }

    @Override
    public <R, P> R accept(Visitor<R, P> visitor, P parameter) {
        return visitor.visit(this, parameter);
    }

    @Override
    protected int hashTo() {
        int hashCode = getClass().hashCode();

        if (matchers != null) {
            hashCode <<= 4;
            hashCode ^= matchers.hashCode();
        }

        return hashCode;
    }

    @Override
    protected boolean equalTo(AstRegion that) {
        return that instanceof AstReadValueNode && equalTo((AstReadValueNode) that);
    }

    protected boolean equalTo(AstReadValueNode that) {
        return equivalent(this.matchers, that.matchers);
    }

    @Override
    protected void describe(StringBuilder buf) {
        super.describe(buf);
        buf.append("read");
        for (AstValueMatcher matcher : matchers) {
            buf.append(" " + matcher);
        }
        buf.append("\n");
    }
}
