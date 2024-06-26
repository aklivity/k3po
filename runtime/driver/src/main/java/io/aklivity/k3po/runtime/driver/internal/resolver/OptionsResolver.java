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
package io.aklivity.k3po.runtime.driver.internal.resolver;

import java.util.HashMap;
import java.util.Map;

import io.aklivity.k3po.runtime.lang.internal.ast.value.AstValue;

/**
 * The class is used to defer the evaluation of accept and connect options.
 */
public final class OptionsResolver {

    private final Map<String, Object> options;

    private Map<String, Object> resolved;

    public OptionsResolver(Map<String, Object> options) {
        this.options = options;
    }

    public Map<String, Object> resolve() {

        if (resolved == null) {
            resolved = new HashMap<String, Object>();
            for (String name : options.keySet()) {
                Object value = options.get(name);
                if (value instanceof AstValue)
                {
                    value = ((AstValue<?>)value).getValue();
                }
                resolved.put(name, value);
            }
        }

        return resolved;
    }
}
