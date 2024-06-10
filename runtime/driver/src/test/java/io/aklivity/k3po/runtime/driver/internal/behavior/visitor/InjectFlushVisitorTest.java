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
package io.aklivity.k3po.runtime.driver.internal.behavior.visitor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.aklivity.k3po.runtime.driver.internal.behavior.visitor.InjectFlushVisitor;
import io.aklivity.k3po.runtime.lang.internal.ast.AstScriptNode;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstScriptNodeBuilder;

public class InjectFlushVisitorTest {

    @Test
    public void shouldInjectFlushAfterWriteConfigBeforeFirstReadConfig()
        throws Exception {

        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .addWriteConfigCommand()
                    .done()
                .addFlushCommand()
                    .done()
                .addReadConfigEvent()
                    .done()
                .addReadConfigEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .addWriteConfigCommand()
                    .done()
                .addReadConfigEvent()
                    .done()
                .addReadConfigEvent()
                    .done()
                .done()
            .done();

        InjectFlushVisitor injectFlush = new InjectFlushVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectFlush, new InjectFlushVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldInjectFlushAfterWriteConfigBeforeFirstReadValue()
        throws Exception {

        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .addWriteConfigCommand()
                    .done()
                .addFlushCommand()
                    .done()
                .addReadEvent()
                    .done()
                .addReadEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .addWriteConfigCommand()
                    .done()
                .addReadEvent()
                    .done()
                .addReadEvent()
                    .done()
                .done()
            .done();

        InjectFlushVisitor injectFlush = new InjectFlushVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectFlush, new InjectFlushVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldNotInjectFlushAfterWriteConfigBeforeWrite()
        throws Exception {

        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .addWriteConfigCommand()
                    .done()
                .addWriteCommand()
                    .addExactText("Hello, world")
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .addWriteConfigCommand()
                    .done()
                .addWriteCommand()
                    .addExactText("Hello, world")
                    .done()
                .done()
            .done();

        InjectFlushVisitor injectFlush = new InjectFlushVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectFlush, new InjectFlushVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldNotInjectFlushAfterWriteBeforeReadConfig()
        throws Exception {

        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .addWriteCommand()
                    .addExactText("Hello, world")
                    .done()
                .addReadConfigEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .addWriteCommand()
                    .addExactText("Hello, world")
                    .done()
                .addReadConfigEvent()
                    .done()
                .done()
            .done();

        InjectFlushVisitor injectFlush = new InjectFlushVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectFlush, new InjectFlushVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldNotInjectFlushAfterExplicitFlushBeforeReadConfig()
        throws Exception {

        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .addFlushCommand()
                    .done()
                .addReadConfigEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .addFlushCommand()
                    .done()
                .addReadConfigEvent()
                    .done()
                .done()
            .done();

        InjectFlushVisitor injectFlush = new InjectFlushVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectFlush, new InjectFlushVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }
}
