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
package io.aklivity.k3po.runtime.driver.internal.behavior;

import static java.util.Collections.unmodifiableMap;
import static java.util.ServiceLoader.load;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import org.jboss.netty.channel.ChannelHandler;

import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.MessageDecoder;
import io.aklivity.k3po.runtime.driver.internal.behavior.handler.codec.MessageEncoder;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAdviseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAdvisedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadConfigNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadOptionNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAdviseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAdvisedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteConfigNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteOptionNode;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstValueMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstValue;
import io.aklivity.k3po.runtime.lang.types.StructuredTypeInfo;
import io.aklivity.k3po.runtime.lang.types.TypeInfo;

public final class BehaviorSystem {

    private final Map<TypeInfo<?>, ReadOptionFactory> readOptions;
    private final Map<TypeInfo<?>, WriteOptionFactory> writeOptions;
    private final Map<StructuredTypeInfo, ReadConfigFactory> readConfigs;
    private final Map<StructuredTypeInfo, WriteConfigFactory> writeConfigs;
    private final Map<StructuredTypeInfo, ReadAdviseFactory> readAdvises;
    private final Map<StructuredTypeInfo, WriteAdviseFactory> writeAdvises;
    private final Map<StructuredTypeInfo, ReadAdvisedFactory> readAdviseds;
    private final Map<StructuredTypeInfo, WriteAdvisedFactory> writeAdviseds;

    private BehaviorSystem(Iterable<BehaviorSystemSpi> behaviorSystems) {

        Map<TypeInfo<?>, ReadOptionFactory> readOptions = new IdentityHashMap<>();
        Map<TypeInfo<?>, WriteOptionFactory> writeOptions = new IdentityHashMap<>();
        Map<StructuredTypeInfo, ReadConfigFactory> readConfigs = new IdentityHashMap<>();
        Map<StructuredTypeInfo, WriteConfigFactory> writeConfigs = new IdentityHashMap<>();
        Map<StructuredTypeInfo, ReadAdviseFactory> readAdvises = new IdentityHashMap<>();
        Map<StructuredTypeInfo, WriteAdviseFactory> writeAdvises = new IdentityHashMap<>();
        Map<StructuredTypeInfo, ReadAdvisedFactory> readAdviseds = new IdentityHashMap<>();
        Map<StructuredTypeInfo, WriteAdvisedFactory> writeAdviseds = new IdentityHashMap<>();

        for (BehaviorSystemSpi behaviorSystem : behaviorSystems) {
            for (TypeInfo<?> optionType : behaviorSystem.getReadOptionTypes()) {
                readOptions.put(optionType, behaviorSystem.readOptionFactory(optionType));
            }
            for (TypeInfo<?> optionType : behaviorSystem.getWriteOptionTypes()) {
                writeOptions.put(optionType, behaviorSystem.writeOptionFactory(optionType));
            }

            for (StructuredTypeInfo configType : behaviorSystem.getReadConfigTypes()) {
                readConfigs.put(configType, behaviorSystem.readConfigFactory(configType));
            }
            for (StructuredTypeInfo configType : behaviorSystem.getWriteConfigTypes()) {
                writeConfigs.put(configType, behaviorSystem.writeConfigFactory(configType));
            }

            for (StructuredTypeInfo advisoryType : behaviorSystem.getReadAdvisoryTypes()) {
                readAdvises.put(advisoryType, behaviorSystem.readAdviseFactory(advisoryType));
                writeAdviseds.put(advisoryType, behaviorSystem.writeAdvisedFactory(advisoryType));
            }
            for (StructuredTypeInfo advisoryType : behaviorSystem.getWriteAdvisoryTypes()) {
                writeAdvises.put(advisoryType, behaviorSystem.writeAdviseFactory(advisoryType));
                readAdviseds.put(advisoryType, behaviorSystem.readAdvisedFactory(advisoryType));
            }
        }

        this.readOptions = unmodifiableMap(readOptions);
        this.writeOptions = unmodifiableMap(writeOptions);
        this.readConfigs = unmodifiableMap(readConfigs);
        this.writeConfigs = unmodifiableMap(writeConfigs);
        this.readAdvises = unmodifiableMap(readAdvises);
        this.writeAdvises = unmodifiableMap(writeAdvises);
        this.readAdviseds = unmodifiableMap(readAdviseds);
        this.writeAdviseds = unmodifiableMap(writeAdviseds);
    }

    public ChannelHandler newReadOptionHandler(
        AstReadOptionNode node) {

        TypeInfo<?> optionType = node.getOptionType();
        ReadOptionFactory factory = readOptions.getOrDefault(optionType, n -> null);
        return factory.newHandler(node);
    }

    public ChannelHandler newWriteOptionHandler(
        AstWriteOptionNode node) {

        TypeInfo<?> optionType = node.getOptionType();
        WriteOptionFactory factory = writeOptions.getOrDefault(optionType, n -> null);
        return factory.newHandler(node);
    }

    public ChannelHandler newReadConfigHandler(
        AstReadConfigNode node,
        Function<AstValueMatcher, MessageDecoder> decoderFactory) {

        StructuredTypeInfo type = node.getType();
        ReadConfigFactory factory = readConfigs.getOrDefault(type, (n, f) -> null);
        return factory.newHandler(node, decoderFactory);
    }

    public ChannelHandler newWriteConfigHandler(
        AstWriteConfigNode node,
        Function<AstValue<?>, MessageEncoder> encoderFactory) {

        StructuredTypeInfo type = node.getType();
        WriteConfigFactory factory = writeConfigs.getOrDefault(type, (n, f) -> null);
        return factory.newHandler(node, encoderFactory);
    }

    public ChannelHandler newReadAdviseHandler(
        AstReadAdviseNode node,
        Function<AstValue<?>, MessageEncoder> encoderFactory) {

        StructuredTypeInfo type = node.getType();
        ReadAdviseFactory factory = readAdvises.getOrDefault(type, (n, f) -> null);
        return factory.newHandler(node, encoderFactory);
    }

    public ChannelHandler newWriteAdviseHandler(
        AstWriteAdviseNode node,
        Function<AstValue<?>, MessageEncoder> encoderFactory) {

        StructuredTypeInfo type = node.getType();
        WriteAdviseFactory factory = writeAdvises.getOrDefault(type, (n, f) -> null);
        return factory.newHandler(node, encoderFactory);
    }

    public ChannelHandler newReadAdvisedHandler(
        AstReadAdvisedNode node,
        Function<AstValueMatcher, MessageDecoder> decoderFactory) {

        StructuredTypeInfo type = node.getType();
        ReadAdvisedFactory factory = readAdviseds.getOrDefault(type, (n, f) -> null);
        return factory.newHandler(node, decoderFactory);
    }

    public ChannelHandler newWriteAdvisedHandler(
        AstWriteAdvisedNode node,
        Function<AstValueMatcher, MessageDecoder> decoderFactory) {

        StructuredTypeInfo type = node.getType();
        WriteAdvisedFactory factory = writeAdviseds.getOrDefault(type, (n, f) -> null);
        return factory.newHandler(node, decoderFactory);
    }

    public static final BehaviorSystem newInstance() {
        return new BehaviorSystem(load(BehaviorSystemSpi.class));
    }
}
