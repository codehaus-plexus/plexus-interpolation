package org.codehaus.plexus.interpolation;

/*
 * Copyright 2001-2008 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.interpolation.os.OperatingSystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RegexBasedInterpolatorTest {

    @BeforeEach
    public void setUp() {
        EnvarBasedValueSource.resetStatics();
    }

    public String getVar() {
        return "testVar";
    }

    @Test
    public void testShouldFailOnExpressionCycle() {
        Properties props = new Properties();
        props.setProperty("key1", "${key2}");
        props.setProperty("key2", "${key1}");

        RegexBasedInterpolator rbi = new RegexBasedInterpolator();
        rbi.addValueSource(new PropertiesBasedValueSource(props));

        try {
            rbi.interpolate("${key1}", new SimpleRecursionInterceptor());

            fail("Should detect expression cycle and fail.");
        } catch (InterpolationException e) {
            // expected
        }
    }

    @Test
    public void testShouldResolveByMy_getVar_Method() throws InterpolationException {
        RegexBasedInterpolator rbi = new RegexBasedInterpolator();
        rbi.addValueSource(new ObjectBasedValueSource(this));
        String result = rbi.interpolate("this is a ${this.var}", "this");

        assertEquals("this is a testVar", result);
    }

    @Test
    public void testShouldResolveByContextValue() throws InterpolationException {
        RegexBasedInterpolator rbi = new RegexBasedInterpolator();

        Map context = new HashMap();
        context.put("var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        String result = rbi.interpolate("this is a ${this.var}", "this");

        assertEquals("this is a testVar", result);
    }

    @Test
    public void testShouldResolveByEnvar() throws IOException, InterpolationException {
        OperatingSystemUtils.setEnvVarSource(new OperatingSystemUtils.EnvVarSource() {
            public Map<String, String> getEnvMap() {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("SOME_ENV", "variable");
                return map;
            }
        });

        RegexBasedInterpolator rbi = new RegexBasedInterpolator();

        rbi.addValueSource(new EnvarBasedValueSource());

        String result = rbi.interpolate("this is a ${env.SOME_ENV}", "this");

        assertEquals("this is a variable", result);
    }

    @Test
    public void testUseAlternateRegex() throws Exception {
        RegexBasedInterpolator rbi = new RegexBasedInterpolator("\\@\\{(", ")?([^}]+)\\}@");

        Map context = new HashMap();
        context.put("var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        String result = rbi.interpolate("this is a @{this.var}@", "this");

        assertEquals("this is a testVar", result);
    }

    @Test
    public void testNPEFree() throws Exception {
        RegexBasedInterpolator rbi = new RegexBasedInterpolator("\\@\\{(", ")?([^}]+)\\}@");

        Map context = new HashMap();
        context.put("var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        String result = rbi.interpolate(null);

        assertEquals("", result);
    }

    @Test
    public void testUsePostProcessor_DoesNotChangeValue() throws InterpolationException {
        RegexBasedInterpolator rbi = new RegexBasedInterpolator();

        Map context = new HashMap();
        context.put("test.var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        rbi.addPostProcessor(new InterpolationPostProcessor() {
            public Object execute(String expression, Object value) {
                return null;
            }
        });

        String result = rbi.interpolate("this is a ${test.var}", "");

        assertEquals("this is a testVar", result);
    }

    @Test
    public void testUsePostProcessor_ChangesValue() throws InterpolationException {

        int loopNumber = 200000;

        long start = System.currentTimeMillis();

        RegexBasedInterpolator rbi = new RegexBasedInterpolator();

        Map context = new HashMap();
        context.put("test.var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        rbi.addPostProcessor(new InterpolationPostProcessor() {
            public Object execute(String expression, Object value) {
                return value + "2";
            }
        });

        for (int i = 0, number = loopNumber; i < number; i++) {

            String result = rbi.interpolate("this is a ${test.var}", "");

            assertEquals("this is a testVar2", result);
        }
        long end = System.currentTimeMillis();

        System.out.println("time without pattern reuse and RegexBasedInterpolator instance reuse " + (end - start));

        System.gc();

        start = System.currentTimeMillis();

        rbi = new RegexBasedInterpolator(true);

        rbi.addPostProcessor(new InterpolationPostProcessor() {
            public Object execute(String expression, Object value) {
                return value + "2";
            }
        });

        rbi.addValueSource(new MapBasedValueSource(context));

        for (int i = 0, number = loopNumber; i < number; i++) {

            String result = rbi.interpolate("this is a ${test.var}", "");

            assertEquals("this is a testVar2", result);
        }
        end = System.currentTimeMillis();

        System.out.println("time with pattern reuse and RegexBasedInterpolator instance reuse " + (end - start));
    }
}
