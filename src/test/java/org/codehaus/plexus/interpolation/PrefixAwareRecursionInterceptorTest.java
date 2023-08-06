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

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrefixAwareRecursionInterceptorTest {

    @Test
    public void testFindExpression() {
        PrefixAwareRecursionInterceptor receptor =
                new PrefixAwareRecursionInterceptor(Collections.singleton("prefix."));

        String expr = "prefix.first";

        receptor.expressionResolutionStarted(expr);

        assertTrue(receptor.hasRecursiveExpression(expr));
        assertEquals("[first]", receptor.getExpressionCycle(expr).toString());

        receptor.expressionResolutionFinished(expr);

        assertFalse(receptor.hasRecursiveExpression(expr));
    }

    @Test
    public void testFindExpressionWithDifferentPrefix() {
        PrefixAwareRecursionInterceptor receptor =
                new PrefixAwareRecursionInterceptor(Arrays.asList(new String[] {"prefix.", "other."}));

        String expr = "prefix.first";

        receptor.expressionResolutionStarted(expr);

        assertTrue(receptor.hasRecursiveExpression(expr));

        receptor.expressionResolutionFinished(expr);

        assertFalse(receptor.hasRecursiveExpression(expr));
    }

    @Test
    public void testFindExpressionWithoutPrefix() {
        PrefixAwareRecursionInterceptor receptor =
                new PrefixAwareRecursionInterceptor(Arrays.asList(new String[] {"prefix.", "other."}));

        String prefixedExpr = "prefix.first";
        String expr = "first";

        receptor.expressionResolutionStarted(prefixedExpr);

        assertTrue(receptor.hasRecursiveExpression(expr));

        receptor.expressionResolutionFinished(prefixedExpr);

        assertFalse(receptor.hasRecursiveExpression(expr));
    }
}
