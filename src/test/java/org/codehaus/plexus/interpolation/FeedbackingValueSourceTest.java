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

import junit.framework.TestCase;

import static java.util.Collections.singletonMap;

public class FeedbackingValueSourceTest
    extends TestCase
{

    public void testStandalone()
    {
        ValueSource valueSource = new FeedbackingValueSource();
        assertNull( valueSource.getValue( "test" ) );
        assertEquals( 1, valueSource.getFeedback().size() );
        assertEquals( "'test' not resolved", valueSource.getFeedback().iterator().next() );
    }

    public void testAfterResolvedExpression() throws InterpolationException
    {
        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource( new MapBasedValueSource( singletonMap( "key", "val" ) ) );
        interpolator.addValueSource( new FeedbackingValueSource() );
        assertEquals( "val", interpolator.interpolate( "${key}" ) );
        assertTrue( interpolator.getFeedback().isEmpty() );
    }

    public void testBeforeResolvedExpression() throws InterpolationException
    {
        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource( new FeedbackingValueSource("Resolving ${expression}") );
        interpolator.addValueSource( new MapBasedValueSource( singletonMap( "key", "val" ) ) );
        assertEquals( "val", interpolator.interpolate( "${key}" ) );
        assertEquals( 1, interpolator.getFeedback().size() );
        assertEquals( "Resolving key", interpolator.getFeedback().iterator().next() );
    }

    public void testAfterNotResolvedExpression() throws InterpolationException
    {
        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource( new MapBasedValueSource( singletonMap( "key", "val" ) ) );
        interpolator.addValueSource( new FeedbackingValueSource() );
        assertEquals( "${other-key}", interpolator.interpolate( "${other-key}" ) );
        assertEquals( 1, interpolator.getFeedback().size() );
    }
}
