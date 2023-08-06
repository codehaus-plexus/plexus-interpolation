package org.codehaus.plexus.interpolation;
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

/**
 * Knows how to do basic interpolation services.
 *
 * TODO: Really really needs a way to communicate errors.
 */
public interface BasicInterpolator {
    /**
     * See {@link org.codehaus.plexus.interpolation.Interpolator#interpolate(String, String, org.codehaus.plexus.interpolation.RecursionInterceptor)}.
     * <p>
     * This method triggers the use of a {@link org.codehaus.plexus.interpolation.SimpleRecursionInterceptor}
     * instance for protection against expression cycles. It also leaves empty the
     * expression prefix which would otherwise be trimmed from expressions. The
     * result is that any detected expression will be resolved as-is.</p>
     *
     * @param input The input string to interpolate
     * @return the interpolated string.
     * @throws InterpolationException in case of an error.
     */
    String interpolate(String input) throws InterpolationException;

    /**
     * See {@link org.codehaus.plexus.interpolation.Interpolator#interpolate(String, String, org.codehaus.plexus.interpolation.RecursionInterceptor)}.
     * <p>
     * This method leaves empty the expression prefix which would otherwise be
     * trimmed from expressions. The result is that any detected expression will
     * be resolved as-is.</p>
     *
     * @param input The input string to interpolate
     *
     * @param recursionInterceptor Used to protect the interpolation process
     *                             from expression cycles, and throw an
     *                             exception if one is detected.
     * @return the interpolated string.
     * @throws InterpolationException in case of an error.
     */
    String interpolate(String input, RecursionInterceptor recursionInterceptor) throws InterpolationException;
}
