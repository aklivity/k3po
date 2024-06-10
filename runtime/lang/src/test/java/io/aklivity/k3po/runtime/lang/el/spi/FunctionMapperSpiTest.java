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
package io.aklivity.k3po.runtime.lang.el.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import javax.el.ELException;

import org.junit.Test;

import io.aklivity.k3po.runtime.lang.el.Function;

public class FunctionMapperSpiTest {

    public static class Functions {
        @Function
        public static int add(int left, int right) {
            return left + right;
        }

        @Function(
                name = "add3")
        public static int add(int left, int middle, int right) {
            return left + middle + right;
        }
    }

    public static class FunctionsOfReality {
        // Since we annotated multiple methods with the same function name,
        // resolving this class should cause a problem.

        @Function(
                name = "collision")
        public static void unstoppableForce() {
        }

        @Function(
                name = "collision")
        public static void immovableObject() {
        }
    }

    @Test
    public void shouldResolveAddFunction() throws Exception {

        FunctionMapperSpi mapper = new FunctionMapperSpi.Reflective(Functions.class) {
            @Override
            public String getPrefixName() {
                return "test";
            }
        };

        Method add = mapper.resolveFunction("add");
        assertNotNull(add);

        assertEquals(1 + 2, add.invoke(null, 1, 2));
    }

    @Test
    public void shouldResolveAdd3Function() throws Exception {

        FunctionMapperSpi mapper = new FunctionMapperSpi.Reflective(Functions.class) {
            @Override
            public String getPrefixName() {
                return "test";
            }
        };

        Method add3 = mapper.resolveFunction("add3");
        assertNotNull(add3);

        assertEquals(1 + 2 + 3, add3.invoke(null, 1, 2, 3));
    }

    @Test(
            expected = ELException.class)
    public void shouldCollideOnDuplicateFunctionName() throws Exception {

        new FunctionMapperSpi.Reflective(FunctionsOfReality.class) {
            @Override
            public String getPrefixName() {
                return "test";
            }
        };

        fail();
    }

    @Test
    public void shouldRetainProperPackageName() throws Exception {
        String packageName = "package io.aklivity.k3po.runtime.lang.el.spi";
        assertEquals("You may have broken the function mapper spi", packageName,
                this.getClass().getPackage().toString());
    }
}
