package org.codehaus.plexus.interpolation.fixed;

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

import org.codehaus.plexus.interpolation.InterpolationPostProcessor;
import org.codehaus.plexus.interpolation.multi.DelimiterSpecification;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class FixedMultiDelimiterStringSearchInterpolator
    implements FixedInterpolator
{

    private static final int MAX_TRIES = 10;

    private final List<FixedValueSource> valueSources;

    private final InterpolationPostProcessor postProcessors;

    private final LinkedHashSet<DelimiterSpecification> delimiters;

    private String escapeString;

    public FixedMultiDelimiterStringSearchInterpolator( List<FixedValueSource> valueSources, InterpolationPostProcessor postProcessors,
                                                        LinkedHashSet<DelimiterSpecification> delimiters )
    {
        this.valueSources = valueSources;
        this.postProcessors = postProcessors;
        this.delimiters = delimiters;
        this.delimiters.add( DelimiterSpecification.DEFAULT_SPEC );
    }

    public static FixedMultiDelimiterStringSearchInterpolator create( )
    {
        LinkedHashSet<DelimiterSpecification> delimiters = new LinkedHashSet<DelimiterSpecification>( 1 );
        delimiters.add(DelimiterSpecification.DEFAULT_SPEC );
        return new FixedMultiDelimiterStringSearchInterpolator( new ArrayList<FixedValueSource>(  ), null,  delimiters);
    }

    public FixedMultiDelimiterStringSearchInterpolator withDelimiterSpec( DelimiterSpecification vs )
    {
        LinkedHashSet<DelimiterSpecification> newList = new LinkedHashSet<DelimiterSpecification>( delimiters );
        newList.add( vs);
        return new FixedMultiDelimiterStringSearchInterpolator( valueSources, postProcessors, newList);
    }

    public FixedMultiDelimiterStringSearchInterpolator withDelimiterSpec( String delimiterSpec )
    {
        return withDelimiterSpec( DelimiterSpecification.parse( delimiterSpec ));
    }

    public FixedMultiDelimiterStringSearchInterpolator withDelimiterSpec( Iterable<String> delimiterSpec )
    {
        FixedMultiDelimiterStringSearchInterpolator current = this;
        for ( String s : delimiterSpec )
        {
            current = current.withDelimiterSpec( s );
        }
        return current;
    }


    public FixedMultiDelimiterStringSearchInterpolator addDelimiterSpec( String delimiterSpec )
    {
        if ( delimiterSpec == null )
        {
            return this;
        }
        delimiters.add( DelimiterSpecification.parse( delimiterSpec ) );
        return this;
    }


    public FixedMultiDelimiterStringSearchInterpolator withValueSource( FixedValueSource vs )
    {
        addValueSource( vs );
        return this;
    }

    public FixedMultiDelimiterStringSearchInterpolator withPostProcessor( InterpolationPostProcessor postProcessor )
    {
        return new FixedMultiDelimiterStringSearchInterpolator( valueSources, postProcessor, delimiters );
    }

    /**
     * {@inheritDoc}
     */
    public void addValueSource( FixedValueSource valueSource )
    {
        valueSources.add( valueSource );
    }


    public String interpolate( String input, InterpolationState interpolationState )
    {
        if ( input == null )
        {
            // return empty String to prevent NPE too
            return "";
        }
        StringBuilder result = new StringBuilder( input.length() * 2 );

        String lastResult = input;
        int tries = 0;
        do
        {
            tries++;
            if ( result.length() > 0 )
            {
                lastResult = result.toString();
                result.setLength( 0 );
            }

            int startIdx = -1;
            int endIdx = -1;

            DelimiterSpecification selectedSpec;
            while( ( selectedSpec = select( input, endIdx ) ) != null )
            {
                String startExpr = selectedSpec.getBegin();
                String endExpr = selectedSpec.getEnd();

                startIdx = selectedSpec.getNextStartIndex();
                result.append( input, endIdx + 1, startIdx );

                endIdx = input.indexOf( endExpr, startIdx + 1 );
                if ( endIdx < 0 )
                {
                    break;
                }

                String wholeExpr = input.substring( startIdx, endIdx + endExpr.length() );
                String realExpr = wholeExpr.substring( startExpr.length(), wholeExpr.length() - endExpr.length() );

                if ( startIdx >= 0 && escapeString != null && escapeString.length() > 0 )
                {
                    int startEscapeIdx = startIdx == 0 ? 0 : startIdx - escapeString.length();
                    if ( startEscapeIdx >= 0 )
                    {
                        String escape = input.substring( startEscapeIdx, startIdx );
                        if ( escape != null && escapeString.equals( escape ) )
                        {
                            result.append( wholeExpr );
                            result.replace( startEscapeIdx, startEscapeIdx + escapeString.length(), "" );
                            continue;
                        }
                    }
                }

                boolean resolved = false;
                if ( ! interpolationState.unresolvable.contains( wholeExpr ) )
                {
                    if ( realExpr.startsWith( "." ) )
                    {
                        realExpr = realExpr.substring( 1 );
                    }

                    if ( interpolationState.recursionInterceptor.hasRecursiveExpression( realExpr ) )
                    {
                        throw new InterpolationCycleException( interpolationState.recursionInterceptor, realExpr, wholeExpr );
                    }

                    interpolationState.recursionInterceptor.expressionResolutionStarted( realExpr );

                    Object value = null;
                    Object bestAnswer = null;
                    for ( FixedValueSource vs : valueSources )
                    {
                        if (value != null ) break;

                        value = vs.getValue( realExpr, interpolationState );

                        if ( value != null && value.toString().contains( wholeExpr ) )
                        {
                            bestAnswer = value;
                            value = null;
                        }
                    }

                    // this is the simplest recursion check to catch exact recursion
                    // (non synonym), and avoid the extra effort of more string
                    // searching.
                    if ( value == null && bestAnswer != null )
                    {
                        throw new InterpolationCycleException( interpolationState.recursionInterceptor, realExpr, wholeExpr );
                    }

                    if ( value != null )
                    {
                        value = interpolate( String.valueOf( value ), interpolationState );

                        if ( postProcessors != null  )
                        {
                            Object newVal = postProcessors.execute( realExpr, value );
                            if ( newVal != null )
                            {
                                value = newVal;
                            }
                        }

                        // could use:
                        // result = matcher.replaceFirst( stringValue );
                        // but this could result in multiple lookups of stringValue, and replaceAll is not correct behaviour
                        result.append( String.valueOf( value ) );
                        resolved = true;
                    }
                    else
                    {
                        interpolationState.unresolvable.add( wholeExpr );
                    }

                    interpolationState.recursionInterceptor.expressionResolutionFinished( realExpr );
                }

                if ( !resolved )
                {
                    result.append( wholeExpr );
                }

                if ( endIdx > -1 )
                {
                    endIdx += endExpr.length() - 1;
                }
            }

            if ( endIdx == -1 && startIdx > -1 )
            {
                result.append( input, startIdx, input.length() );
            }
            else if ( endIdx < input.length() )
            {
                result.append( input, endIdx + 1, input.length() );
            }
        }
        while( !lastResult.equals( result.toString() ) && tries < MAX_TRIES );

        return result.toString();
    }

    private DelimiterSpecification select( String input, int lastEndIdx )
    {
        DelimiterSpecification selected = null;

        for ( DelimiterSpecification spec : delimiters )
        {
            spec.clearNextStart();

            if ( selected == null )
            {
                int idx = input.indexOf( spec.getBegin(), lastEndIdx + 1 );
                if ( idx > -1 )
                {
                    spec.setNextStartIndex( idx );
                    selected = spec;
                }
            }
        }

        return selected;
    }
}
