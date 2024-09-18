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
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MultiDelimiterStringSearchInterpolatorTest {

    @Test
    public void testInterpolationWithDifferentDelimiters() throws InterpolationException {
        Map ctx = new HashMap();
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
    public void testSuccessiveInterpolationWithDifferentDelimiters_ReversedDelimiterSequence()
            throws InterpolationException {
        Map ctx = new HashMap();
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
    public void testInterpolationWithMultipleEscapes() throws InterpolationException {
        Map ctx = new HashMap();
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
    public void testInterpolationWithMultipleEscapes2() throws InterpolationException {
        Map ctx = new HashMap();
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
    public void testInterpolationWithMultipleEscapes3() throws InterpolationException {
        Map ctx = new HashMap();
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
    public void testDelimitersPassedToValueSource() throws InterpolationException {
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
}
