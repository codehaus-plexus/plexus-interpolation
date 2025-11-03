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

import java.util.List;

/**
 * Interpolator interface. Based on existing RegexBasedInterpolator interface.
 *
 * @author cstamas
 */
public interface Interpolator extends BasicInterpolator {

    /**
     * Add a new {@link ValueSource} to the stack used to resolve expressions
     * in this interpolator instance.
     * @param valueSource {@link ValueSource}.
     */
    void addValueSource(ValueSource valueSource);

    /**
     * Remove the specified {@link ValueSource} from the stack used to resolve
     * expressions in this interpolator instance.
     * @param valueSource {@link ValueSource}.
     */
    void removeValuesSource(ValueSource valueSource);

    /**
     * Add a new post-processor to handle final processing after
     * recursively-interpolated value is determined.
     * @param postProcessor {@link InterpolationPostProcessor}.
     */
    void addPostProcessor(InterpolationPostProcessor postProcessor);

    /**
     * Remove the given post-processor.
     * @param postProcessor {@link InterpolationPostProcessor}.
     */
    void removePostProcessor(InterpolationPostProcessor postProcessor);

    /**
     * See {@link Interpolator#interpolate(String, String, RecursionInterceptor)}.
     * <p>
     * This method triggers the use of a {@link SimpleRecursionInterceptor}
     * instance for protection against expression cycles.</p>
     *
     * @param input The input string to interpolate
     *
     * @param thisPrefixPattern An optional pattern that should be trimmed from
     *                          the start of any expressions found in the input.
     * @return interpolated string.
     * @throws InterpolationException in case of an error.
     */
    String interpolate(String input, String thisPrefixPattern) throws InterpolationException;

    /**
     * Attempt to resolve all expressions in the given input string, using the
     * given pattern to first trim an optional prefix from each expression. The
     * supplied recursion interceptor will provide protection from expression
     * cycles, ensuring that the input can be resolved or an exception is
     * thrown.
     * <b>return an empty String if input is null</b>
     * @param input The input string to interpolate
     *
     * @param thisPrefixPattern An optional pattern that should be trimmed from
     *                          the start of any expressions found in the input.
     *
     * @param recursionInterceptor Used to protect the interpolation process
     *                             from expression cycles, and throw an
     *                             exception if one is detected.
     * @return interpolated string.
     * @throws InterpolationException in case of an error.
     */
    String interpolate(String input, String thisPrefixPattern, RecursionInterceptor recursionInterceptor)
            throws InterpolationException;

    /**
     * Attempt to resolve all expressions in the given input string, using the
     * provided lists of value sources and post processors. This method allows
     * for efficient interpolation without the need to repeatedly add and remove
     * value sources and post processors from the interpolator instance.
     * <p>
     * This method triggers the use of a {@link SimpleRecursionInterceptor}
     * instance for protection against expression cycles.</p>
     * <p>
     * <b>return an empty String if input is null</b>
     *
     * @param input The input string to interpolate
     * @param valueSources The list of value sources to use for resolving expressions.
     *                     If null or empty, no value sources will be used.
     * @param postProcessors The list of post processors to apply after interpolation.
     *                       If null or empty, no post processors will be used.
     * @return interpolated string.
     * @throws InterpolationException in case of an error.
     * @since 1.29
     */
    default String interpolate(
            String input, List<ValueSource> valueSources, List<InterpolationPostProcessor> postProcessors)
            throws InterpolationException {
        return interpolate(input, valueSources, postProcessors, new SimpleRecursionInterceptor());
    }

    /**
     * Attempt to resolve all expressions in the given input string, using the
     * provided lists of value sources and post processors. This method allows
     * for efficient interpolation without the need to repeatedly add and remove
     * value sources and post processors from the interpolator instance.
     * <p>
     * The supplied recursion interceptor will provide protection from expression
     * cycles, ensuring that the input can be resolved or an exception is thrown.
     * <p>
     * <b>return an empty String if input is null</b>
     *
     * @param input The input string to interpolate
     * @param valueSources The list of value sources to use for resolving expressions.
     *                     If null or empty, no value sources will be used.
     * @param postProcessors The list of post processors to apply after interpolation.
     *                       If null or empty, no post processors will be used.
     * @param recursionInterceptor Used to protect the interpolation process
     *                             from expression cycles, and throw an
     *                             exception if one is detected.
     * @return interpolated string.
     * @throws InterpolationException in case of an error.
     * @since 1.29
     */
    String interpolate(
            String input,
            List<ValueSource> valueSources,
            List<InterpolationPostProcessor> postProcessors,
            RecursionInterceptor recursionInterceptor)
            throws InterpolationException;

    /**
     * Return any feedback messages and errors that were generated - but
     * suppressed - during the interpolation process. Since unresolvable
     * expressions will be left in the source string as-is, this feedback is
     * optional, and will only be useful for debugging interpolation problems.
     *
     * @return a {@link List} that may be interspersed with {@link String} and
     * {@link Throwable} instances.
     */
    List getFeedback();

    /**
     * Clear the feedback messages from previous interpolate(..) calls.
     */
    void clearFeedback();

    /**
     * @return state of the cacheAnswers
     */
    boolean isCacheAnswers();

    /**
     * @param cacheAnswers true/false.
     */
    void setCacheAnswers(boolean cacheAnswers);

    void clearAnswers();
}
