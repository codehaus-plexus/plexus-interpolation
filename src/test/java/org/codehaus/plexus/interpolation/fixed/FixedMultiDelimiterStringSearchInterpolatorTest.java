package org.codehaus.plexus.interpolation.fixed;

import org.codehaus.plexus.interpolation.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FixedMultiDelimiterStringSearchInterpolatorTest
{

    @Test
    public void interpolationWithDifferentDelimiters()
        throws InterpolationException
    {
        Map<String,String> ctx = new HashMap<String,String>();
        ctx.put( "name", "User" );
        ctx.put( "otherName", "@name@" );

        String input = "${otherName}";

        FixedValueSource vs = new MapBasedValueSource( ctx );
        FixedMultiDelimiterStringSearchInterpolator interpolator = FixedMultiDelimiterStringSearchInterpolator.create().withDelimiterSpec(
            Arrays.asList( "@" ) )
            .withValueSource( vs );

        InterpolationState is = new InterpolationState();

        String result = interpolator.interpolate( input,is );

        assertEquals( ctx.get( "name" ), result );
    }

    @Test
    public void testSuccessiveInterpolationWithDifferentDelimiters_ReversedDelimiterSequence()
        throws InterpolationException
    {
        Map<String,String> ctx = new HashMap<String,String>();
        ctx.put( "name", "User" );
        ctx.put( "otherName", "${name}" );

        String input = "@otherName@";

        FixedValueSource vs = new MapBasedValueSource( ctx );
        FixedMultiDelimiterStringSearchInterpolator interpolator = FixedMultiDelimiterStringSearchInterpolator.create().addDelimiterSpec(
            "@" )
            .withValueSource( vs );

        InterpolationState is = new InterpolationState();

        String result = interpolator.interpolate( input, is );

        assertEquals( ctx.get( "name" ), result );
    }

}