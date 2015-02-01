package org.codehaus.plexus.interpolation.fixed;

import org.codehaus.plexus.interpolation.InterpolationPostProcessor;
import org.codehaus.plexus.interpolation.SimpleRecursionInterceptor;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class FixedRegexBasedInterpolatorTest
{
    @SuppressWarnings( "UnusedDeclaration" )
    public String getVar()
    {
        return "testVar";
    }

    @Test
    public void shouldFailOnExpressionCycle()
    {
        Properties props = new Properties();
        props.setProperty( "key1", "${key2}" );
        props.setProperty( "key2", "${key1}" );

        FixedRegexBasedInterpolator rbi = FixedRegexBasedInterpolator.create(
            new org.codehaus.plexus.interpolation.fixed.PropertiesBasedValueSource( props ) );

        InterpolationState is = new InterpolationState();
        is.setRecursionInterceptor( new SimpleRecursionInterceptor() );
        try
        {
            rbi.interpolate( "${key1}", is );

            fail( "Should detect expression cycle and fail." );
        }
        catch ( InterpolationCycleException e )
        {
            // expected
        }
    }

    @Test
    public void shouldResolveByMy_getVar_Method()
    {
        FixedRegexBasedInterpolator rbi =
            FixedRegexBasedInterpolator.create( new ObjectBasedValueSource( this ) );
        InterpolationState is = new InterpolationState();

        String result = rbi.withPrefix( "this" ).interpolate( "this is a ${this.var}", is );

        assertEquals( "this is a testVar", result );
    }

    @Test
    public void shouldResolveByContextValue()
    {
        Map<String, String> context = new HashMap<String, String>();
        context.put( "var", "testVar" );

        FixedRegexBasedInterpolator rbi = FixedRegexBasedInterpolator.create(
            new org.codehaus.plexus.interpolation.fixed.MapBasedValueSource( context ) );
        InterpolationState is = new InterpolationState();

        String result = rbi.withPrefix( "this" ).interpolate( "this is a ${this.var}", is );

        assertEquals( "this is a testVar", result );
    }

    @Test
    public void shouldResolveByEnvar()
        throws IOException
    {
        InterpolationState is = new InterpolationState();
        FixedRegexBasedInterpolator rbi =
            FixedRegexBasedInterpolator.create( new EnvarBasedValueSource() );

        String result = rbi.withPrefix( "this" ).interpolate( "this is a ${env.HOME}", is );

        assertFalse( "this is a ${HOME}".equals( result ) );
        assertFalse( "this is a ${env.HOME}".equals( result ) );
    }

    @Test
    public void useAlternateRegex()
        throws Exception
    {
        Map<String, String> context = new HashMap<String, String>();
        context.put( "var", "testVar" );

        InterpolationState is = new InterpolationState();
        FixedRegexBasedInterpolator rbi = FixedRegexBasedInterpolator.create( "\\@\\{(", ")?([^}]+)\\}@",
                                                                              new org.codehaus.plexus.interpolation.fixed.MapBasedValueSource(
                                                                                  context ) );

        String result = rbi.withPrefix( "this" ).interpolate( "this is a @{this.var}@", is );

        assertEquals( "this is a testVar", result );
    }

    @Test
    public void testNPEFree()
        throws Exception
    {
        Map<String, String> context = new HashMap<String, String>();
        context.put( "var", "testVar" );

        InterpolationState is = new InterpolationState();
        FixedRegexBasedInterpolator rbi = FixedRegexBasedInterpolator.create( "\\@\\{(", ")?([^}]+)\\}@",
                                                                              new org.codehaus.plexus.interpolation.fixed.MapBasedValueSource(
                                                                                  context ) );

        String result = rbi.interpolate( null, is );

        assertEquals( "", result );
    }

    @Test
    public void testUsePostProcessor_DoesNotChangeValue()
    {
        Map<String, String> context = new HashMap<String, String>();
        context.put( "test.var", "testVar" );

        InterpolationState is = new InterpolationState();
        FixedRegexBasedInterpolator rbi = FixedRegexBasedInterpolator.create(
            new org.codehaus.plexus.interpolation.fixed.MapBasedValueSource( context ) ).withPostProcessor(
            new InterpolationPostProcessor()
            {
                public Object execute( String expression, Object value )
                {
                    return null;
                }
            } );

        String result = rbi.interpolate( "this is a ${test.var}", is );

        assertEquals( "this is a testVar", result );
    }

    @Test
    public void testUsePostProcessor_ChangesValue()
    {
        int loopNumber = 200000;

        Map<String, String> context = new HashMap<String, String>();
        context.put( "test.var", "testVar" );

        InterpolationState is = new InterpolationState();
        FixedRegexBasedInterpolator rbi = FixedRegexBasedInterpolator.create(
            new org.codehaus.plexus.interpolation.fixed.MapBasedValueSource( context ) ).withPostProcessor(
            new InterpolationPostProcessor()
            {
                public Object execute( String expression, Object value )
                {
                    return value + "2";
                }
            } );

        for ( int i = 0; i < loopNumber; i++ )
        {
            String result = rbi.interpolate( "this is a ${test.var}", is );

            assertEquals( "this is a testVar2", result );
        }
    }

}