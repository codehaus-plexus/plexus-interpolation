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

import org.codehaus.plexus.interpolation.InterpolationPostProcessor;
import org.codehaus.plexus.interpolation.SimpleRecursionInterceptor;
import org.codehaus.plexus.interpolation.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Expansion of the original RegexBasedInterpolator, found in plexus-utils, this
 * interpolator provides options for setting custom prefix/suffix regex parts,
 * and includes a {@link org.codehaus.plexus.interpolation.RecursionInterceptor} parameter in its interpolate(..)
 * call, to allow the detection of cyclical expression references.
 *
 * @version $Id$
 */
public class FixedRegexBasedInterpolator
    implements FixedInterpolator
{

    private final String startRegex;

    private final String endRegex;

    private final String prefixPattern;

    private final List<FixedValueSource> valueSources;

    private final InterpolationPostProcessor postProcessor;

    private boolean reusePatterns = false;

    public static final String DEFAULT_REGEXP = "\\$\\{(.+?)\\}";

    /**
     * the key is the regex the value is the Pattern
     * At the class construction time the Map will contains the default Pattern
     */
    private Map<String, Pattern> compiledPatterns = new WeakHashMap<String, Pattern>();

    private FixedRegexBasedInterpolator( String startRegex, String endRegex, String prefixPattern,
                                         List<FixedValueSource> valueSources, InterpolationPostProcessor postProcessor,
                                         boolean reusePatterns )
    {
        this.startRegex = startRegex;
        this.endRegex = endRegex;
        this.valueSources = valueSources;
        this.prefixPattern = prefixPattern != null && prefixPattern.length() > 0 ? prefixPattern : null;
        this.postProcessor = postProcessor;
        this.reusePatterns = reusePatterns;
        compiledPatterns.put( DEFAULT_REGEXP, Pattern.compile( DEFAULT_REGEXP ) );
    }



    public static FixedRegexBasedInterpolator create()
    {
        return new FixedRegexBasedInterpolator( null, null, null, new ArrayList<FixedValueSource>(), null, false );
    }


    public static FixedRegexBasedInterpolator create(String startRegex, String endRegex, FixedValueSource... fixedValueSources)
    {
        return FixedRegexBasedInterpolator.create().withStartRegex( startRegex )
            .withEndRegex( endRegex ).withValueSources( fixedValueSources );
    }

    public static FixedRegexBasedInterpolator create( FixedValueSource... fixedValueSources)
    {
        return create().withValueSources( fixedValueSources );
    }

    public FixedRegexBasedInterpolator withPrefix( String prefix )
    {
        return new FixedRegexBasedInterpolator( startRegex, endRegex, prefix, valueSources, postProcessor,
                                                reusePatterns );
    }

    public FixedRegexBasedInterpolator withValueSources( FixedValueSource... valueSources )
    {
        FixedRegexBasedInterpolator result = this;
        for ( FixedValueSource valueSource : valueSources )
        {
            result = withValueSource( valueSource );
        }
        return result;
    }

    public FixedRegexBasedInterpolator withValueSource( FixedValueSource vs )
    {
        List<FixedValueSource> sources = new ArrayList<FixedValueSource>( valueSources );
        sources.add( vs );
        return new FixedRegexBasedInterpolator( startRegex, endRegex, prefixPattern, sources, postProcessor,
                                                reusePatterns );
    }

    public FixedRegexBasedInterpolator withStartRegex( String startRegex )
    {
        return new FixedRegexBasedInterpolator( startRegex, endRegex, prefixPattern, valueSources, postProcessor,
                                                reusePatterns );
    }

    public FixedRegexBasedInterpolator withEndRegex( String endRegex )
    {
        return new FixedRegexBasedInterpolator( startRegex, endRegex, prefixPattern, valueSources, postProcessor,
                                                reusePatterns );
    }

    public FixedRegexBasedInterpolator withPostProcessor( InterpolationPostProcessor postProcessor )
    {
        return new FixedRegexBasedInterpolator( startRegex, endRegex, prefixPattern, valueSources, postProcessor,
                                                reusePatterns );
    }

    public FixedRegexBasedInterpolator reusePatterns( boolean reusePatterns )
    {
        return new FixedRegexBasedInterpolator( startRegex, endRegex, prefixPattern, valueSources, postProcessor,
                                                reusePatterns );
    }

    /**
     * Attempt to resolve all expressions in the given input string, using the
     * given pattern to first trim an optional prefix from each expression. The
     * supplied recursion interceptor will provide protection from expression
     * cycles, ensuring that the input can be resolved or an exception is
     * thrown.
     *
     * @param input              The input string to interpolate
     * @param interpolationState The state used during interpolation.
     */
    public String interpolate( String input, InterpolationState interpolationState )
        throws org.codehaus.plexus.interpolation.fixed.InterpolationCycleException
    {
        if ( input == null )
        {
            // return empty String to prevent NPE too
            return "";
        }
        if ( interpolationState.recursionInterceptor == null )
        {
            interpolationState.setRecursionInterceptor( new SimpleRecursionInterceptor() );
        }

        int realExprGroup = 2;
        Pattern expressionPattern;
        if ( startRegex != null || endRegex != null )
        {
            if ( prefixPattern == null )
            {
                expressionPattern = getPattern( startRegex + endRegex );
                realExprGroup = 1;
            }
            else
            {
                expressionPattern = getPattern( startRegex + prefixPattern + endRegex );
            }

        }
        else if ( prefixPattern != null )
        {
            expressionPattern = getPattern( "\\$\\{(" + prefixPattern + ")?(.+?)\\}" );
        }
        else
        {
            expressionPattern = getPattern( DEFAULT_REGEXP );
            realExprGroup = 1;
        }

        return interpolate( input, interpolationState, expressionPattern, realExprGroup );
    }

    private Pattern getPattern( String regExp )
    {
        if ( !reusePatterns )
        {
            return Pattern.compile( regExp );
        }

        Pattern pattern;
        synchronized ( this )
        {
            pattern = compiledPatterns.get( regExp );

            if ( pattern != null )
            {
                return pattern;
            }

            pattern = Pattern.compile( regExp );
            compiledPatterns.put( regExp, pattern );
        }

        return pattern;
    }

    /**
     * Entry point for recursive resolution of an expression and all of its
     * nested expressions.
     *
     * @todo Ensure unresolvable expressions don't trigger infinite recursion.
     */
    private String interpolate( String input, InterpolationState interpolationState, Pattern expressionPattern,
                                int realExprGroup )
    {
        if ( input == null )
        {
            // return empty String to prevent NPE too
            return "";
        }
        String result = input;

        Matcher matcher = expressionPattern.matcher( result );

        while ( matcher.find() )
        {
            String wholeExpr = matcher.group( 0 );
            String realExpr = matcher.group( realExprGroup );

            if ( realExpr.startsWith( "." ) )
            {
                realExpr = realExpr.substring( 1 );
            }

            if ( interpolationState.recursionInterceptor.hasRecursiveExpression( realExpr ) )
            {
                throw new InterpolationCycleException( interpolationState.recursionInterceptor, realExpr, wholeExpr );
            }

            interpolationState.recursionInterceptor.expressionResolutionStarted( realExpr );
            try
            {
                Object value = null;
                for ( FixedValueSource vs : valueSources )
                {
                    if ( value != null )
                    {
                        break;
                    }

                    value = vs.getValue( realExpr, interpolationState );
                }

                if ( value != null )
                {
                    value =
                        interpolate( String.valueOf( value ), interpolationState, expressionPattern, realExprGroup );

                    if ( postProcessor != null )
                    {
                        Object newVal = postProcessor.execute( realExpr, value );
                        if ( newVal != null )
                        {
                            value = newVal;
                        }
                    }

                    // could use:
                    // result = matcher.replaceFirst( stringValue );
                    // but this could result in multiple lookups of stringValue, and replaceAll is not correct behaviour
                    result = StringUtils.replace( result, wholeExpr, String.valueOf( value ) );

                    matcher.reset( result );
                }
            }
            finally
            {
                interpolationState.recursionInterceptor.expressionResolutionFinished( realExpr );
            }
        }

        return result;
    }

}
