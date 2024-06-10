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
package io.aklivity.k3po.runtime.driver.internal.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FinishedMessage extends ControlMessage {

    private String script = "";
    private final List<String> completedBarriers;
    private final List<String> incompleteBarriers;
    
    public FinishedMessage() {
        super();
        this.completedBarriers = new ArrayList<>();
        this.incompleteBarriers = new ArrayList<>();
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKind(), script);
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || (obj instanceof FinishedMessage) && equals((FinishedMessage) obj);
    }

    @Override
    public Kind getKind() {
        return Kind.FINISHED;
    }

    protected final boolean equals(FinishedMessage that) {
        return super.equalTo(that) && Objects.equals(this.script, that.script);
    }

    public List<String> getCompletedBarriers() {
        return completedBarriers;
    }

    public List<String> getIncompleteBarriers() {
        return incompleteBarriers;
    }
}
