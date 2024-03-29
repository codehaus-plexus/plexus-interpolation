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

import java.util.Properties;

/**
 * {@link org.codehaus.plexus.interpolation.fixed.FixedValueSource} implementation that wraps a {@link java.util.Properties} instance,
 * and does a simple lookup of the entire expression string as the parameter for
 * {@link java.util.Properties#getProperty(String)}, returning the result as the resolved
 * value.
 *
 * @author jdcasey
 */
public class PropertiesBasedValueSource implements FixedValueSource {

    private final Properties properties;

    /**
     * Wrap the specified {@link java.util.Properties} object for use as a value source.
     * Nulls are allowed.
     *
     * @param properties The properties instance to wrap.
     */
    public PropertiesBasedValueSource(Properties properties) {
        this.properties = properties;
    }

    /**
     * @return the result of {@link java.util.Properties#getProperty(String)}, using the
     * entire expression as the key to lookup. If the wrapped properties instance
     * is null, simply return null.
     */
    public Object getValue(String expression, InterpolationState interpolationState) {
        return properties == null ? null : properties.getProperty(expression);
    }
}
