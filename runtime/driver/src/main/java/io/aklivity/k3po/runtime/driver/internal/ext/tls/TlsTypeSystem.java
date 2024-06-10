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

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import io.aklivity.k3po.runtime.lang.types.StructuredTypeInfo;
import io.aklivity.k3po.runtime.lang.types.TypeInfo;
import io.aklivity.k3po.runtime.lang.types.TypeSystemSpi;

public final class TlsTypeSystem implements TypeSystemSpi
{
    public static final TypeInfo<URI> OPTION_TRANSPORT = new TypeInfo<>("transport", URI.class);
    public static final TypeInfo<String> OPTION_KEY_STORE_FILE = new TypeInfo<>("keyStoreFile", String.class);
    public static final TypeInfo<String> OPTION_KEY_STORE_PASSWORD = new TypeInfo<>("keyStorePassword", String.class);
    public static final TypeInfo<String> OPTION_TRUST_STORE_FILE = new TypeInfo<>("trustStoreFile", String.class);
    public static final TypeInfo<String> OPTION_TRUST_STORE_PASSWORD = new TypeInfo<>("trustStorePassword", String.class);
    public static final TypeInfo<String> OPTION_APPLICATION_PROTOCOLS = new TypeInfo<>("applicationProtocols", String.class);
    public static final TypeInfo<Boolean> OPTION_NEED_CLIENT_AUTH = new TypeInfo<>("needClientAuth", Boolean.class);
    public static final TypeInfo<Boolean> OPTION_WANT_CLIENT_AUTH = new TypeInfo<>("wantClientAuth", Boolean.class);

    private final Set<TypeInfo<?>> acceptOptions;
    private final Set<TypeInfo<?>> connectOptions;
    private final Set<TypeInfo<?>> readOptions;
    private final Set<TypeInfo<?>> writeOptions;
    private final Set<StructuredTypeInfo> readConfigs;
    private final Set<StructuredTypeInfo> writeConfigs;
    private final Set<StructuredTypeInfo> readAdvisories;
    private final Set<StructuredTypeInfo> writeAdvisories;

    public TlsTypeSystem()
    {
        Set<TypeInfo<?>> acceptOptions = new LinkedHashSet<>();
        acceptOptions.add(OPTION_TRANSPORT);
        acceptOptions.add(OPTION_KEY_STORE_FILE);
        acceptOptions.add(OPTION_KEY_STORE_PASSWORD);
        acceptOptions.add(OPTION_TRUST_STORE_FILE);
        acceptOptions.add(OPTION_TRUST_STORE_PASSWORD);
        acceptOptions.add(OPTION_APPLICATION_PROTOCOLS);
        acceptOptions.add(OPTION_NEED_CLIENT_AUTH);
        acceptOptions.add(OPTION_WANT_CLIENT_AUTH);
        this.acceptOptions = unmodifiableSet(acceptOptions);

        Set<TypeInfo<?>> connectOptions = new LinkedHashSet<>();
        connectOptions.add(OPTION_TRANSPORT);
        connectOptions.add(OPTION_KEY_STORE_FILE);
        connectOptions.add(OPTION_KEY_STORE_PASSWORD);
        connectOptions.add(OPTION_TRUST_STORE_FILE);
        connectOptions.add(OPTION_TRUST_STORE_PASSWORD);
        connectOptions.add(OPTION_APPLICATION_PROTOCOLS);
        this.connectOptions = unmodifiableSet(connectOptions);

        this.readOptions = emptySet();
        this.writeOptions = emptySet();
        this.readConfigs = emptySet();
        this.writeConfigs = emptySet();;
        this.readAdvisories = emptySet();
        this.writeAdvisories = emptySet();
    }

    @Override
    public String getName()
    {
        return "tls";
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
