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
package io.aklivity.k3po.runtime.control.internal.command;

import static io.aklivity.k3po.runtime.control.internal.command.Command.Kind.NOTIFY;

import java.util.Objects;

public class NotifyCommand extends Command {
    private String barrier;

    @Override
    public Kind getKind() {
        return NOTIFY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKind());
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof NotifyCommand && equalTo((NotifyCommand) o);
    }

    protected boolean equalTo(NotifyCommand that) {
        return super.equalTo(that) && Objects.equals(this.getBarrier(), that.getBarrier());
    }

    public String getBarrier() {
        return barrier;
    }

    public void setBarrier(String barrier) {
        this.barrier = barrier;
    }
}
