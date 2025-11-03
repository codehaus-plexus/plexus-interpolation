package org.codehaus.plexus.interpolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterpolateWithListsTest {

    @Test
    public void testStringSearchInterpolatorWithLists() throws InterpolationException {
        String src = "This is a ${test.label}.";
        String result = "This is a test value.";

        Properties p = new Properties();
        p.setProperty("test.label", "test value");

        List<ValueSource> valueSources = new ArrayList<>();
        valueSources.add(new PropertiesBasedValueSource(p));

        List<InterpolationPostProcessor> postProcessors = new ArrayList<>();

        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        assertEquals(result, interpolator.interpolate(src, valueSources, postProcessors));
    }

    @Test
    public void testRegexBasedInterpolatorWithLists() throws InterpolationException {
        String src = "This is a ${test.label}.";
        String result = "This is a test value.";

        Properties p = new Properties();
        p.setProperty("test.label", "test value");

        List<ValueSource> valueSources = new ArrayList<>();
        valueSources.add(new PropertiesBasedValueSource(p));

        List<InterpolationPostProcessor> postProcessors = new ArrayList<>();

        RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
        assertEquals(result, interpolator.interpolate(src, valueSources, postProcessors));
    }

    @Test
    public void testMultiDelimiterInterpolatorWithLists() throws InterpolationException {
        String src = "This is a ${test.label}.";
        String result = "This is a test value.";

        Properties p = new Properties();
        p.setProperty("test.label", "test value");

        List<ValueSource> valueSources = new ArrayList<>();
        valueSources.add(new PropertiesBasedValueSource(p));

        List<InterpolationPostProcessor> postProcessors = new ArrayList<>();

        org.codehaus.plexus.interpolation.multi.MultiDelimiterStringSearchInterpolator interpolator =
                new org.codehaus.plexus.interpolation.multi.MultiDelimiterStringSearchInterpolator();
        assertEquals(result, interpolator.interpolate(src, valueSources, postProcessors));
    }

    @Test
    public void testInterpolatorWithPostProcessor() throws InterpolationException {
        String src = "This is a ${test.label}.";
        String result = "This is a PROCESSED.";

        Properties p = new Properties();
        p.setProperty("test.label", "test value");

        List<ValueSource> valueSources = new ArrayList<>();
        valueSources.add(new PropertiesBasedValueSource(p));

        List<InterpolationPostProcessor> postProcessors = new ArrayList<>();
        postProcessors.add(new InterpolationPostProcessor() {
            @Override
            public Object execute(String expression, Object value) {
                if ("test.label".equals(expression)) {
                    return "PROCESSED";
                }
                return null;
            }
        });

        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        assertEquals(result, interpolator.interpolate(src, valueSources, postProcessors));
    }

    @Test
    public void testInstanceValueSourcesNotAffected() throws InterpolationException {
        String src = "This is a ${test.label}.";

        Properties p1 = new Properties();
        p1.setProperty("test.label", "instance value");

        Properties p2 = new Properties();
        p2.setProperty("test.label", "list value");

        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource(new PropertiesBasedValueSource(p1));

        List<ValueSource> valueSources = new ArrayList<>();
        valueSources.add(new PropertiesBasedValueSource(p2));

        // Should use the list value sources, not instance value sources
        assertEquals("This is a list value.", interpolator.interpolate(src, valueSources, null));

        // Should still use instance value sources with regular interpolate
        assertEquals("This is a instance value.", interpolator.interpolate(src));
    }
}
