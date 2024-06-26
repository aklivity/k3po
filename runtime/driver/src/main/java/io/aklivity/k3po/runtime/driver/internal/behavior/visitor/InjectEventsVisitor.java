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

import io.aklivity.k3po.runtime.driver.internal.behavior.visitor.InjectEventsVisitor.State;
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

public class InjectEventsVisitor implements AstNode.Visitor<AstScriptNode, State> {

    public enum ConnectivityState {
        NONE, OPENED, BOUND, CONNECTED, DISCONNECTED, UNBOUND, CLOSED
    }

    public static final class State {
        private List<AstStreamNode> streams;
        private List<AstStreamableNode> streamables;
        private ConnectivityState connectivityState;
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

        state.connectivityState = ConnectivityState.NONE;

        AstAcceptNode newAcceptNode = new AstAcceptNode(acceptNode);

        state.streamables = newAcceptNode.getStreamables();
        for (AstStreamableNode streamable : acceptNode.getStreamables()) {
            streamable.accept(this, state);
        }

        for (AstAcceptableNode acceptable : acceptNode.getAcceptables()) {
            acceptable.accept(this, state);
        }

        state.streams.add(newAcceptNode);

        return null;
    }

    @Override
    public AstScriptNode visit(AstAcceptedNode acceptedNode, State state) {

        state.connectivityState = ConnectivityState.NONE;

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

        state.connectivityState = ConnectivityState.NONE;

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

        state.connectivityState = ConnectivityState.NONE;

        AstConnectNode newConnectNode = new AstConnectNode(connectNode);

        state.streamables = newConnectNode.getStreamables();
        for (AstStreamableNode streamable : connectNode.getStreamables()) {
            streamable.accept(this, state);
        }

        state.streams.add(newConnectNode);

        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAwaitNode node, State state) {
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

        switch (state.connectivityState) {
        case CONNECTED:
            state.streamables.add(node);
            break;

        default:
            throw new IllegalStateException("Unexpected write before connected");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstDisconnectNode node, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
            state.streamables.add(node);
            break;

        default:
            throw new IllegalStateException("Unexpected disconnect before connected");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstUnbindNode node, State state) {

        switch (state.connectivityState) {
        case DISCONNECTED:
            state.streamables.add(node);
            break;

        default:
            throw new IllegalStateException("Unexpected unbind before disconnected");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstCloseNode node, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
            state.streamables.add(node);
            break;

        default:
            throw new IllegalStateException("Unexpected close before connected");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAbortNode node, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
            state.streamables.add(node);
            break;

        default:
            throw new IllegalStateException("Unexpected write abort before connected");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAbortNode node, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
            state.streamables.add(node);
            break;

        default:
            throw new IllegalStateException("Unexpected read abort before connected");
        }

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

        switch (state.connectivityState) {
        case NONE:
            state.connectivityState = ConnectivityState.OPENED;
            state.streamables.add(openedNode);
            break;
        default:
            throw new IllegalStateException("Unexpected event: opened");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstBoundNode boundNode, State state) {

        switch (state.connectivityState) {
        case NONE:
            AstOpenedNode openedNode = new AstOpenedNode();
            openedNode.setRegionInfo(boundNode.getRegionInfo());
            openedNode.accept(this, state);
            break;
        default:
            break;
        }

        // The above switch might have changed the connectivity state, so
        // we switch on it again
        switch (state.connectivityState) {
        case OPENED:
            state.streamables.add(boundNode);
            state.connectivityState = ConnectivityState.BOUND;
            break;
        default:
            throw new IllegalStateException("Unexpected event: bound");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstConnectedNode connectedNode, State state) {

        switch (state.connectivityState) {
        case NONE:
        case OPENED:
            AstBoundNode boundNode = new AstBoundNode();
            boundNode.setRegionInfo(connectedNode.getRegionInfo());
            boundNode.accept(this, state);
            break;
        default:
            break;
        }

        // The above switch might have changed the connectivity state, so
        // we switch on it again
        switch (state.connectivityState) {
        case BOUND:
            state.streamables.add(connectedNode);
            state.connectivityState = ConnectivityState.CONNECTED;
            break;

        default:
            throw new IllegalStateException("Unexpected event: connected");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstConnectAbortNode connectAbortNode, State state) {

        switch (state.connectivityState) {
        case NONE:
        case OPENED:
            AstBoundNode boundNode = new AstBoundNode();
            boundNode.setRegionInfo(connectAbortNode.getRegionInfo());
            boundNode.accept(this, state);
            break;
        default:
            break;
        }

        // The above switch might have changed the connectivity state, so
        // we switch on it again
        switch (state.connectivityState) {
        case BOUND:
            state.streamables.add(connectAbortNode);
            state.connectivityState = ConnectivityState.CLOSED;
            break;

        default:
            throw new IllegalStateException("Unexpected connect abort");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstConnectAbortedNode connectAbortedNode, State state) {

        switch (state.connectivityState) {
        case NONE:
        case OPENED:
            AstBoundNode boundNode = new AstBoundNode();
            boundNode.setRegionInfo(connectAbortedNode.getRegionInfo());
            boundNode.accept(this, state);
            break;
        default:
            break;
        }

        // The above switch might have changed the connectivity state, so
        // we switch on it again
        switch (state.connectivityState) {
        case BOUND:
            state.streamables.add(connectAbortedNode);
            state.connectivityState = ConnectivityState.CLOSED;
            break;

        default:
            throw new IllegalStateException("Unexpected event: connect aborted");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstReadValueNode node, State state) {

        switch (state.connectivityState) {
            case CONNECTED:
            state.streamables.add(node);
            break;

        default:
            throw new IllegalStateException("Unexpected read before connected");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstDisconnectedNode disconnectedNode, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
            state.streamables.add(disconnectedNode);
            state.connectivityState = ConnectivityState.DISCONNECTED;
            break;

        default:
            throw new IllegalStateException("Unexpected event: disconnected");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstUnboundNode unboundNode, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
            AstDisconnectedNode disconnectedNode = new AstDisconnectedNode();
            disconnectedNode.setRegionInfo(unboundNode.getRegionInfo());
            disconnectedNode.accept(this, state);
            break;
        default:
            break;
        }

        switch (state.connectivityState) {
        case DISCONNECTED:
            state.streamables.add(unboundNode);
            state.connectivityState = ConnectivityState.UNBOUND;
            break;

        default:
            throw new IllegalStateException("Unexpected event: unbound");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstClosedNode closedNode, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
        case DISCONNECTED:
            AstUnboundNode unboundNode = new AstUnboundNode();
            unboundNode.setRegionInfo(closedNode.getRegionInfo());
            unboundNode.accept(this, state);
            break;
        default:
            break;
        }

        switch (state.connectivityState) {
        case UNBOUND:
            state.streamables.add(closedNode);
            state.connectivityState = ConnectivityState.CLOSED;
            break;

        default:
            throw new IllegalStateException("Unexpected event: closed");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAbortedNode node, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
            state.streamables.add(node);
            break;

        default:
            throw new IllegalStateException("Unexpected read aborted before connected");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAbortedNode node, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
            state.streamables.add(node);
            break;

        default:
            throw new IllegalStateException("Unexpected write aborted before connected");
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstReadConfigNode node, State state) {

        switch (state.connectivityState) {
        case NONE:
        case CONNECTED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected \"%s\" before connected", node));
        }
        state.streamables.add(node);

        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteConfigNode node, State state) {

        switch (state.connectivityState) {
        case NONE:
        case CONNECTED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected \"%s\" before connected", node));
        }
        state.streamables.add(node);

        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAdviseNode node, State state) {
        switch (state.connectivityState) {
        case NONE:
        case CONNECTED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected \"%s\" before connected", node));
        }
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAdviseNode node, State state) {
        switch (state.connectivityState) {
        case NONE:
        case CONNECTED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected \"%s\" before connected", node));
        }
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAdvisedNode node, State state) {
        switch (state.connectivityState) {
        case NONE:
        case CONNECTED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected \"%s\" before connected", node));
        }
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAdvisedNode node, State state) {
        switch (state.connectivityState) {
        case NONE:
        case CONNECTED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected \"%s\" before connected", node));
        }
        state.streamables.add(node);
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadClosedNode node, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected \"%s\" before connected", node));
        }
        state.streamables.add(node);

        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteCloseNode node, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected \"%s\" before connected", node));
        }
        state.streamables.add(node);

        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteFlushNode node, State state) {

        switch (state.connectivityState) {
        case CONNECTED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected \"%s\" before connected", node));
        }
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

}
