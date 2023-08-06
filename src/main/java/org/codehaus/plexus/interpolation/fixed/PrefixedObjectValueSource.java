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

import java.util.List;

/**
 * Wraps an arbitrary object with an {@link org.codehaus.plexus.interpolation.fixed.ObjectBasedValueSource} instance, then
 * wraps that source with a {@link org.codehaus.plexus.interpolation.fixed.PrefixedValueSourceWrapper} instance, to which
 * this class delegates all of its calls.
 */
public class PrefixedObjectValueSource extends AbstractDelegatingValueSource {

    /**
     * Wrap the specified root object, allowing the specified expression prefix.
     * @param prefix the prefix.
     * @param root The root of the graph.
     */
    public PrefixedObjectValueSource(String prefix, Object root) {
        super(new PrefixedValueSourceWrapper(new ObjectBasedValueSource(root), prefix));
    }

    /**
     * Wrap the specified root object, allowing the specified list of expression
     * prefixes and setting whether the {@link org.codehaus.plexus.interpolation.PrefixedValueSourceWrapper} allows
     * unprefixed expressions.
     * @param possiblePrefixes The list of possible prefixed.
     * @param root The root of the graph.
     * @param allowUnprefixedExpressions allow unrefixed expressions or not.
     */
    public PrefixedObjectValueSource(List<String> possiblePrefixes, Object root, boolean allowUnprefixedExpressions) {
        super(new PrefixedValueSourceWrapper(
                new ObjectBasedValueSource(root), possiblePrefixes, allowUnprefixedExpressions));
    }
}
