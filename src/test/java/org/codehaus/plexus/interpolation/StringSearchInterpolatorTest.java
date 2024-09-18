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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.interpolation.os.OperatingSystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class StringSearchInterpolatorTest {

    @BeforeEach
    public void setUp() {
        EnvarBasedValueSource.resetStatics();
    }

    @Test
    public void testLongDelimitersInContext() throws InterpolationException {
        String src = "This is a <expression>test.label</expression> for long delimiters in context.";
        String result = "This is a test for long delimiters in context.";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("<expression>", "</expression>");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    public void testLongDelimitersWithNoStartContext() throws InterpolationException {
        String src = "<expression>test.label</expression> for long delimiters in context.";
        String result = "test for long delimiters in context.";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("<expression>", "</expression>");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    public void testLongDelimitersWithNoEndContext() throws InterpolationException {
        String src = "This is a <expression>test.label</expression>";
        String result = "This is a test";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("<expression>", "</expression>");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    public void testLongDelimitersWithNoContext() throws InterpolationException {
        String src = "<expression>test.label</expression>";
        String result = "test";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("<expression>", "</expression>");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    public void testLongDelimitersPassedToValueSource() throws InterpolationException {
        String src = "<expression>test</expression>";

        StringSearchInterpolator interpolator = new StringSearchInterpolator("<expression>", "</expression>");
        interpolator.addValueSource(new AbstractValueSource(false) {

            @Override
            public Object getValue(String expression, String expressionStartDelimiter, String expressionEndDelimiter) {
                assertEquals("<expression>", expressionStartDelimiter);
                assertEquals("</expression>", expressionEndDelimiter);
                return expression;
            }

            @Override
            public Object getValue(String expression) {
                fail("This method is not supposed to be called");
                return null;
            }
        });

        assertEquals("test", interpolator.interpolate(src));
    }

    @Test
    public void testSimpleSubstitution() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals("This is a test value.", interpolator.interpolate("This is a test ${key}."));
    }

    @Test
    public void testSimpleSubstitution_TwoExpressions() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");
        p.setProperty("key2", "value2");

        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals("value-value2", interpolator.interpolate("${key}-${key2}"));
    }

    @Test
    public void testBrokenExpression_LeaveItAlone() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals("This is a test ${key.", interpolator.interpolate("This is a test ${key."));
    }

    @Test
    public void testShouldFailOnExpressionCycle() {
        Properties props = new Properties();
        props.setProperty("key1", "${key2}");
        props.setProperty("key2", "${key1}");

        StringSearchInterpolator rbi = new StringSearchInterpolator();
        rbi.addValueSource(new PropertiesBasedValueSource(props));

        try {
            rbi.interpolate("${key1}", new SimpleRecursionInterceptor());

            fail("Should detect expression cycle and fail.");
        } catch (InterpolationException e) {
            // expected
        }
    }

    @Test
    public void testShouldResolveByUsingObject_List_Map() throws InterpolationException {
        StringSearchInterpolator rbi = new StringSearchInterpolator();
        rbi.addValueSource(new ObjectBasedValueSource(this));
        String result =
                rbi.interpolate("this is a ${var} ${list[1].name} ${anArray[2].name} ${map(Key with spaces).name}");

        assertEquals("this is a testVar testIndexedWithList testIndexedWithArray testMap", result);
    }

    @Test
    public void testShouldResolveByContextValue() throws InterpolationException {
        StringSearchInterpolator rbi = new StringSearchInterpolator();

        Map context = new HashMap();
        context.put("var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        String result = rbi.interpolate("this is a ${var}");

        assertEquals("this is a testVar", result);
    }

    @Test
    public void testShouldResolveByEnvar() throws IOException, InterpolationException {
        OperatingSystemUtils.setEnvVarSource(new OperatingSystemUtils.EnvVarSource() {
            public Map<String, String> getEnvMap() {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("SOME_ENV", "variable");
                map.put("OTHER_ENV", "other variable");
                return map;
            }
        });

        StringSearchInterpolator rbi = new StringSearchInterpolator();

        rbi.addValueSource(new EnvarBasedValueSource(false));

        String result = rbi.interpolate("this is a ${env.SOME_ENV} ${env.OTHER_ENV}");

        assertEquals("this is a variable other variable", result);
    }

    @Test
    public void testUsePostProcessor_DoesNotChangeValue() throws InterpolationException {
        StringSearchInterpolator rbi = new StringSearchInterpolator();

        Map context = new HashMap();
        context.put("test.var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        rbi.addPostProcessor(new InterpolationPostProcessor() {
            public Object execute(String expression, Object value) {
                return null;
            }
        });

        String result = rbi.interpolate("this is a ${test.var}");

        assertEquals("this is a testVar", result);
    }

    @Test
    public void testUsePostProcessor_ChangesValue() throws InterpolationException {

        StringSearchInterpolator rbi = new StringSearchInterpolator();

        Map context = new HashMap();
        context.put("test.var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        rbi.addPostProcessor(new InterpolationPostProcessor() {
            public Object execute(String expression, Object value) {
                return value + "2";
            }
        });

        String result = rbi.interpolate("this is a ${test.var}");

        assertEquals("this is a testVar2", result);
    }

    @Test
    public void testSimpleSubstitutionWithDefinedExpr() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals("This is a test value.", interpolator.interpolate("This is a test @{key}."));
    }

    @Test
    public void testEscape() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("\\");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("This is a test \\@{key}.");

        assertEquals("This is a test @{key}.", result);
    }

    @Test
    public void testEscapeWithLongEscapeStr() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("$$");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("This is a test $$@{key}.");

        assertEquals("This is a test @{key}.", result);
    }

    @Test
    public void testEscapeWithLongEscapeStrAtStart() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("$$");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("$$@{key} This is a test.");

        assertEquals("@{key} This is a test.", result);
    }

    @Test
    public void testNotEscapeWithLongEscapeStrAtStart() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("$$");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("@{key} This is a test.");

        assertEquals("value This is a test.", result);
    }

    @Test
    public void testEscapeNotFailWithNullEscapeStr() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString(null);
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("This is a test @{key}.");

        assertEquals("This is a test value.", result);
    }

    @Test
    public void testOnlyEscapeExprAtStart() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("\\");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("\\@{key} This is a test.");

        assertEquals("@{key} This is a test.", result);
    }

    @Test
    public void testNotEscapeExprAtStart() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("\\");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("@{key} This is a test.");

        assertEquals("value This is a test.", result);
    }

    @Test
    public void testEscapeExprAtStart() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@", "@");
        interpolator.setEscapeString("\\");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("\\@key@ This is a test @key@.");

        assertEquals("@key@ This is a test value.", result);
    }

    @Test
    public void testNPEFree() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("\\");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate(null);

        assertEquals("", result);
    }

    @Test
    public void testInterruptedInterpolate() throws InterpolationException {
        Interpolator interpolator = new StringSearchInterpolator();
        RecursionInterceptor recursionInterceptor = new SimpleRecursionInterceptor();
        final boolean[] error = new boolean[] {false};
        interpolator.addValueSource(new ValueSource() {
            public Object getValue(String expression) {
                if (expression.equals("key")) {
                    if (error[0]) {
                        throw new IllegalStateException("broken");
                    }
                    return "val";
                } else {
                    return null;
                }
            }

            public List getFeedback() {
                return Collections.EMPTY_LIST;
            }

            public void clearFeedback() {}
        });
        assertEquals("-val-", interpolator.interpolate("-${key}-", recursionInterceptor), "control case");
        error[0] = true;
        try {
            interpolator.interpolate("-${key}-", recursionInterceptor);
            fail("should have thrown exception");
        } catch (IllegalStateException x) {
            // right
        }
        error[0] = false;
        assertEquals(
                "-val-",
                interpolator.interpolate("-${key}-", recursionInterceptor),
                "should not believe there is a cycle here");
    }

    @Test
    public void testCacheAnswersTrue() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        class CountingStringSearchInterpolator extends StringSearchInterpolator {
            private int existingCallCount;

            @Override
            protected Object getExistingAnswer(String key) {
                Object value = super.getExistingAnswer(key);
                if (value != null) {
                    ++existingCallCount;
                }
                return value;
            }

            public int getExistingCallCount() {
                return existingCallCount;
            }
        }

        CountingStringSearchInterpolator interpolator = new CountingStringSearchInterpolator();
        interpolator.setCacheAnswers(true);
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("${key}-${key}-${key}-${key}");

        assertEquals("value-value-value-value", result);
        // first value is interpolated and saved, then the 3 next answers came from existing answer Map
        assertEquals(3, interpolator.getExistingCallCount());

        // answers are preserved between calls:
        result = interpolator.interpolate("${key}-${key}-${key}-${key}");
        assertEquals("value-value-value-value", result);
        // 3 from the first call to interpolate(), plus 4 from second call
        assertEquals(3 + 4, interpolator.getExistingCallCount());
    }

    @Test
    public void testCacheAnswersFalse() throws InterpolationException {
        Properties p = new Properties();
        p.setProperty("key", "value");

        class CountingStringSearchInterpolator extends StringSearchInterpolator {
            private int existingCallCount;

            @Override
            protected Object getExistingAnswer(String key) {
                Object value = super.getExistingAnswer(key);
                if (value != null) {
                    ++existingCallCount;
                }
                return value;
            }

            public int getExistingCallCount() {
                return existingCallCount;
            }
        }

        CountingStringSearchInterpolator interpolator = new CountingStringSearchInterpolator();
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("${key}-${key}-${key}-${key}");

        assertEquals("value-value-value-value", result);
        // all values are interpolated each time
        assertEquals(0, interpolator.getExistingCallCount());
    }

    public String getVar() {
        return "testVar";
    }

    public Person[] getAnArray() {
        Person[] array = new Person[3];
        array[0] = new Person("Gabriel");
        array[1] = new Person("Daniela");
        array[2] = new Person("testIndexedWithArray");
        return array;
    }

    public List<Person> getList() {
        List<Person> list = new ArrayList<Person>();
        list.add(new Person("Gabriel"));
        list.add(new Person("testIndexedWithList"));
        list.add(new Person("Daniela"));
        return list;
    }

    public Map<String, Person> getMap() {
        Map<String, Person> map = new HashMap<String, StringSearchInterpolatorTest.Person>();
        map.put("Key with spaces", new Person("testMap"));
        return map;
    }

    public static class Person {
        private String name;

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
