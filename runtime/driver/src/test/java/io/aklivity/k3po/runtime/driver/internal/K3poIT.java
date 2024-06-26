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
package io.aklivity.k3po.runtime.driver.internal;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.rules.RuleChain.outerRule;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import io.aklivity.k3po.runtime.driver.internal.RobotServer;
import io.aklivity.k3po.runtime.driver.internal.test.utils.K3poTestRule;
import io.aklivity.k3po.runtime.driver.internal.test.utils.TestSpecification;

public class K3poIT {
    private final K3poTestRule k3po = new K3poTestRule()
        .setScriptRoot("io/aklivity/k3po/specs/control");

    private final TestRule timeout = new DisableOnDebug(new Timeout(3, SECONDS));

    private RobotServer robot;

    @Before
    public void setupRobot() throws Exception {
        robot = new RobotServer(URI.create("tcp://localhost:12345"), false,
            new URLClassLoader(new URL[]{new File("src/test/scripts").toURI().toURL()}));
        robot.start();
    }

    @After
    public void shutdownRobot() throws Exception {
        robot.stop();
    }

    @Rule
    public final TestRule chain = outerRule(k3po).around(timeout);

    @TestSpecification("connect.finished.empty")
    @Test
    public void connectFinishEmpty() throws Exception {
        k3po.finish();
    }

    @TestSpecification("connect.finished")
    @Test
    public void connectFinished() throws Exception {
        k3po.finish();
    }

    @TestSpecification("connect.finished.with.override.properties")
    @Test
    public void connectFinishedWithOverrideProperties() throws Exception {
        k3po.finish();
    }

    @TestSpecification("connect.finished.with.barriers")
    @Test
    public void connectFinishedWithBarriers() throws Exception {
        k3po.finish();
    }

    @Test
    @TestSpecification({ "connect.abort.after.prepare" })
    public void shouldPrepareThenAbort() throws Exception {
        k3po.finish();
    }

    @Test
    @TestSpecification({ "connect.abort.after.await" })
    public void shouldPrepareAwaitThenAbort() throws Exception {
        k3po.finish();
    }

    @Test
    @TestSpecification({ "connect.abort.after.finished" })
    public void shouldFinishThenAbort() throws Exception {
        k3po.finish();
    }

    @Test
    @TestSpecification({ "connect.failed.prepare" })
    public void shouldPrepareAfterFailedPrepare() throws Exception {
        k3po.finish();
    }

    @Test
    @TestSpecification({ "connect.failed.prepare.version" })
    public void shouldFailPrepareWithIncorrectVersion() throws Exception {
        k3po.finish();
    }

    @Test
    @TestSpecification({ "connect.error.if.no.prepare" })
    public void shouldFailIfNotPrepared() throws Exception {
        k3po.finish();
    }

    @Test
    @TestSpecification({ "connect.error.if.prepare.failed" })
    public void shouldFailIfPrepareFailed() throws Exception {
        k3po.finish();
    }

    @Test
    @TestSpecification({ "connect.already.prepared.or.started" })
    public void shouldFailIfAlreadyPreparedOrStarted() throws Exception {
        k3po.finish();
    }

    @Test
    @TestSpecification({ "connect.incorrect.barrier.name" })
    public void shouldFailWithIncorrectBarrierName() throws Exception {
        k3po.finish();
    }
}
