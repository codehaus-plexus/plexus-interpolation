package org.codehaus.plexus.interpolation.fixed;

/*
 * Copyright 2014 The Codehaus Foundation.
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

public interface FixedValueSource {
    /**
     * @param expression The expression.
     * @param interpolationState {@link InterpolationState}.
     * @return the value related to the expression, or null if not found (not available
     * from this source)
     */
    public Object getValue(String expression, InterpolationState interpolationState);
}
