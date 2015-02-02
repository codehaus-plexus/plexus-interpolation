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

import org.codehaus.plexus.interpolation.ValueSource;

import java.util.Collections;
import java.util.List;

/**
 * If the expression matches, simply return the response object.
 * @since 1.12
 */
public class FixedSingleResponseValueSource
    implements FixedValueSource
{

    private final String expression;
    private final Object response;

    public FixedSingleResponseValueSource( String expression, Object response )
    {
        this.expression = expression;
        this.response = response;
    }


    public Object getValue( String expression, InterpolationState interpolationState )
    {
        if ( this.expression.equals( expression ) )
        {
            return response;
        }

        return null;
    }

}
