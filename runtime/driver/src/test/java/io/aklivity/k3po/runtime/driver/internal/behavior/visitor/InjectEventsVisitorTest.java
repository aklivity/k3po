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

import io.aklivity.k3po.runtime.driver.internal.behavior.parser.Parser;
import io.aklivity.k3po.runtime.driver.internal.behavior.visitor.InjectEventsVisitor;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAdviseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAdvisedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstScriptNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAdviseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAdvisedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstScriptNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseException;
import io.aklivity.k3po.runtime.lang.internal.parser.ScriptParser;

public class InjectEventsVisitorTest {

    @Test
    public void shouldNotInjectBeforeOpened()
        throws Exception {

        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .done()
            .done();

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldInjectOpenedBeforeBound()
        throws Exception {

        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addBoundEvent()
                    .done()
                .done()
            .done();
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldInjectOpenedAndBoundBeforeConnected()
        throws Exception {

        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .done()
            .done();
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldInjectBoundBetweenOpenedBeforeConnected()
        throws Exception {

        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .done()
            .done();
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldNotInjectBeforeDisconnected()
        throws Exception {

        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .addDisconnectedEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .addDisconnectedEvent()
                    .done()
                .done()
            .done();
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldInjectDisconnectedBeforeUnbound()
        throws Exception {
        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .addDisconnectedEvent()
                    .done()
                .addUnboundEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .addUnboundEvent()
                    .done()
                .done()
            .done();
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldInjectDisconnectedAndUnboundBeforeClosed()
        throws Exception {
        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .addDisconnectedEvent()
                    .done()
                .addUnboundEvent()
                    .done()
                .addClosedEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .addClosedEvent()
                    .done()
                .done()
            .done();
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldInjectUnboundBetweenDisconnectedAndClosed()
        throws Exception {

        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .addDisconnectedEvent()
                    .done()
                .addUnboundEvent()
                    .done()
                .addClosedEvent()
                    .done()
                .done()
            .done();

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .addDisconnectedEvent()
                    .done()
                .addClosedEvent()
                    .done()
                .done()
            .done();
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    // These validation tests ensure that the InjectEventsVisitor continues
    // to throw exceptions on malformed (connectivity-wise) streams

    @Test(expected = ScriptParseException.class)
    public void shouldNotParseScriptWithoutConnect()
        throws Exception {

        String script =
            "# tcp.client.connect-then-close\n" +
            "connected\n" +
            "close\n" +
            "closed\n";

        ScriptParser parser = new Parser();
        parser.parse(script);
    }

    @Test(expected = ScriptParseException.class)
    public void shouldNotParseScriptWithMultiplyClosedStream()
        throws Exception {

        String script =
            "# tcp.client.connect-then-close\n" +
            "connect 'tcp://localhost:7788'\n" +
            "connected\n" +
            "close\n" +
            "closed\n" +
            "close\n" +
            "closed\n";

        ScriptParser parser = new Parser();
        parser.parse(script);
    }

    @Test
    public void shouldInjectOpenedAndBoundBeforeReadAdviseAndConnected()
        throws Exception {

        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .done()
            .done();
        AstReadAdviseNode expectedAdvise = new AstReadAdviseNode();
        expectedScriptNode.getStreams().get(0).getStreamables().add(expectedAdvise);
        AstConnectedNode expectedConnected = new AstConnectedNode();
        expectedScriptNode.getStreams().get(0).getStreamables().add(expectedConnected);

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .done()
            .done();
        AstReadAdviseNode inputAdvise = new AstReadAdviseNode();
        inputScriptNode.getStreams().get(0).getStreamables().add(inputAdvise);
        AstConnectedNode inputConnected = new AstConnectedNode();
        inputScriptNode.getStreams().get(0).getStreamables().add(inputConnected);
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldInjectOpenedAndBoundBeforeReadAdvisedAndConnected()
        throws Exception {

        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .done()
            .done();
        AstReadAdvisedNode expectedAdvised = new AstReadAdvisedNode();
        expectedScriptNode.getStreams().get(0).getStreamables().add(expectedAdvised);
        AstConnectedNode expectedConnected = new AstConnectedNode();
        expectedScriptNode.getStreams().get(0).getStreamables().add(expectedConnected);

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .done()
            .done();
        AstReadAdvisedNode inputAdvised = new AstReadAdvisedNode();
        inputScriptNode.getStreams().get(0).getStreamables().add(inputAdvised);
        AstConnectedNode inputConnected = new AstConnectedNode();
        inputScriptNode.getStreams().get(0).getStreamables().add(inputConnected);
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldInjectOpenedAndBoundBeforeWriteAdviseAndConnected()
        throws Exception {

        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .done()
            .done();
        AstWriteAdviseNode expectedAdvise = new AstWriteAdviseNode();
        expectedScriptNode.getStreams().get(0).getStreamables().add(expectedAdvise);
        AstConnectedNode expectedConnected = new AstConnectedNode();
        expectedScriptNode.getStreams().get(0).getStreamables().add(expectedConnected);

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .done()
            .done();
        AstWriteAdviseNode inputAdvise = new AstWriteAdviseNode();
        inputScriptNode.getStreams().get(0).getStreamables().add(inputAdvise);
        AstConnectedNode inputConnected = new AstConnectedNode();
        inputScriptNode.getStreams().get(0).getStreamables().add(inputConnected);
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldInjectOpenedAndBoundBeforeWriteAdvisedAndConnected()
        throws Exception {

        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .done()
            .done();
        AstWriteAdvisedNode expectedAdvised = new AstWriteAdvisedNode();
        expectedScriptNode.getStreams().get(0).getStreamables().add(expectedAdvised);
        AstConnectedNode expectedConnected = new AstConnectedNode();
        expectedScriptNode.getStreams().get(0).getStreamables().add(expectedConnected);

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .done()
            .done();
        AstWriteAdvisedNode inputAdvised = new AstWriteAdvisedNode();
        inputScriptNode.getStreams().get(0).getStreamables().add(inputAdvised);
        AstConnectedNode inputConnected = new AstConnectedNode();
        inputScriptNode.getStreams().get(0).getStreamables().add(inputConnected);
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }

    @Test
    public void shouldKeepReadAdviseAfterConnectedUnchanged()
        throws Exception {

        // @formatter:off
        AstScriptNode expectedScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addOpenedEvent()
                    .done()
                .addBoundEvent()
                    .done()
                .addConnectedEvent()
                    .done()
                .done()
            .done();
        AstReadAdviseNode expectedAdvise = new AstReadAdviseNode();
        expectedScriptNode.getStreams().get(0).getStreamables().add(expectedAdvise);

        AstScriptNode inputScriptNode = new AstScriptNodeBuilder()
            .addConnectStream()
                .addConnectedEvent()
                    .done()
                .done()
            .done();
        AstReadAdviseNode inputAdvise = new AstReadAdviseNode();
        inputScriptNode.getStreams().get(0).getStreamables().add(inputAdvise);
        // @formatter:on

        InjectEventsVisitor injectEvents = new InjectEventsVisitor();
        AstScriptNode actualScriptNode = inputScriptNode.accept(injectEvents, new InjectEventsVisitor.State());

        assertEquals(expectedScriptNode, actualScriptNode);
    }
}
