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

import java.util.List;

import io.aklivity.k3po.runtime.driver.internal.behavior.visitor.InjectBarriersVisitor.State;
import io.aklivity.k3po.runtime.lang.internal.RegionInfo;
import io.aklivity.k3po.runtime.lang.internal.ast.AstAcceptNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstAcceptableNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstAcceptedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstBoundNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstChildClosedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstChildOpenedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstCloseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstClosedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectAbortNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectAbortedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstDisconnectNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstDisconnectedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstOpenedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstPropertyNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAbortNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAbortedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAdviseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAdvisedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAwaitNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadClosedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadConfigNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadNotifyNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadOptionNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadValueNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstRejectedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstScriptNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstStreamNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstStreamableNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstUnbindNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstUnboundNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAbortNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAbortedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAdviseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAdvisedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAwaitNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteCloseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteConfigNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteFlushNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteNotifyNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteOptionNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteValueNode;

public class InjectBarriersVisitor implements AstNode.Visitor<AstScriptNode, State> {

    public enum ReadWriteState {
        NONE, READ, WRITE
    }

    public static final class State {
        private List<AstStreamNode> streams;
        private List<AstStreamableNode> streamables;
        private ReadWriteState readWriteState;
        private int readWriteBarrierCount;
    }

    @Override
    public AstScriptNode visit(AstScriptNode script, State state) {

        AstScriptNode newScript = new AstScriptNode();
        newScript.setRegionInfo(script.getRegionInfo());
        newScript.getProperties().addAll(script.getProperties());

        state.streams = newScript.getStreams();

        for (AstStreamNode stream : script.getStreams()) {
            stream.accept(this, state);
        }

        return newScript;
    }

    @Override
    public AstScriptNode visit(AstPropertyNode propertyNode, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstAcceptNode acceptNode, State state) {

        state.readWriteState = ReadWriteState.NONE;

        AstAcceptNode newAcceptNode = new AstAcceptNode(acceptNode);

        state.streamables = newAcceptNode.getStreamables();
        for (AstStreamableNode streamable : acceptNode.getStreamables()) {
            streamable.accept(this, state);
        }

        for (AstAcceptableNode acceptableNode : acceptNode.getAcceptables()) {
            acceptableNode.accept(this, state);
        }

        state.streams.add(newAcceptNode);

        return null;
    }

    @Override
    public AstScriptNode visit(AstAcceptedNode acceptedNode, State state) {

        state.readWriteState = ReadWriteState.NONE;

        AstAcceptedNode newAcceptedNode = new AstAcceptedNode();
        newAcceptedNode.setRegionInfo(acceptedNode.getRegionInfo());
        newAcceptedNode.setAcceptName(acceptedNode.getAcceptName());

        state.streamables = newAcceptedNode.getStreamables();
        for (AstStreamableNode streamable : acceptedNode.getStreamables()) {
            streamable.accept(this, state);
        }

        state.streams.add(newAcceptedNode);

        return null;
    }

    @Override
    public AstScriptNode visit(AstRejectedNode rejectedNode, State state) {

        state.readWriteState = ReadWriteState.NONE;

        AstRejectedNode newRejectedNode = new AstRejectedNode();
        newRejectedNode.setRegionInfo(rejectedNode.getRegionInfo());
        newRejectedNode.setAcceptName(rejectedNode.getAcceptName());

        state.streamables = newRejectedNode.getStreamables();
        for (AstStreamableNode streamable : rejectedNode.getStreamables()) {
            streamable.accept(this, state);
        }

        state.streams.add(newRejectedNode);

        return null;
    }

    @Override
    public AstScriptNode visit(AstConnectNode connectNode, State state) {

        state.readWriteState = ReadWriteState.NONE;

        AstConnectNode newConnectNode = new AstConnectNode(connectNode);

        state.streamables = newConnectNode.getStreamables();
        for (AstStreamableNode streamable : connectNode.getStreamables()) {
            streamable.accept(this, state);
        }

        state.streams.add(newConnectNode);

        return null;
    }

    @Override
    public AstScriptNode visit(AstConnectAbortNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstConnectAbortedNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAwaitNode node, State state) {

        state.readWriteState = ReadWriteState.NONE;
        state.streamables.add(node);

        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAwaitNode node, State state) {

        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadNotifyNode node, State state) {

        state.readWriteState = ReadWriteState.NONE;
        state.streamables.add(node);

        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteNotifyNode node, State state) {

        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteValueNode node, State state) {

        conditionallyInjectWriteBarrier(state, node.getRegionInfo());
        state.streamables.add(node);
        state.readWriteState = ReadWriteState.WRITE;

        return null;
    }

    @Override
    public AstScriptNode visit(AstDisconnectNode node, State state) {

        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstUnbindNode node, State state) {

        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstCloseNode node, State state) {

        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAbortNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAbortNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAbortedNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAbortedNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstChildOpenedNode childOpenedNode, State state) {
        state.streamables.add(childOpenedNode);
        return null;
    }

    @Override
    public AstScriptNode visit(AstChildClosedNode childClosedNode, State state) {

        state.streamables.add(childClosedNode);
        return null;
    }

    @Override
    public AstScriptNode visit(AstOpenedNode openedNode, State state) {

        state.streamables.add(openedNode);
        return null;
    }

    @Override
    public AstScriptNode visit(AstBoundNode boundNode, State state) {

        state.streamables.add(boundNode);
        return null;
    }

    @Override
    public AstScriptNode visit(AstConnectedNode connectedNode, State state) {

        state.streamables.add(connectedNode);
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadValueNode node, State state) {

        state.streamables.add(node);
        state.readWriteState = ReadWriteState.READ;
        return null;
    }

    @Override
    public AstScriptNode visit(AstDisconnectedNode disconnectedNode, State state) {

        state.streamables.add(disconnectedNode);
        return null;
    }

    @Override
    public AstScriptNode visit(AstUnboundNode unboundNode, State state) {

        state.streamables.add(unboundNode);
        return null;
    }

    @Override
    public AstScriptNode visit(AstClosedNode closedNode, State state) {

        state.streamables.add(closedNode);
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadConfigNode node, State state) {
        state.streamables.add(node);
        state.readWriteState = ReadWriteState.READ;
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteConfigNode node, State state) {
        conditionallyInjectWriteBarrier(state, node.getRegionInfo());
        state.streamables.add(node);
        state.readWriteState = ReadWriteState.WRITE;
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAdviseNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAdviseNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAdvisedNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAdvisedNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadClosedNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteCloseNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteFlushNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadOptionNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteOptionNode node, State state) {
        state.streamables.add(node);
        return null;
    }

    private void conditionallyInjectWriteBarrier(State state, RegionInfo regionInfo) {
        List<AstStreamableNode> streamables = state.streamables;

        switch (state.readWriteState) {
        case READ:
            String barrierName = String.format("~read~write~%d", ++state.readWriteBarrierCount);
            AstReadNotifyNode readNotify = new AstReadNotifyNode();
            readNotify.setRegionInfo(regionInfo);
            readNotify.setBarrierName(barrierName);
            AstWriteAwaitNode writeAwait = new AstWriteAwaitNode();
            writeAwait.setRegionInfo(regionInfo);
            writeAwait.setBarrierName(barrierName);
            streamables.add(readNotify);
            streamables.add(writeAwait);
            break;
        default:
            break;
        }
    }

}
