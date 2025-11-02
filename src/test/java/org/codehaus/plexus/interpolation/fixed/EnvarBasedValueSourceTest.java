package org.codehaus.plexus.interpolation.fixed;

/*
 * Copyright 2007 The Codehaus Foundation.
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

import java.util.HashMap;

import org.codehaus.plexus.interpolation.os.OperatingSystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnvarBasedValueSourceTest {

    @BeforeEach
    void setUp() {
        EnvarBasedValueSource.resetStatics();
    }

    @Test
    void noArgConstructorIsCaseSensitive() throws Exception {
        OperatingSystemUtils.setEnvVarSource(() -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("aVariable", "variable");
            return map;
        });

        EnvarBasedValueSource source = new EnvarBasedValueSource();

        assertEquals("variable", source.getValue("aVariable", null));
        assertEquals("variable", source.getValue("env.aVariable", null));
        assertNull(source.getValue("AVARIABLE", null));
        assertNull(source.getValue("env.AVARIABLE", null));
    }

    @Test
    void caseInsensitive() throws Exception {
        OperatingSystemUtils.setEnvVarSource(() -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("aVariable", "variable");
            return map;
        });

        EnvarBasedValueSource source = new EnvarBasedValueSource(false);

        assertEquals("variable", source.getValue("aVariable", null));
        assertEquals("variable", source.getValue("env.aVariable", null));
        assertEquals("variable", source.getValue("AVARIABLE", null));
        assertEquals("variable", source.getValue("env.AVARIABLE", null));
    }

    @Test
    void getRealEnvironmentVariable() throws Exception {
        OperatingSystemUtils.setEnvVarSource(new OperatingSystemUtils.DefaultEnvVarSource());

        EnvarBasedValueSource source = new EnvarBasedValueSource();

        String realEnvVar = "JAVA_HOME";

        String realValue = System.getenv().get(realEnvVar);
        assertNotNull(realValue, "Can't run this test until " + realEnvVar + " env variable is set");

        assertEquals(realValue, source.getValue(realEnvVar, null));
    }
}
