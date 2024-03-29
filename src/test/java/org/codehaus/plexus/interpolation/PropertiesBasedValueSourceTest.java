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

import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PropertiesBasedValueSourceTest {

    @Test
    public void testPropertyShouldReturnValueFromProperties() {
        Properties props = new Properties();

        String key = "key";
        String value = "value";

        props.setProperty(key, value);

        PropertiesBasedValueSource vs = new PropertiesBasedValueSource(props);

        assertNotNull(vs.getValue(key));
    }

    @Test
    public void testPropertyShouldReturnNullWhenPropertyMissing() {
        Properties props = new Properties();

        String key = "key";

        PropertiesBasedValueSource vs = new PropertiesBasedValueSource(props);

        assertNull(vs.getValue(key));
    }
}
