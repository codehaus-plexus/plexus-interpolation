package org.codehaus.plexus.interpolation.fixed;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.interpolation.FixedInterpolatorValueSource;
import org.codehaus.plexus.interpolation.InterpolationPostProcessor;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.interpolation.os.OperatingSystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.codehaus.plexus.interpolation.fixed.FixedStringSearchInterpolator.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FixedStringSearchInterpolatorTest {

    @BeforeEach
    void setUp() {
        EnvarBasedValueSource.resetStatics();
    }

    @Test
    void longDelimitersInContext() {
        String src = "This is a <expression>test.label</expression> for long delimiters in context.";
        String result = "This is a test for long delimiters in context.";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        FixedStringSearchInterpolator interpolator =
                create(new PropertiesBasedValueSource(p)).withExpressionMarkers("<expression>", "</expression>");

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    void longDelimitersWithNoStartContext() {
        String src = "<expression>test.label</expression> for long delimiters in context.";
        String result = "test for long delimiters in context.";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        FixedStringSearchInterpolator interpolator =
                create(new PropertiesBasedValueSource(p)).withExpressionMarkers("<expression>", "</expression>");

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    void longDelimitersWithNoEndContext() {
        String src = "This is a <expression>test.label</expression>";
        String result = "This is a test";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        FixedStringSearchInterpolator interpolator =
                create(new PropertiesBasedValueSource(p)).withExpressionMarkers("<expression>", "</expression>");

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    void longDelimitersWithNoContext() {
        String src = "<expression>test.label</expression>";
        String result = "test";

        Properties p = new Properties();
        p.setProperty("test.label", "test");

        FixedStringSearchInterpolator interpolator =
                create(new PropertiesBasedValueSource(p)).withExpressionMarkers("<expression>", "</expression>");

        assertEquals(result, interpolator.interpolate(src));
    }

    @Test
    void simpleSubstitution() {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p));

        assertEquals("This is a test value.", interpolator.interpolate("This is a test ${key}."));
    }

    @Test
    void simpleSubstitutionTwoExpressions() {
        Properties p = new Properties();
        p.setProperty("key", "value");
        p.setProperty("key2", "value2");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p));

        assertEquals("value-value2", interpolator.interpolate("${key}-${key2}"));
    }

    @Test
    void brokenExpressionLeaveItAlone() {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p));

        assertEquals("This is a test ${key.", interpolator.interpolate("This is a test ${key."));
    }

    @Test
    void shouldFailOnExpressionCycle() {
        Properties props = new Properties();
        props.setProperty("key1", "${key2}");
        props.setProperty("key2", "${key1}");

        FixedStringSearchInterpolator rbi = create(new PropertiesBasedValueSource(props));

        assertThrows(
                InterpolationCycleException.class,
                () -> rbi.interpolate("${key1}"),
                "Should detect expression cycle and fail.");
    }

    @Test
    void shouldResolveByUsingObjectListMap() throws Exception {
        FixedStringSearchInterpolator rbi = create(new ObjectBasedValueSource(this));
        String result =
                rbi.interpolate("this is a ${var} ${list[1].name} ${anArray[2].name} ${map(Key with spaces).name}");

        assertEquals("this is a testVar testIndexedWithList testIndexedWithArray testMap", result);
    }

    @Test
    void shouldResolveByContextValue() throws Exception {

        Map<String, String> context = new HashMap<>();
        context.put("var", "testVar");

        FixedStringSearchInterpolator rbi = create(new MapBasedValueSource(context));

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

        FixedStringSearchInterpolator rbi = create(new EnvarBasedValueSource(false));

        String result = rbi.interpolate("this is a ${env.SOME_ENV} ${env.OTHER_ENV}");

        assertEquals("this is a variable other variable", result);
    }

    @Test
    void usePostProcessorDoesNotChangeValue() throws Exception {

        Map<String, String> context = new HashMap<>();
        context.put("test.var", "testVar");

        InterpolationPostProcessor postProcessor = (expression, value) -> null;
        FixedStringSearchInterpolator rbi =
                create(new MapBasedValueSource(context)).withPostProcessor(postProcessor);

        String result = rbi.interpolate("this is a ${test.var}");

        assertEquals("this is a testVar", result);
    }

    @Test
    void usePostProcessorChangesValue() throws Exception {

        Map<String, String> context = new HashMap<>();
        context.put("test.var", "testVar");

        InterpolationPostProcessor postProcessor = (expression, value) -> value + "2";

        FixedStringSearchInterpolator rbi =
                create(new MapBasedValueSource(context)).withPostProcessor(postProcessor);

        String result = rbi.interpolate("this is a ${test.var}");

        assertEquals("this is a testVar2", result);
    }

    @Test
    void simpleSubstitutionWithDefinedExpr() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create("@{", "}", new PropertiesBasedValueSource(p));

        assertEquals("This is a test value.", interpolator.interpolate("This is a test @{key}."));
    }

    @Test
    void escape() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p))
                .withExpressionMarkers("@{", "}")
                .withEscapeString("\\");

        String result = interpolator.interpolate("This is a test \\@{key}.");

        assertEquals("This is a test @{key}.", result);
    }

    @Test
    void escapeWithLongEscapeStr() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p))
                .withExpressionMarkers("@{", "}")
                .withEscapeString("$$");

        String result = interpolator.interpolate("This is a test $$@{key}.");

        assertEquals("This is a test @{key}.", result);
    }

    @Test
    void escapeWithLongEscapeStrAtStart() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p))
                .withExpressionMarkers("@{", "}")
                .withEscapeString("$$");

        String result = interpolator.interpolate("$$@{key} This is a test.");

        assertEquals("@{key} This is a test.", result);
    }

    @Test
    void notEscapeWithLongEscapeStrAtStart() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p))
                .withExpressionMarkers("@{", "}")
                .withEscapeString("$$");

        String result = interpolator.interpolate("@{key} This is a test.");

        assertEquals("value This is a test.", result);
    }

    @Test
    void escapeNotFailWithNullEscapeStr() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p))
                .withExpressionMarkers("@{", "}")
                .withEscapeString(null);

        String result = interpolator.interpolate("This is a test @{key}.");

        assertEquals("This is a test value.", result);
    }

    @Test
    void onlyEscapeExprAtStart() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p))
                .withExpressionMarkers("@{", "}")
                .withEscapeString("\\");

        String result = interpolator.interpolate("\\@{key} This is a test.");

        assertEquals("@{key} This is a test.", result);
    }

    @Test
    void notEscapeExprAtStart() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p))
                .withExpressionMarkers("@{", "}")
                .withEscapeString("\\");

        String result = interpolator.interpolate("@{key} This is a test.");

        assertEquals("value This is a test.", result);
    }

    @Test
    void escapeExprAtStart() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p))
                .withExpressionMarkers("@", "@")
                .withEscapeString("\\");

        String result = interpolator.interpolate("\\@key@ This is a test @key@.");

        assertEquals("@key@ This is a test value.", result);
    }

    @Test
    void npeFree() throws Exception {
        Properties p = new Properties();
        p.setProperty("key", "value");

        FixedStringSearchInterpolator interpolator = create(new PropertiesBasedValueSource(p))
                .withExpressionMarkers("@{", "}")
                .withEscapeString("\\");

        String result = interpolator.interpolate(null);

        assertEquals("", result);
    }

    @Test
    void interruptedInterpolate() throws Exception {
        final boolean[] error = new boolean[] {false};
        FixedValueSource valueSource = (expression, errorCollector) -> {
            if (expression.equals("key")) {
                if (error[0]) {
                    throw new IllegalStateException("broken");
                }
                return "val";
            } else {
                return null;
            }
        };

        FixedStringSearchInterpolator interpolator = create(valueSource);

        assertEquals("-val-", interpolator.interpolate("-${key}-"), "control case");
        error[0] = true;

        assertThrows(
                IllegalStateException.class,
                () -> interpolator.interpolate("-${key}-"),
                "should have thrown exception");

        error[0] = false;
        assertEquals("-val-", interpolator.interpolate("-${key}-"), "should not believe there is a cycle here");
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

    @Test
    void linkedInterpolators() {
        final String EXPR = "${test.label}AND${test2}";
        final String EXPR2 = "${test.label}${test2.label}AND${test2}";

        FixedStringSearchInterpolator interWith2Fields =
                create(properttyBasedValueSource("test.label", "p", "test2", "x"));
        assertEquals("pANDx", interWith2Fields.interpolate(EXPR));

        FixedStringSearchInterpolator joined = create(interWith2Fields, properttyBasedValueSource("test2.label", "zz"));
        assertEquals("pzzANDx", joined.interpolate(EXPR2));
    }

    @Test
    void dominance() {
        final String EXPR = "${test.label}AND${test2}";
        final String EXPR2 = "${test.label}${test2.label}AND${test2}";

        FixedStringSearchInterpolator interWith2Fields =
                create(properttyBasedValueSource("test.label", "p", "test2", "x", "test2.label", "dominant"));
        assertEquals("pANDx", interWith2Fields.interpolate(EXPR));

        FixedStringSearchInterpolator joined = create(interWith2Fields, properttyBasedValueSource("test2.label", "zz"));
        assertEquals("pdominantANDx", joined.interpolate(EXPR2));
    }

    @Test
    void unresolable_linked() {
        final String EXPR2 = "${test.label}${test2.label}AND${test2}";

        FixedStringSearchInterpolator interWith2Fields =
                create(properttyBasedValueSource("test.label", "p", "test2", "x", "test2.label", "dominant"));

        FixedStringSearchInterpolator joined = create(interWith2Fields, properttyBasedValueSource("test2.label", "zz"));
        assertEquals("pdominantANDx", joined.interpolate(EXPR2));
    }

    @Test
    void cyclesWithLinked() {
        FixedStringSearchInterpolator first = create(properttyBasedValueSource("key1", "${key2}"));
        FixedStringSearchInterpolator second = create(first, properttyBasedValueSource("key2", "${key1}"));
        assertThrows(InterpolationCycleException.class, () -> second.interpolate("${key2}"));
    }

    @Test
    void cyclesWithLinkedBetweenRootAndOther() {
        FixedStringSearchInterpolator first = create(properttyBasedValueSource("key1", "${key2}"));
        FixedStringSearchInterpolator second = create(first, properttyBasedValueSource("key2", "${key1}"));
        assertThrows(InterpolationCycleException.class, () -> second.interpolate("${key1}"));
    }

    @Test
    void fixedInjectedIntoRegular() throws Exception {
        FixedStringSearchInterpolator first = create(properttyBasedValueSource("key1", "v1"));

        Properties p = new Properties();
        p.setProperty("key", "X");
        StringSearchInterpolator interpolator = new StringSearchInterpolator("${", "}");
        interpolator.setEscapeString("\\");
        interpolator.addValueSource(new org.codehaus.plexus.interpolation.PropertiesBasedValueSource(p));
        interpolator.addValueSource(new FixedInterpolatorValueSource(first));
        assertEquals("v1X", interpolator.interpolate("${key1}${key}"));
    }

    private PropertiesBasedValueSource properttyBasedValueSource(String... values) {
        Properties p = new Properties();
        for (int i = 0; i < values.length; i += 2) {
            p.setProperty(values[i], values[i + 1]);
        }
        return new PropertiesBasedValueSource(p);
    }
}
