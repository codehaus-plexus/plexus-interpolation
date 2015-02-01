package org.codehaus.plexus.interpolation.fixed;
/*
 * Copyright 2014 Codehaus Foundation.
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

import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.RecursionInterceptor;

/**
 * A stateless interpolator that can be re-used safely.
 * May also be thread safe, depending on safety of underlying model objects
 *
 */
public interface FixedInterpolator
{
    /**
     * Interpolate the supplied string with the enclosed interpolationstate
     * @param interpolationState the state of the interpolation operation
     * @param input The input string to interpolate
     * @return the interpolated value
     */

    public String interpolate( String input, InterpolationState interpolationState )
        throws InterpolationCycleException;

}
