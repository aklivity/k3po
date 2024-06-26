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
package io.aklivity.k3po.runtime.driver.internal.behavior.parser;

import io.aklivity.k3po.runtime.driver.internal.behavior.visitor.AssociateStreamsVisitor;
import io.aklivity.k3po.runtime.driver.internal.behavior.visitor.InjectBarriersVisitor;
import io.aklivity.k3po.runtime.driver.internal.behavior.visitor.InjectEventsVisitor;
import io.aklivity.k3po.runtime.driver.internal.behavior.visitor.InjectFlushVisitor;
import io.aklivity.k3po.runtime.lang.internal.ast.AstScriptNode;
import io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseException;
import io.aklivity.k3po.runtime.lang.internal.parser.ScriptParser;
import io.aklivity.k3po.runtime.lang.internal.parser.ScriptParserImpl;

public class Parser implements ScriptParser {

    private final ScriptParser parser;

    public Parser() {
        parser = new ScriptParserImpl();
    }

    @Override
    public AstScriptNode parse(String input) throws ScriptParseException {

        try {
            AstScriptNode script = parser.parse(input);

            InjectEventsVisitor injectEvents = new InjectEventsVisitor();
            InjectEventsVisitor.State injectEventsState = new InjectEventsVisitor.State();
            script = script.accept(injectEvents, injectEventsState);

            InjectBarriersVisitor injectBarriers = new InjectBarriersVisitor();
            script = script.accept(injectBarriers, new InjectBarriersVisitor.State());

            InjectFlushVisitor injectFlush = new InjectFlushVisitor();
            script = script.accept(injectFlush, new InjectFlushVisitor.State());

            AssociateStreamsVisitor associateStreams = new AssociateStreamsVisitor();
            script = script.accept(associateStreams, new AssociateStreamsVisitor.State());

            return script;

        } catch (ScriptParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ScriptParseException(e);
        }
    }
}
