package org.codehaus.plexus.interpolation;

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

        assertEquals("variable", source.getValue("aVariable"));
        assertEquals("variable", source.getValue("env.aVariable"));
        assertNull(source.getValue("AVARIABLE"));
        assertNull(source.getValue("env.AVARIABLE"));
    }

    @Test
    void caseInsensitive() throws Exception {
        OperatingSystemUtils.setEnvVarSource(() -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("aVariable", "variable");
            return map;
        });

        EnvarBasedValueSource source = new EnvarBasedValueSource(false);

        assertEquals("variable", source.getValue("aVariable"));
        assertEquals("variable", source.getValue("env.aVariable"));
        assertEquals("variable", source.getValue("AVARIABLE"));
        assertEquals("variable", source.getValue("env.AVARIABLE"));
    }

    @Test
    void getRealEnvironmentVariable() throws Exception {
        OperatingSystemUtils.setEnvVarSource(new OperatingSystemUtils.DefaultEnvVarSource());

        EnvarBasedValueSource source = new EnvarBasedValueSource();

        String realEnvVar = "JAVA_HOME";

        String realValue = System.getenv().get(realEnvVar);
        assertNotNull(realValue, "Can't run this test until " + realEnvVar + " env variable is set");

        assertEquals(realValue, source.getValue(realEnvVar));
    }
}
