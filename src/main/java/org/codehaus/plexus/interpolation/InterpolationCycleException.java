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

public class InterpolationCycleException extends InterpolationException {

    private static final long serialVersionUID = 1L;

    public InterpolationCycleException(RecursionInterceptor recursionInterceptor, String realExpr, String wholeExpr) {
        super(
                "Detected the following recursive expression cycle in '" + realExpr + "': "
                        + recursionInterceptor.getExpressionCycle(realExpr),
                wholeExpr);
    }
}
