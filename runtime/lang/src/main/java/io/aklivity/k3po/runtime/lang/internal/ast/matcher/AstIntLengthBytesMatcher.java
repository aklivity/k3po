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

import static java.lang.String.format;

import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;

public final class AstIntLengthBytesMatcher extends AstFixedLengthBytesMatcher {

    public AstIntLengthBytesMatcher(String captureName, ExpressionContext environment) {
        super(Integer.SIZE / Byte.SIZE, captureName, environment);
    }

    @Override
    protected void describe(StringBuilder buf) {
        String captureName = getCaptureName();
        if (captureName == null) {
            buf.append("int");
        }
        else {
            buf.append(format("(int:%s)", captureName));
        }
    }

    @Override
    public <R, P> R accept(Visitor<R, P> visitor, P parameter) {

        return visitor.visit(this, parameter);
    }
}
