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
package io.aklivity.k3po.runtime.driver.internal.types;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.Set;

import io.aklivity.k3po.runtime.lang.types.StructuredTypeInfo;
import io.aklivity.k3po.runtime.lang.types.TypeInfo;
import io.aklivity.k3po.runtime.lang.types.TypeSystemSpi;

public final class UdpTypeSystem implements TypeSystemSpi
{
    public static final TypeInfo<Long> OPTION_TIMEOUT = new TypeInfo<>("timeout", long.class);

    private final Set<TypeInfo<?>> acceptOptions;
    private final Set<TypeInfo<?>> connectOptions;
    private final Set<TypeInfo<?>> readOptions;
    private final Set<TypeInfo<?>> writeOptions;
    private final Set<StructuredTypeInfo> readConfigs;
    private final Set<StructuredTypeInfo> writeConfigs;
    private final Set<StructuredTypeInfo> readAdvisories;
    private final Set<StructuredTypeInfo> writeAdvisories;

    public UdpTypeSystem()
    {
        this.acceptOptions = singleton(OPTION_TIMEOUT);
        this.connectOptions = singleton(OPTION_TIMEOUT);
        this.readOptions = emptySet();
        this.writeOptions = emptySet();
        this.readConfigs = emptySet();
        this.writeConfigs = emptySet();
        this.readAdvisories = emptySet();
        this.writeAdvisories = emptySet();
    }

    @Override
    public String getName()
    {
        return "udp";
    }

    @Override
    public Set<TypeInfo<?>> acceptOptions()
    {
        return acceptOptions;
    }

    @Override
    public Set<TypeInfo<?>> connectOptions()
    {
        return connectOptions;
    }

    @Override
    public Set<TypeInfo<?>> readOptions()
    {
        return readOptions;
    }

    @Override
    public Set<TypeInfo<?>> writeOptions()
    {
        return writeOptions;
    }

    @Override
    public Set<StructuredTypeInfo> readConfigs()
    {
        return readConfigs;
    }

    @Override
    public Set<StructuredTypeInfo> writeConfigs()
    {
        return writeConfigs;
    }

    @Override
    public Set<StructuredTypeInfo> readAdvisories()
    {
        return readAdvisories;
    }

    @Override
    public Set<StructuredTypeInfo> writeAdvisories()
    {
        return writeAdvisories;
    }
}
