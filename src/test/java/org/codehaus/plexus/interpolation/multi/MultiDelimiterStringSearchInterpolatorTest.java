package org.codehaus.plexus.interpolation.multi;

/*
 * Copyright 2001-2009 Codehaus Foundation.
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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.interpolation.AbstractValueSource;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MultiDelimiterStringSearchInterpolatorTest {

    @Test
    void interpolationWithDifferentDelimiters() throws Exception {
        Map<String, String> ctx = new HashMap<>();
        ctx.put("name", "User");
        ctx.put("otherName", "@name@");

        String input = "${otherName}";

        ValueSource vs = new MapBasedValueSource(ctx);
        MultiDelimiterStringSearchInterpolator interpolator = new MultiDelimiterStringSearchInterpolator()
                .addDelimiterSpec("@")
                .withValueSource(vs);

        String result = interpolator.interpolate(input);

        assertEquals(ctx.get("name"), result);
    }

    @Test
    void successiveInterpolationWithDifferentDelimitersReversedDelimiterSequence() throws Exception {
        Map<String, String> ctx = new HashMap<>();
        ctx.put("name", "User");
        ctx.put("otherName", "${name}");

        String input = "@otherName@";

        ValueSource vs = new MapBasedValueSource(ctx);
        MultiDelimiterStringSearchInterpolator interpolator = new MultiDelimiterStringSearchInterpolator()
                .addDelimiterSpec("@")
                .withValueSource(vs);

        String result = interpolator.interpolate(input);

        assertEquals(ctx.get("name"), result);
    }

    @Test
    void interpolationWithMultipleEscapes() throws Exception {
        Map<String, String> ctx = new HashMap<>();
        ctx.put("name", "User");
        ctx.put("otherName", "##${first} and #${last}");

        String input = "${otherName}";

        ValueSource vs = new MapBasedValueSource(ctx);
        MultiDelimiterStringSearchInterpolator interpolator =
                new MultiDelimiterStringSearchInterpolator().withValueSource(vs);
        interpolator.setEscapeString("#");

        String result = interpolator.interpolate(input);

        assertEquals("#${first} and ${last}", result);
    }

    @Test
    void interpolationWithMultipleEscapes2() throws Exception {
        Map<String, String> ctx = new HashMap<>();
        ctx.put("name", "User");
        ctx.put("otherName", "#${first} and ##${last}");

        String input = "${otherName}";

        ValueSource vs = new MapBasedValueSource(ctx);
        MultiDelimiterStringSearchInterpolator interpolator =
                new MultiDelimiterStringSearchInterpolator().withValueSource(vs);
        interpolator.setEscapeString("#");

        String result = interpolator.interpolate(input);

        assertEquals("${first} and #${last}", result);
    }

    @Test
    void interpolationWithMultipleEscapes3() throws Exception {
        Map<String, String> ctx = new HashMap<>();
        ctx.put("name", "User");
        ctx.put("last", "beer");
        ctx.put("otherName", "###${first} and ##${second} and ${last}");

        String input = "${otherName}";

        ValueSource vs = new MapBasedValueSource(ctx);
        MultiDelimiterStringSearchInterpolator interpolator = new MultiDelimiterStringSearchInterpolator() //
                .withValueSource(vs) //
                .escapeString("#");

        String result = interpolator.interpolate(input);

        assertEquals("##${first} and #${second} and beer", result);
    }

    @Test
    void delimitersPassedToValueSource() throws Exception {
        ValueSource vs = new AbstractValueSource(false) {

            @Override
            public Object getValue(String expression, String expressionStartDelimiter, String expressionEndDelimiter) {
                assertEquals("#(", expressionStartDelimiter);
                assertEquals(")", expressionEndDelimiter);
                return expression;
            }

            @Override
            public Object getValue(String expression) {
                fail("This method is not supposed to be called");
                return null;
            }
        };
        MultiDelimiterStringSearchInterpolator interpolator = new MultiDelimiterStringSearchInterpolator() //
                .withValueSource(vs) //
                .escapeString("#");
        interpolator.addDelimiterSpec("#(*)");

        assertEquals("test", interpolator.interpolate("#(test)"));
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

        MultiDelimiterStringSearchInterpolator interpolator = new MultiDelimiterStringSearchInterpolator();
        interpolator.setCacheAnswers(true);
        interpolator.addValueSource(vs);

        String result = interpolator.interpolate("${key}-${key}-${key}-${key}");

        assertEquals("value-value-value-value", result);
        // first value is interpolated and saved, then the 3 next answers came from existing answer Map
        assertEquals(1, valueSourceCallCount[0]);

        // answers are preserved between calls:
        result = interpolator.interpolate("${key}-${key}-${key}-${key}");
        assertEquals("value-value-value-value", result);
        // still 1 from first call as cache is preserved
        assertEquals(1, valueSourceCallCount[0]);
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

        MultiDelimiterStringSearchInterpolator interpolator = new MultiDelimiterStringSearchInterpolator();
        interpolator.addValueSource(vs);

        String result = interpolator.interpolate("${key}-${key}-${key}-${key}");

        assertEquals("value-value-value-value", result);
        // Without caching, expressions are evaluated multiple times due to multi-pass resolution
        // In this case: 4 expressions evaluated in 2 passes = 8 calls
        assertEquals(8, valueSourceCallCount[0]);
    }
}
