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

import java.util.List;

import org.codehaus.plexus.interpolation.util.ValueSourceUtils;

/**
 * {@link org.codehaus.plexus.interpolation.fixed.FixedValueSource} implementation which simply wraps another
 * value source, and trims any of a set of possible expression prefixes before delegating the
 * modified expression to be resolved by the real value source.
 *
 * @author jdcasey
 * @author krosenvold
 */
public class PrefixedValueSourceWrapper implements FixedValueSource {

    private final FixedValueSource valueSource;

    private final String[] possiblePrefixes;

    private boolean allowUnprefixedExpressions;

    private String lastExpression;

    /**
     * Wrap the given value source, but first trim the given prefix from any
     * expressions before they are passed along for resolution. If an expression
     * doesn't start with the given prefix, do not resolve it.
     *
     * @param valueSource The {@link org.codehaus.plexus.interpolation.ValueSource} to wrap.
     * @param prefix      The expression prefix to trim.
     */
    public PrefixedValueSourceWrapper(FixedValueSource valueSource, String prefix) {
        this.valueSource = valueSource;
        possiblePrefixes = new String[] {prefix};
    }

    /**
     * Wrap the given value source, but first trim the given prefix from any
     * expressions before they are passed along for resolution. If an expression
     * doesn't start with the given prefix and the allowUnprefixedExpressions flag
     * is set to true, simply pass the expression through to the nested value source
     * unchanged. If this flag is false, only allow resolution of those expressions
     * that start with the specified prefix.
     *
     * @param valueSource                The {@link org.codehaus.plexus.interpolation.ValueSource} to wrap.
     * @param prefix                     The expression prefix to trim.
     * @param allowUnprefixedExpressions Flag telling the wrapper whether to
     *                                   continue resolving expressions that don't start with the prefix it tracks.
     */
    public PrefixedValueSourceWrapper(FixedValueSource valueSource, String prefix, boolean allowUnprefixedExpressions) {
        this.valueSource = valueSource;
        possiblePrefixes = new String[] {prefix};
        this.allowUnprefixedExpressions = allowUnprefixedExpressions;
    }

    /**
     * Wrap the given value source, but first trim one of the given prefixes from any
     * expressions before they are passed along for resolution. If an expression
     * doesn't start with one of the given prefixes, do not resolve it.
     *
     * @param valueSource      The {@link org.codehaus.plexus.interpolation.ValueSource} to wrap.
     * @param possiblePrefixes The List of expression prefixes to trim.
     */
    public PrefixedValueSourceWrapper(FixedValueSource valueSource, List<String> possiblePrefixes) {
        this.valueSource = valueSource;
        this.possiblePrefixes = possiblePrefixes.toArray(new String[possiblePrefixes.size()]);
    }

    /**
     * Wrap the given value source, but first trim one of the given prefixes from any
     * expressions before they are passed along for resolution. If an expression
     * doesn't start with the given prefix and the allowUnprefixedExpressions flag
     * is set to true, simply pass the expression through to the nested value source
     * unchanged. If this flag is false, only allow resolution of those expressions
     * that start with the specified prefix.
     *
     * @param valueSource                The {@link org.codehaus.plexus.interpolation.ValueSource} to wrap.
     * @param possiblePrefixes           The List of expression prefixes to trim.
     * @param allowUnprefixedExpressions Flag telling the wrapper whether to
     *                                   continue resolving expressions that don't start with one of the prefixes it tracks.
     */
    public PrefixedValueSourceWrapper(
            FixedValueSource valueSource, List<String> possiblePrefixes, boolean allowUnprefixedExpressions) {
        this.valueSource = valueSource;
        this.possiblePrefixes = possiblePrefixes.toArray(new String[possiblePrefixes.size()]);
        this.allowUnprefixedExpressions = allowUnprefixedExpressions;
    }

    public Object getValue(String expression, InterpolationState interpolationState) {
        expression = ValueSourceUtils.trimPrefix(expression, possiblePrefixes, allowUnprefixedExpressions);

        if (expression == null) {
            return null;
        }

        return valueSource.getValue(expression, interpolationState);
    }
}
