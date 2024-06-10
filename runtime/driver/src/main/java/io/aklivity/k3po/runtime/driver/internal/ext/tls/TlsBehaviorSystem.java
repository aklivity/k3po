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
package io.aklivity.k3po.runtime.driver.internal.ext.tls;

import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.Set;

import io.aklivity.k3po.runtime.driver.internal.behavior.BehaviorSystemSpi;
import io.aklivity.k3po.runtime.driver.internal.behavior.ReadAdviseFactory;
import io.aklivity.k3po.runtime.driver.internal.behavior.ReadAdvisedFactory;
import io.aklivity.k3po.runtime.driver.internal.behavior.ReadConfigFactory;
import io.aklivity.k3po.runtime.driver.internal.behavior.ReadOptionFactory;
import io.aklivity.k3po.runtime.driver.internal.behavior.WriteAdviseFactory;
import io.aklivity.k3po.runtime.driver.internal.behavior.WriteAdvisedFactory;
import io.aklivity.k3po.runtime.driver.internal.behavior.WriteConfigFactory;
import io.aklivity.k3po.runtime.driver.internal.behavior.WriteOptionFactory;
import io.aklivity.k3po.runtime.lang.types.StructuredTypeInfo;
import io.aklivity.k3po.runtime.lang.types.TypeInfo;

public class TlsBehaviorSystem implements BehaviorSystemSpi {

    private final Map<TypeInfo<?>, ReadOptionFactory> readOptionFactories;
    private final Map<TypeInfo<?>, WriteOptionFactory> writeOptionFactories;

    private final Map<StructuredTypeInfo, ReadConfigFactory> readConfigFactories;
    private final Map<StructuredTypeInfo, WriteConfigFactory> writeConfigFactories;

    private final Map<StructuredTypeInfo, ReadAdviseFactory> readAdviseFactories;
    private final Map<StructuredTypeInfo, WriteAdviseFactory> writeAdviseFactories;
    private final Map<StructuredTypeInfo, ReadAdvisedFactory> readAdvisedFactories;
    private final Map<StructuredTypeInfo, WriteAdvisedFactory> writeAdvisedFactories;

    public TlsBehaviorSystem()
    {
        this.readOptionFactories = emptyMap();
        this.writeOptionFactories = emptyMap();
        this.readConfigFactories = emptyMap();
        this.writeConfigFactories = emptyMap();
        this.readAdviseFactories = emptyMap();
        this.writeAdviseFactories = emptyMap();
        this.readAdvisedFactories = emptyMap();
        this.writeAdvisedFactories = emptyMap();
    }

    @Override
    public Set<StructuredTypeInfo> getReadConfigTypes()
    {
        return readConfigFactories.keySet();
    }

    @Override
    public Set<StructuredTypeInfo> getWriteConfigTypes()
    {
        return writeConfigFactories.keySet();
    }

    @Override
    public ReadConfigFactory readConfigFactory(
        StructuredTypeInfo configType)
    {
        return readConfigFactories.get(configType);
    }

    @Override
    public WriteConfigFactory writeConfigFactory(
        StructuredTypeInfo configType)
    {
        return writeConfigFactories.get(configType);
    }

    @Override
    public Set<StructuredTypeInfo> getReadAdvisoryTypes()
    {
        return readAdviseFactories.keySet();
    }

    @Override
    public Set<StructuredTypeInfo> getWriteAdvisoryTypes()
    {
        return writeAdviseFactories.keySet();
    }

    @Override
    public ReadAdviseFactory readAdviseFactory(
        StructuredTypeInfo advisoryType)
    {
        return readAdviseFactories.get(advisoryType);
    }

    @Override
    public ReadAdvisedFactory readAdvisedFactory(
        StructuredTypeInfo advisoryType)
    {
        return readAdvisedFactories.get(advisoryType);
    }

    @Override
    public WriteAdviseFactory writeAdviseFactory(
        StructuredTypeInfo advisoryType)
    {
        return writeAdviseFactories.get(advisoryType);
    }

    @Override
    public WriteAdvisedFactory writeAdvisedFactory(
        StructuredTypeInfo advisoryType)
    {
        return writeAdvisedFactories.get(advisoryType);
    }

    @Override
    public Set<TypeInfo<?>> getReadOptionTypes()
    {
        return readOptionFactories.keySet();
    }

    @Override
    public Set<TypeInfo<?>> getWriteOptionTypes()
    {
        return writeOptionFactories.keySet();
    }

    @Override
    public ReadOptionFactory readOptionFactory(
        TypeInfo<?> optionType)
    {
        return readOptionFactories.get(optionType);
    }

    @Override
    public WriteOptionFactory writeOptionFactory(
        TypeInfo<?> optionType)
    {
        return writeOptionFactories.get(optionType);
    }
}
