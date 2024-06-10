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
package io.aklivity.k3po.runtime.junit.internal;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Latch {

    enum State { INIT, PREPARED, STARTABLE, FINISHED }

    private volatile State state;
    private volatile Exception exception;

    private final CountDownLatch prepared;
    private final CountDownLatch startable;
    private final CountDownLatch finished;
    private volatile Thread testThread;
    
    private final AtomicBoolean testThreadInterrupted = new AtomicBoolean(false); 

    public Latch() {
        state = State.INIT;

        prepared = new CountDownLatch(1);
        startable = new CountDownLatch(1);
        finished = new CountDownLatch(1);
    }

    public void notifyPrepared() {
        switch (state) {
        case INIT:
            state = State.PREPARED;
            prepared.countDown();
            break;
        default:
            throw new IllegalStateException(state.name());
        }
    }

    public void awaitPrepared() throws Exception {
        prepared.await();
        if (exception != null) {
            throw exception;
        }
    }

    public boolean isPrepared() {
        return prepared.getCount() == 0L;
    }

    public boolean isInInitState() {
        return this.state == State.INIT;
    }

    public void notifyStartable() {
        switch (state) {
        case PREPARED:
            state = State.STARTABLE;
            startable.countDown();
            break;
        case STARTABLE:
        case FINISHED:
            // its all right to call this multiple times if its prepared
            break;
        default:
            throw new IllegalStateException(state.name());
        }
    }

    public void awaitStartable() throws Exception {
        startable.await();
        if (exception != null) {
            throw exception;
        }
    }

    public boolean isStartable() {
        return startable.getCount() == 0L;
    }

    public void notifyFinished() {
        switch (state) {
        case INIT:
            notifyPrepared();
        // We could abort before started.
        case PREPARED:
        case STARTABLE:
            state = State.FINISHED;
            finished.countDown();
            break;
        default:
            throw new IllegalStateException(state.name());
        }
    }

    public void notifyAbort() {
        switch (state) {
        case INIT:
            notifyPrepared();
        case PREPARED:
            notifyStartable();
            break;
        default:
        }
    }

    public void awaitFinished() throws Exception {
        finished.await();
    }

    public boolean isFinished() {
        return finished.getCount() == 0L;
    }

    public boolean hasException() {
        return exception != null;
    }

    public void notifyException(Exception exception) {
        this.exception = Objects.requireNonNull(exception);

        if (! Thread.currentThread().equals(testThread) 
                && testThreadInterrupted.compareAndSet(false, true)) {
            testThread.interrupt();
        }
    }

    public void setInterruptOnException(Thread testThread) {
        this.testThread = testThread;
    }

    public Exception getException() {
		return exception;
	}
}
