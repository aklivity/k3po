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
package io.aklivity.k3po.runtime.lang.internal.ast.value;

import static io.aklivity.k3po.runtime.lang.internal.ast.util.AstUtil.equivalent;

import java.net.URI;

import io.aklivity.k3po.runtime.lang.internal.ast.AstRegion;

public final class AstLiteralURIValue extends AstValue<URI> {

    private final URI value;

    public AstLiteralURIValue(URI value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        this.value = value;
    }

    @Override
    public URI getValue() {
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
        return (that instanceof AstLiteralURIValue) && equalTo((AstLiteralURIValue) that);
    }

    protected boolean equalTo(AstLiteralURIValue that) {
        return equivalent(this.value, that.value);
    }

    @Override
    protected void describe(StringBuilder buf) {
        buf.append(value.toString());
    }

    public static String toString(int value) {
        return Integer.toString(value);
    }
}
