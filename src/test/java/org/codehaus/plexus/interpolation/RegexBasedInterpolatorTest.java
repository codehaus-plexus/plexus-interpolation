package org.codehaus.plexus.interpolation;

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
    void setUp() {
        EnvarBasedValueSource.resetStatics();
    }

    public String getVar() {
        return "testVar";
    }

    @Test
    void shouldFailOnExpressionCycle() {
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
    void shouldResolveByMyGetVarMethod() throws Exception {
        RegexBasedInterpolator rbi = new RegexBasedInterpolator();
        rbi.addValueSource(new ObjectBasedValueSource(this));
        String result = rbi.interpolate("this is a ${this.var}", "this");

        assertEquals("this is a testVar", result);
    }

    @Test
    void shouldResolveByContextValue() throws Exception {
        RegexBasedInterpolator rbi = new RegexBasedInterpolator();

        Map<String, String> context = new HashMap();
        context.put("var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        String result = rbi.interpolate("this is a ${this.var}", "this");

        assertEquals("this is a testVar", result);
    }

    @Test
    void delimitersPassedToValueSource() throws Exception {
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
    void shouldResolveByEnvar() throws Exception {
        OperatingSystemUtils.setEnvVarSource(() -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("SOME_ENV", "variable");
            return map;
        });

        RegexBasedInterpolator rbi = new RegexBasedInterpolator();

        rbi.addValueSource(new EnvarBasedValueSource());

        String result = rbi.interpolate("this is a ${env.SOME_ENV}", "this");

        assertEquals("this is a variable", result);
    }

    @Test
    void useAlternateRegex() throws Exception {
        RegexBasedInterpolator rbi = new RegexBasedInterpolator("\\@\\{(", ")?([^}]+)\\}@");

        Map<String, String> context = new HashMap<>();
        context.put("var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        String result = rbi.interpolate("this is a @{this.var}@", "this");

        assertEquals("this is a testVar", result);
    }

    @Test
    void npeFree() throws Exception {
        RegexBasedInterpolator rbi = new RegexBasedInterpolator("\\@\\{(", ")?([^}]+)\\}@");

        Map<String, String> context = new HashMap<>();
        context.put("var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        String result = rbi.interpolate(null);

        assertEquals("", result);
    }

    @Test
    void usePostProcessorDoesNotChangeValue() throws Exception {
        RegexBasedInterpolator rbi = new RegexBasedInterpolator();

        Map<String, String> context = new HashMap<>();
        context.put("test.var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        rbi.addPostProcessor((expression, value) -> null);

        String result = rbi.interpolate("this is a ${test.var}", "");

        assertEquals("this is a testVar", result);
    }

    @Test
    void usePostProcessorChangesValue() throws Exception {

        int loopNumber = 200000;

        long start = System.currentTimeMillis();

        RegexBasedInterpolator rbi = new RegexBasedInterpolator();

        Map<String, String> context = new HashMap<>();
        context.put("test.var", "testVar");

        rbi.addValueSource(new MapBasedValueSource(context));

        rbi.addPostProcessor((expression, value) -> value + "2");

        for (int i = 0, number = loopNumber; i < number; i++) {

            String result = rbi.interpolate("this is a ${test.var}", "");

            assertEquals("this is a testVar2", result);
        }
        long end = System.currentTimeMillis();

        System.out.println("time without pattern reuse and RegexBasedInterpolator instance reuse " + (end - start));

        System.gc();

        start = System.currentTimeMillis();

        rbi = new RegexBasedInterpolator(true);

        rbi.addPostProcessor((expression, value) -> value + "2");

        rbi.addValueSource(new MapBasedValueSource(context));

        for (int i = 0, number = loopNumber; i < number; i++) {

            String result = rbi.interpolate("this is a ${test.var}", "");

            assertEquals("this is a testVar2", result);
        }
        end = System.currentTimeMillis();

        System.out.println("time with pattern reuse and RegexBasedInterpolator instance reuse " + (end - start));
    }

    @Test
    void cacheAnswersTrue() throws Exception {
        Map<String, String> ctx = new HashMap<>();
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
    void cacheAnswersFalse() throws Exception {
        Map<String, String> ctx = new HashMap<>();
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
