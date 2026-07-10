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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import io.aklivity.k3po.runtime.lang.internal.ast.value.AstValue;
import io.aklivity.k3po.runtime.lang.types.StructuredTypeInfo;

public class AstRejectedNode extends AstAcceptableNode {

    private StructuredTypeInfo type;
    private Collection<AstValue<?>> values;
    private Map<String, AstValue<?>> valuesByName;

    public AstRejectedNode() {
        this.valuesByName = new LinkedHashMap<>();
        this.values = new LinkedList<>();
    }

    public void setType(StructuredTypeInfo type) {
        this.type = type;
    }

    public StructuredTypeInfo getType() {
        return type;
    }

    public void setValue(String name, AstValue<?> value) {
        valuesByName.put(name, value);
    }

    public AstValue<?> getValue(String name) {
        return valuesByName.get(name);
    }

    public void addValue(AstValue<?> value) {
        values.add(value);
    }

    public Collection<AstValue<?>> getValues() {
        return values;
    }

    @Override
    protected int hashTo() {
        int hashCode = super.hashTo();

        if (type != null) {
            hashCode <<= 4;
            hashCode ^= type.hashCode();
        }
        if (valuesByName != null) {
            hashCode <<= 4;
            hashCode ^= valuesByName.hashCode();
        }

        return hashCode;
    }

    @Override
    protected boolean equalTo(AstAcceptableNode that) {
        return that instanceof AstRejectedNode && equalTo((AstRejectedNode) that);
    }

    protected boolean equalTo(AstRejectedNode that) {
        return super.equalTo(that) &&
                equivalent(this.type, that.type) &&
                equivalent(this.values, that.values) &&
                equivalent(this.valuesByName, that.valuesByName);
    }

    @Override
    public <R, P> R accept(Visitor<R, P> visitor, P parameter) {
        return visitor.visit(this, parameter);
    }

    @Override
    protected void describeLine(StringBuilder sb) {
        super.describeLine(sb);

        sb.append("rejected");

        String acceptName = getAcceptName();
        if (acceptName != null) {
            sb.append(" as ");
            sb.append(acceptName);
        }

        if (type != null) {
            sb.append(' ').append(type);
            for (Map.Entry<String, AstValue<?>> entry : valuesByName.entrySet()) {
                sb.append(' ').append(entry.getKey()).append('=').append(entry.getValue());
            }
            for (AstValue<?> value : values) {
                sb.append(' ').append(value);
            }
        }

        sb.append('\n');
    }
}
