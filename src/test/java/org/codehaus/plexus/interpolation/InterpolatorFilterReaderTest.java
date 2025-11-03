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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * InterpolatorFilterReaderTest, heavily based on InterpolationFilterReaderTest. Heh, even the test strings remained the
 * same!
 *
 * @author cstamas
 *
 */
class InterpolatorFilterReaderTest {
    /*
     * Added and commented by jdcasey@03-Feb-2005 because it is a bug in the InterpolationFilterReader.
     * kenneyw@15-04-2005 fixed the bug.
     */
    @Test
    void shouldNotInterpolateExpressionAtEndOfDataWithInvalidEndToken() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("test", "TestValue");

        String testStr = "This is a ${test";

        assertEquals("This is a ${test", interpolate(testStr, m));
    }

    /*
     * kenneyw@14-04-2005 Added test to check above fix.
     */
    @Test
    void shouldNotInterpolateExpressionWithMissingEndToken() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("test", "TestValue");

        String testStr = "This is a ${test, really";

        assertEquals("This is a ${test, really", interpolate(testStr, m));
    }

    @Test
    void shouldNotInterpolateWithMalformedStartToken() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("test", "testValue");

        String foo = "This is a $!test} again";

        assertEquals("This is a $!test} again", interpolate(foo, m));
    }

    @Test
    void shouldNotInterpolateWithMalformedEndToken() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("test", "testValue");

        String foo = "This is a ${test!} again";

        assertEquals("This is a ${test!} again", interpolate(foo, m));
    }

    @Test
    void defaultInterpolationWithNonInterpolatedValueAtEnd() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("name", "jason");
        m.put("noun", "asshole");

        String foo = "${name} is an ${noun}. ${not.interpolated}";

        assertEquals("jason is an asshole. ${not.interpolated}", interpolate(foo, m));
    }

    @Test
    void defaultInterpolationWithInterpolatedValueAtEnd() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("name", "jason");
        m.put("noun", "asshole");

        String foo = "${name} is an ${noun}";

        assertEquals("jason is an asshole", interpolate(foo, m));
    }

    @Test
    void interpolationWithInterpolatedValueAtEndWithCustomToken() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("name", "jason");
        m.put("noun", "asshole");

        String foo = "@{name} is an @{noun}";

        assertEquals("jason is an asshole", interpolate(foo, m, "@{", "}"));
    }

    @Test
    void interpolationWithInterpolatedValueAtEndWithCustomTokenAndCustomString() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("name", "jason");
        m.put("noun", "asshole");

        String foo = "@name@ is an @noun@";

        assertEquals("jason is an asshole", interpolate(foo, m, "@", "@"));
    }

    @Test
    void escape() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("name", "jason");
        m.put("noun", "asshole");

        String foo = "${name} is an \\${noun}";

        assertEquals("jason is an ${noun}", interpolate(foo, m, "\\"));
    }

    @Test
    void escapeAtStart() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("name", "jason");
        m.put("noun", "asshole");

        String foo = "\\${name} is an \\${noun}";

        assertEquals("${name} is an ${noun}", interpolate(foo, m, "\\"));
    }

    @Test
    void escapeOnlyAtStart() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("name", "jason");
        m.put("noun", "asshole");

        String foo = "\\@name@ is an @noun@";

        String result = interpolate(foo, m, "@", "@");
        assertEquals("@name@ is an asshole", result);
    }

    @Test
    void escapeOnlyAtStartDefaultToken() throws Exception {
        Map<String, String> m = new HashMap<>();
        m.put("name", "jason");
        m.put("noun", "asshole");

        String foo = "\\${name} is an ${noun}";

        String result = interpolate(foo, m, "${", "}");
        assertEquals("${name} is an asshole", result);
    }

    @Test
    void shouldDetectRecursiveExpressionPassingThroughTwoPrefixes() throws Exception {
        List<String> prefixes = new ArrayList<>();

        prefixes.add("prefix1");
        prefixes.add("prefix2");

        RecursionInterceptor ri = new PrefixAwareRecursionInterceptor(prefixes, false);

        Map<String, String> context = new HashMap<>();
        context.put("name", "${prefix2.name}");

        String input = "${prefix1.name}";

        StringSearchInterpolator interpolator = new StringSearchInterpolator();

        interpolator.addValueSource(new MapBasedValueSource(context));

        InterpolatorFilterReader r = new InterpolatorFilterReader(new StringReader(input), interpolator, ri);
        r.setInterpolateWithPrefixPattern(false);
        r.setEscapeString("\\");
        StringBuilder buf = new StringBuilder();
        int read = -1;
        char[] cbuf = new char[1024];
        while ((read = r.read(cbuf)) > -1) {
            buf.append(cbuf, 0, read);
        }

        assertEquals(input, buf.toString());
    }

    @Test
    void shouldDetectRecursiveExpressionWithPrefixAndWithout() throws Exception {
        List<String> prefixes = new ArrayList<>();

        prefixes.add("prefix1");

        RecursionInterceptor ri = new PrefixAwareRecursionInterceptor(prefixes, false);

        Map<String, String> context = new HashMap<>();
        context.put("name", "${prefix1.name}");

        String input = "${name}";

        StringSearchInterpolator interpolator = new StringSearchInterpolator();

        interpolator.addValueSource(new MapBasedValueSource(context));

        InterpolatorFilterReader r = new InterpolatorFilterReader(new StringReader(input), interpolator, ri);
        r.setInterpolateWithPrefixPattern(false);
        r.setEscapeString("\\");
        StringBuilder buf = new StringBuilder();
        int read = -1;
        char[] cbuf = new char[1024];
        while ((read = r.read(cbuf)) > -1) {
            buf.append(cbuf, 0, read);
        }

        assertEquals("${prefix1.name}", buf.toString());
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String interpolate(String input, Map<String, String> context) throws Exception {
        return interpolate(input, context, null);
    }

    private String interpolate(String input, Map<String, String> context, String escapeStr) throws Exception {
        Interpolator interpolator = new StringSearchInterpolator();

        interpolator.addValueSource(new MapBasedValueSource(context));

        InterpolatorFilterReader r = new InterpolatorFilterReader(new StringReader(input), interpolator);
        r.setInterpolateWithPrefixPattern(false);
        if (escapeStr != null) {
            r.setEscapeString(escapeStr);
        }
        StringBuilder buf = new StringBuilder();
        int read = -1;
        char[] cbuf = new char[1024];
        while ((read = r.read(cbuf)) > -1) {
            buf.append(cbuf, 0, read);
        }

        return buf.toString();
    }

    private String interpolate(String input, Map<String, String> context, String beginToken, String endToken)
            throws Exception {
        StringSearchInterpolator interpolator = new StringSearchInterpolator(beginToken, endToken);

        interpolator.addValueSource(new MapBasedValueSource(context));

        InterpolatorFilterReader r =
                new InterpolatorFilterReader(new StringReader(input), interpolator, beginToken, endToken);
        r.setInterpolateWithPrefixPattern(false);
        r.setEscapeString("\\");
        StringBuilder buf = new StringBuilder();
        int read = -1;
        char[] cbuf = new char[1024];
        while ((read = r.read(cbuf)) > -1) {
            buf.append(cbuf, 0, read);
        }

        return buf.toString();
    }
}
