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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.interpolation.BasicInterpolator;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.InterpolationPostProcessor;
import org.codehaus.plexus.interpolation.RecursionInterceptor;

/**
 * <p>
 * A fixed string search interpolator is permanently bound to a given set of value sources,
 * an is totally fixed and stateless over these value sources.</p>
 * <p>The fixed interpolator is also a #StatelessValueSource and can be used as a source
 * for a different fixed interpolator, creating a scope chain.</p>
 * <p>Once constructed, this interpolator will always point to the same set of objects (value sources),
 * in such a way that if the underlying object is fixed, expressions will always
 * evaluate to the same result.</p>
 * <p>The fixed interpolator can be shared among different clients and is thread safe to
 * the extent the underlying value sources can be accessed safely.
 * Since interpolation expressions cannot modify the objects, thread safety concerns
 * this will normally be limited to safe publication and memory model visibility of
 * underlying objects.</p>
 * <p>The fixed interpolator can be a valuesource</p>
 */
public class FixedStringSearchInterpolator implements FixedValueSource {

    private final FixedValueSource[] valueSources;

    private final InterpolationPostProcessor postProcessor;

    public static final String DEFAULT_START_EXPR = "${";

    public static final String DEFAULT_END_EXPR = "}";

    private final String startExpr;

    private final String endExpr;

    private final String escapeString;

    private FixedStringSearchInterpolator(
            String startExpr,
            String endExpr,
            String escapeString,
            InterpolationPostProcessor postProcessor,
            FixedValueSource... valueSources) {
        this.startExpr = startExpr;
        this.endExpr = endExpr;
        this.escapeString = escapeString;
        if (valueSources == null) {
            throw new IllegalArgumentException("valueSources cannot be null");
        }
        for (int i = 0; i < valueSources.length; i++) {
            if (valueSources[i] == null) {
                throw new IllegalArgumentException("valueSources[" + i + "] is null");
            }
        }

        this.valueSources = valueSources;
        this.postProcessor = postProcessor;
    }

    public static FixedStringSearchInterpolator create(
            String startExpr, String endExpr, FixedValueSource... valueSources) {
        return new FixedStringSearchInterpolator(startExpr, endExpr, null, null, valueSources);
    }

    public static FixedStringSearchInterpolator create(FixedValueSource... valueSources) {
        return new FixedStringSearchInterpolator(DEFAULT_START_EXPR, DEFAULT_END_EXPR, null, null, valueSources);
    }

    public static FixedStringSearchInterpolator createWithPermittedNulls(FixedValueSource... valueSources) {
        List<FixedValueSource> nonnulls = new ArrayList<FixedValueSource>();
        for (FixedValueSource item : valueSources) {
            if (item != null) nonnulls.add(item);
        }
        return new FixedStringSearchInterpolator(
                DEFAULT_START_EXPR,
                DEFAULT_END_EXPR,
                null,
                null,
                nonnulls.toArray(new FixedValueSource[nonnulls.size()]));
    }

    public FixedStringSearchInterpolator withExpressionMarkers(String startExpr, String endExpr) {
        return new FixedStringSearchInterpolator(startExpr, endExpr, escapeString, postProcessor, valueSources);
    }

    public FixedStringSearchInterpolator withPostProcessor(InterpolationPostProcessor postProcessor) {
        return new FixedStringSearchInterpolator(startExpr, endExpr, escapeString, postProcessor, valueSources);
    }

    public FixedStringSearchInterpolator withEscapeString(String escapeString) {
        return new FixedStringSearchInterpolator(startExpr, endExpr, escapeString, postProcessor, valueSources);
    }

    public String interpolate(String input) throws InterpolationCycleException {
        return interpolate(input, new InterpolationState());
    }

    public static FixedStringSearchInterpolator empty() {
        return create();
    }

    // Find out how to return null when we cannot interpolate this expression
    // At this point we should always be a ${expr}
    public Object getValue(String realExpr, InterpolationState interpolationState) {

        interpolationState.recursionInterceptor.expressionResolutionStarted(realExpr);

        try {
            Object value = null;

            for (FixedValueSource valueSource : valueSources) {
                value = valueSource.getValue(realExpr, interpolationState);
                if (value != null) {
                    break;
                }
            }

            if (value != null) {
                if (interpolationState.root != null) {
                    value = interpolationState.root.interpolate(String.valueOf(value), interpolationState);
                }
                return String.valueOf(value);
            } else {
                return null;
            }
        } finally {
            interpolationState.recursionInterceptor.expressionResolutionFinished(realExpr);
        }
    }

    public BasicInterpolator asBasicInterpolator() {
        final InterpolationState is = new InterpolationState();
        return new BasicInterpolator() {

            public String interpolate(String input) throws InterpolationException {
                return FixedStringSearchInterpolator.this.interpolate(input, is);
            }

            public String interpolate(String input, RecursionInterceptor recursionInterceptor)
                    throws InterpolationException {
                is.setRecursionInterceptor(recursionInterceptor);
                return FixedStringSearchInterpolator.this.interpolate(input, is);
            }
        };
    }

    public String interpolate(String input, InterpolationState interpolationState) throws InterpolationCycleException {
        if (interpolationState.root == null) {
            interpolationState.root = this;
        }

        if (input == null) {
            // return empty String to prevent NPE too
            return "";
        }
        StringBuilder result = new StringBuilder(input.length() * 2);

        int startIdx;
        int endIdx = -1;
        while ((startIdx = input.indexOf(startExpr, endIdx + 1)) > -1) {
            result.append(input, endIdx + 1, startIdx);

            endIdx = input.indexOf(endExpr, startIdx + 1);
            if (endIdx < 0) {
                break;
            }

            final String wholeExpr = input.substring(startIdx, endIdx + endExpr.length());
            String realExpr = wholeExpr.substring(startExpr.length(), wholeExpr.length() - endExpr.length());

            if (startIdx >= 0 && escapeString != null && escapeString.length() > 0) {
                int startEscapeIdx = startIdx == 0 ? 0 : startIdx - escapeString.length();
                if (startEscapeIdx >= 0) {
                    String escape = input.substring(startEscapeIdx, startIdx);
                    if (escapeString.equals(escape)) {
                        result.append(wholeExpr);
                        result.replace(startEscapeIdx, startEscapeIdx + escapeString.length(), "");
                        continue;
                    }
                }
            }

            boolean resolved = false;
            if (!interpolationState.unresolvable.contains(wholeExpr)) {
                if (realExpr.startsWith(".")) {
                    realExpr = realExpr.substring(1);
                }

                if (interpolationState.recursionInterceptor.hasRecursiveExpression(realExpr)) {
                    throw new InterpolationCycleException(interpolationState.recursionInterceptor, realExpr, wholeExpr);
                }

                Object value = getValue(realExpr, interpolationState);
                if (value != null) {
                    value = interpolate(String.valueOf(value), interpolationState);

                    if (postProcessor != null) {
                        Object newVal = postProcessor.execute(realExpr, value);
                        if (newVal != null) {
                            value = newVal;
                        }
                    }

                    result.append(String.valueOf(value));
                    resolved = true;
                } else {
                    interpolationState.unresolvable.add(wholeExpr);
                }
            }

            if (!resolved) {
                result.append(wholeExpr);
            }

            if (endIdx > -1) {
                endIdx += endExpr.length() - 1;
            }
        }

        if (endIdx == -1 && startIdx > -1) {
            result.append(input, startIdx, input.length());
        } else if (endIdx < input.length()) {
            result.append(input, endIdx + 1, input.length());
        }

        return result.toString();
    }
}
