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
package io.aklivity.k3po.runtime.lang.internal.ast.matcher;

import static io.aklivity.k3po.runtime.lang.internal.ast.util.AstUtil.equivalent;
import static java.lang.String.format;

import javax.el.ValueExpression;

import io.aklivity.k3po.runtime.lang.internal.ast.AstRegion;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;

public final class AstExpressionMatcher extends AstValueMatcher {

    private final ValueExpression value;
    private final ExpressionContext environment;

    public AstExpressionMatcher(ValueExpression value, ExpressionContext environment) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        this.value = value;
        this.environment = environment;
    }

    public ValueExpression getValue() {
        return value;
    }

    @Override
    public <R, P> R accept(Visitor<R, P> visitor, P parameter) {

        return visitor.visit(this, parameter);
    }

    @Override
    protected int hashTo() {
        return value.hashCode();
    }

    @Override
    protected boolean equalTo(AstRegion that) {
        return (that instanceof AstExpressionMatcher) && equalTo((AstExpressionMatcher) that);
    }

    protected boolean equalTo(AstExpressionMatcher that) {
        return equivalent(this.value, that.value);
    }

    @Override
    protected void describe(StringBuilder buf) {
        buf.append(format("(%s)%s", value.getExpectedType().getSimpleName(), value.getExpressionString()));
    }

    public ExpressionContext getEnvironment() {
        return environment;
    }
}
