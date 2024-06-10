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

import io.aklivity.k3po.runtime.lang.internal.ast.AstAcceptNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstAcceptableNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstAcceptedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstBoundNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstChildClosedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstChildOpenedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstCloseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstClosedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstCommandNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectAbortNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectAbortedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstDisconnectNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstDisconnectedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstEventNode;
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

// Note: this is no longer injecting, just validating, as injection is now generalized
public class ValidateStreamsVisitor implements AstNode.Visitor<AstScriptNode, ValidateStreamsVisitor.State> {

    public enum StreamState {
        // @formatter:off
        OPEN,
        CLOSED,
        // @formatter:on
    }

    public static final class State {
        private StreamState readState;
        private StreamState writeState;

        public State() {
            readState = StreamState.OPEN;
            writeState = StreamState.OPEN;
        }
    }

    @Override
    public AstScriptNode visit(AstScriptNode script, State state) {
        for (AstStreamNode stream : script.getStreams()) {
            stream.accept(this, state);
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstPropertyNode propertyNode, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstAcceptNode acceptNode, State state) {

        for (AstStreamableNode streamable : acceptNode.getStreamables()) {
            streamable.accept(this, state);
        }

        for (AstAcceptableNode acceptable : acceptNode.getAcceptables()) {
            state.readState = StreamState.OPEN;
            state.writeState = StreamState.OPEN;
            acceptable.accept(this, state);

        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstConnectNode connectNode, State state) {

        state.writeState = StreamState.OPEN;
        state.readState = StreamState.OPEN;

        for (AstStreamableNode streamable : connectNode.getStreamables()) {
            streamable.accept(this, state);
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstConnectAbortNode node, State state) {

        switch (state.writeState) {
        case OPEN:
            state.readState = StreamState.CLOSED;
            break;
        default:
            throw new IllegalStateException(unexpectedInWriteState(node, state));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstConnectAbortedNode node, State state) {

        switch (state.writeState) {
        case OPEN:
            state.readState = StreamState.CLOSED;
            break;
        default:
            throw new IllegalStateException(unexpectedInWriteState(node, state));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadConfigNode node, State state) {

        switch (state.readState) {
        case OPEN:
        case CLOSED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected read config event (%s) while reading in state %s", node
                    .toString().trim(), state.readState));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteConfigNode node, State state) {

        switch (state.writeState) {
        case OPEN:
        case CLOSED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected write config command (%s) while writing in state %s", node
                    .toString().trim(), state.writeState));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAdviseNode node, State state) {

        switch (state.readState) {
        case OPEN:
        case CLOSED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected read advise command (%s) while writing in state %s", node
                    .toString().trim(), state.writeState));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAdvisedNode node, State state) {

        switch (state.readState) {
        case OPEN:
        case CLOSED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected read advised event (%s) while writing in state %s", node
                    .toString().trim(), state.writeState));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAdviseNode node, State state) {

        switch (state.writeState) {
        case OPEN:
        case CLOSED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected write advise command (%s) while writing in state %s", node
                    .toString().trim(), state.writeState));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAdvisedNode node, State state) {

        switch (state.writeState) {
        case OPEN:
        case CLOSED:
            break;
        default:
            throw new IllegalStateException(String.format("Unexpected write advised event (%s) while writing in state %s", node
                    .toString().trim(), state.writeState));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadClosedNode node, State state) {

        switch (state.readState) {
        case OPEN:
            state.readState = StreamState.CLOSED;
            break;
        default:
            throw new IllegalStateException(unexpectedInReadState(node, state));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteCloseNode node, State state) {

        switch (state.writeState) {
        case OPEN:
            state.writeState = StreamState.CLOSED;
            break;
        default:
            throw new IllegalStateException(unexpectedInWriteState(node, state));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAbortNode node, State state) {
        switch (state.readState) {
        case OPEN:
        case CLOSED:
            state.writeState = StreamState.CLOSED;
            break;
        default:
            throw new IllegalStateException(unexpectedInReadState(node, state));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAbortNode node, State state) {
        switch (state.readState) {
        case OPEN:
        case CLOSED:
            state.readState = StreamState.CLOSED;
            break;
        default:
            throw new IllegalStateException(unexpectedInReadState(node, state));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAbortedNode node, State state) {

        switch (state.writeState) {
        case OPEN:
        case CLOSED:
            state.readState = StreamState.CLOSED;
            break;
        default:
            throw new IllegalStateException(unexpectedInWriteState(node, state));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAbortedNode node, State state) {

        switch (state.writeState) {
        case OPEN:
        case CLOSED:
            state.writeState = StreamState.CLOSED;
            break;
        default:
            throw new IllegalStateException(unexpectedInWriteState(node, state));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadValueNode node, State state) {

        switch (state.readState) {
        case OPEN:
            break;
        default:
            throw new IllegalStateException(unexpectedInReadState(node, state));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteValueNode node, State state) {

        switch (state.writeState) {
        case OPEN:
            break;
        default:
            throw new IllegalStateException(unexpectedInWriteState(node, state));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteFlushNode node, State state) {

        switch (state.writeState) {
        case OPEN:
            break;
        default:
            throw new IllegalStateException(unexpectedInWriteState(node, state));
        }
        return null;
    }

    @Override
    public AstScriptNode visit(AstAcceptedNode node, State state) {

        for (AstStreamableNode streamable : node.getStreamables()) {
            streamable.accept(this, state);
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstRejectedNode node, State state) {

        for (AstStreamableNode streamable : node.getStreamables()) {
            streamable.accept(this, state);
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstDisconnectNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstUnbindNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstCloseNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstChildOpenedNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstChildClosedNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstOpenedNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstBoundNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstConnectedNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstDisconnectedNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstUnboundNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstClosedNode node, State state) {
        switch (state.readState) {
        case OPEN:
            state.readState = StreamState.CLOSED;
            break;
        case CLOSED:
            break;
        default:
            throw new IllegalStateException(unexpectedInReadState(node, state));
        }

        switch (state.writeState) {
        case OPEN:
            state.writeState = StreamState.CLOSED;
            break;
        case CLOSED:
            break;
        default:
            throw new IllegalStateException(unexpectedInWriteState(node, state));
        }

        return null;
    }

    @Override
    public AstScriptNode visit(AstReadAwaitNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteAwaitNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadNotifyNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteNotifyNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstReadOptionNode node, State state) {
        return null;
    }

    @Override
    public AstScriptNode visit(AstWriteOptionNode node, State state) {
        return null;
    }

    private String unexpectedInReadState(AstNode node, State state) {
        return String.format("Unexpected %s while reading in state %s", description(node), state.readState);
    }

    private String unexpectedInWriteState(AstNode node, State state) {
        return String.format("Unexpected %s while writing in state %s", description(node), state.writeState);
    }

    private String description(AstNode node) {
        String description = node.toString().trim();

        if (node instanceof AstEventNode) {
            description = String.format("event (%s)", description);
        }
        else if (node instanceof AstCommandNode) {
            description = String.format("command (%s)", description);
        }

        return description;
    }
}
