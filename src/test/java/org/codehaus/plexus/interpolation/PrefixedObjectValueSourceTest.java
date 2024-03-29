package org.codehaus.plexus.interpolation;

/*
 * Copyright 2007 The Codehaus Foundation.
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
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class PrefixedObjectValueSourceTest {

    @Test
    public void testEmptyExpressionResultsInNullReturn_NoPrefixUsed() {
        String target = "Target object";

        List prefixes = new ArrayList();
        prefixes.add("target");
        prefixes.add("object");

        PrefixedObjectValueSource vs = new PrefixedObjectValueSource(prefixes, target, true);
        Object result = vs.getValue("");

        assertNull(result);
    }

    @Test
    public void testEmptyExpressionResultsInNullReturn_PrefixUsedWithDot() {
        String target = "Target object";

        List prefixes = new ArrayList();
        prefixes.add("target");
        prefixes.add("object");

        PrefixedObjectValueSource vs = new PrefixedObjectValueSource(prefixes, target, true);
        Object result = vs.getValue("target.");

        assertNull(result);
    }

    @Test
    public void testEmptyExpressionResultsInNullReturn_PrefixUsedWithoutDot() {
        String target = "Target object";

        List prefixes = new ArrayList();
        prefixes.add("target");
        prefixes.add("object");

        PrefixedObjectValueSource vs = new PrefixedObjectValueSource(prefixes, target, true);
        Object result = vs.getValue("target");

        assertNull(result);
    }
}
