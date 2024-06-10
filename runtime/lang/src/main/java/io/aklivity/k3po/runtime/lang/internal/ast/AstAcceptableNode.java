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

public abstract class AstAcceptableNode extends AstStreamNode {

    private String acceptName;

    public String getAcceptName() {
        return acceptName;
    }

    public void setAcceptName(String acceptName) {
        this.acceptName = acceptName;
    }

    @Override
    protected int hashTo() {
        int hashCode = super.hashTo();

        if (acceptName != null) {
            hashCode <<= 4;
            hashCode ^= acceptName.hashCode();
        }

        return hashCode;
    }

    @Override
    protected boolean equalTo(AstRegion that) {
        return that instanceof AstAcceptableNode && equalTo((AstAcceptableNode) that);
    }

    protected boolean equalTo(AstAcceptableNode that) {
        return super.equalTo(that) && equivalent(this.acceptName, that.acceptName);
    }

}
