package org.codehaus.plexus.interpolation;

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
    void setUp() {
        EnvarBasedValueSource.resetStatics();
    }

    @Test
    void longDelimitersInContext() throws Exception {
        String src = "This is a <expression>test.label</expression> for long delimiters in context.";
        String result = "This is a test for long delimiters in context.";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("<expression>", "</expression>");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    void longDelimitersWithNoStartContext() throws Exception {
        String src = "<expression>test.label</expression> for long delimiters in context.";
        String result = "test for long delimiters in context.";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("<expression>", "</expression>");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    void longDelimitersWithNoEndContext() throws Exception {
        String src = "This is a <expression>test.label</expression>";
        String result = "This is a test";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("<expression>", "</expression>");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    void longDelimitersWithNoContext() throws Exception {
        String src = "<expression>test.label</expression>";
        String result = "test";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("<expression>", "</expression>");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    void longDelimitersPassedToValueSource() throws Exception {
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
    void simpleSubstitution() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals("This is a test value.", interpolator.interpolate("This is a test ${key}."));
    }

    @Test
    void simpleSubstitutionTwoExpressions() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");
        p.setProperty("key2", "value2");

        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals("value-value2", interpolator.interpolate("${key}-${key2}"));
    }

    @Test
    void brokenExpressionLeaveItAlone() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals("This is a test ${key.", interpolator.interpolate("This is a test ${key."));
    }

    @Test
    void shouldFailOnExpressionCycle() {
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
    void shouldResolveByUsingObjectListMap() throws Exception {
        StringSearchInterpolator rbi = new StringSearchInterpolator();
        rbi.addValueSource(new ObjectBasedValueSource(this));
        String result =
                rbi.interpolate("this is a ${var} ${list[1].name} ${anArray[2].name} ${map(Key with spaces).name}");

        assertEquals("this is a testVar testIndexedWithList testIndexedWithArray testMap", result);
    }

    @Test
    void shouldResolveByContextValue() throws Exception {
        StringSearchInterpolator rbi = new StringSearchInterpolator();

        Map<String, String> context = new HashMap<>();
        context.put("var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        String result = rbi.interpolate("this is a ${var}");

        assertEquals("this is a testVar", result);
    }

    @Test
    void shouldResolveByEnvar() throws Exception {
        OperatingSystemUtils.setEnvVarSource(() -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("SOME_ENV", "variable");
            map.put("OTHER_ENV", "other variable");
            return map;
        });

        StringSearchInterpolator rbi = new StringSearchInterpolator();

        rbi.addValueSource(new EnvarBasedValueSource(false));

        String result = rbi.interpolate("this is a ${env.SOME_ENV} ${env.OTHER_ENV}");

        assertEquals("this is a variable other variable", result);
    }

    @Test
    void usePostProcessorDoesNotChangeValue() throws Exception {
        StringSearchInterpolator rbi = new StringSearchInterpolator();

        Map<String, String> context = new HashMap<>();
        context.put("test.var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        rbi.addPostProcessor((expression, value) -> null);

        String result = rbi.interpolate("this is a ${test.var}");

        assertEquals("this is a testVar", result);
    }

    @Test
    void usePostProcessorChangesValue() throws Exception {

        StringSearchInterpolator rbi = new StringSearchInterpolator();

        Map<String, String> context = new HashMap<>();
        context.put("test.var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        rbi.addPostProcessor((expression, value) -> value + "2");

        String result = rbi.interpolate("this is a ${test.var}");

        assertEquals("this is a testVar2", result);
    }

    @Test
    void simpleSubstitutionWithDefinedExpr() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        assertEquals("This is a test value.", interpolator.interpolate("This is a test @{key}."));
    }

    @Test
    void escape() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("\\");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("This is a test \\@{key}.");

        assertEquals("This is a test @{key}.", result);
    }

    @Test
    void escapeWithLongEscapeStr() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("$$");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("This is a test $$@{key}.");

        assertEquals("This is a test @{key}.", result);
    }

    @Test
    void escapeWithLongEscapeStrAtStart() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("$$");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("$$@{key} This is a test.");

        assertEquals("@{key} This is a test.", result);
    }

    @Test
    void notEscapeWithLongEscapeStrAtStart() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("$$");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("@{key} This is a test.");

        assertEquals("value This is a test.", result);
    }

    @Test
    void escapeNotFailWithNullEscapeStr() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString(null);
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("This is a test @{key}.");

        assertEquals("This is a test value.", result);
    }

    @Test
    void onlyEscapeExprAtStart() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("\\");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("\\@{key} This is a test.");

        assertEquals("@{key} This is a test.", result);
    }

    @Test
    void notEscapeExprAtStart() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("\\");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("@{key} This is a test.");

        assertEquals("value This is a test.", result);
    }

    @Test
    void escapeExprAtStart() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@", "@");
        interpolator.setEscapeString("\\");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate("\\@key@ This is a test @key@.");

        assertEquals("@key@ This is a test value.", result);
    }

    @Test
    void npeFree() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator("@{", "}");
        interpolator.setEscapeString("\\");
        interpolator.addValueSource(new PropertiesBasedValueSource(p));

        String result = interpolator.interpolate(null);

        assertEquals("", result);
    }

    @Test
    void interruptedInterpolate() throws Exception {
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
    void cacheAnswersTrue() throws Exception {
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
    void cacheAnswersFalse() throws Exception {
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
        List<Person> list = new ArrayList<>();
        list.add(new Person("Gabriel"));
        list.add(new Person("testIndexedWithList"));
        list.add(new Person("Daniela"));
        return list;
    }

    public Map<String, Person> getMap() {
        Map<String, Person> map = new HashMap<>();
        map.put("Key with spaces", new Person("testMap"));
        return map;
    }

    public static class Person {
        private final String name;

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
