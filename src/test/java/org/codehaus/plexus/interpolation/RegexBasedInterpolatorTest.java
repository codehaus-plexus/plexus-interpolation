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
    public void testDelimitersPassedToValueSource() throws InterpolationException {
        RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
        interpolator.addValueSource(new AbstractValueSource(false) {

            @Override
            public Object getValue(String expression, String expressionStartDelimiter, String expressionEndDelimiter) {
                assertEquals("${", expressionStartDelimiter);
                assertEquals("}", expressionEndDelimiter);
                return expression;
            }

            @Override
            public Object getValue(String expression) {
                fail("This method is not supposed to be called");
                return null;
            }
        });

        assertEquals("test", interpolator.interpolate("${test}"));
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

    @Test
    public void testCacheAnswersTrue() throws InterpolationException {
        Map ctx = new HashMap();
        ctx.put("key", "value");

        final int[] valueSourceCallCount = {0};

        ValueSource vs = new AbstractValueSource(false) {
            @Override
            public Object getValue(String expression) {
                valueSourceCallCount[0]++;
                return ctx.get(expression);
            }
        };

        RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
        interpolator.setCacheAnswers(true);
        interpolator.addValueSource(vs);

        // First interpolation
        String result = interpolator.interpolate("${key}-${key}-${key}-${key}");
        assertEquals("value-value-value-value", result);
        assertEquals(1, valueSourceCallCount[0]);

        // Second interpolation - cache should be used, no new ValueSource calls
        result = interpolator.interpolate("${key}-${key}-${key}-${key}");
        assertEquals("value-value-value-value", result);
        assertEquals(1, valueSourceCallCount[0]); // still 1, cache was used

        // Third interpolation with different expression that also uses cached value
        result = interpolator.interpolate("The value is ${key}");
        assertEquals("The value is value", result);
        assertEquals(1, valueSourceCallCount[0]); // still 1, cache was used
    }

    @Test
    public void testCacheAnswersFalse() throws InterpolationException {
        Map ctx = new HashMap();
        ctx.put("key", "value");

        final int[] valueSourceCallCount = {0};

        ValueSource vs = new AbstractValueSource(false) {
            @Override
            public Object getValue(String expression) {
                valueSourceCallCount[0]++;
                return ctx.get(expression);
            }
        };

        RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
        interpolator.addValueSource(vs);

        // First interpolation
        String result = interpolator.interpolate("${key}-${key}-${key}-${key}");
        assertEquals("value-value-value-value", result);
        assertEquals(1, valueSourceCallCount[0]);

        // Second interpolation - without caching, ValueSource is called again
        result = interpolator.interpolate("${key}-${key}-${key}-${key}");
        assertEquals("value-value-value-value", result);
        assertEquals(2, valueSourceCallCount[0]); // incremented to 2

        // Third interpolation
        result = interpolator.interpolate("The value is ${key}");
        assertEquals("The value is value", result);
        assertEquals(3, valueSourceCallCount[0]); // incremented to 3
    }
}
