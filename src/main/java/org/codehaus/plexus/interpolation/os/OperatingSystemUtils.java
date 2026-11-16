package org.codehaus.plexus.interpolation.os;

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

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * <b>NOTE:</b> This class was copied from plexus-utils, to allow this library
 * to stand completely self-contained.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 */
public final class OperatingSystemUtils {

    private static EnvVarSource envVarSource = new DefaultEnvVarSource();

    private OperatingSystemUtils() {}

    public static Properties getSystemEnvVars() throws IOException {
        return getSystemEnvVars(true);
    }

    /**
     * Return the shell environment variables. If <code>caseSensitive == true</code>, then envar
     * keys will all be upper-case.
     *
     * @param caseSensitive Whether environment variable keys should be treated case-sensitively.
     * @return Properties object of (possibly modified) envar keys mapped to their values.
     * @throws IOException in case of an error.
     */
    public static Properties getSystemEnvVars(boolean caseSensitive) throws IOException {
        Properties envVars = new Properties();
        Map<String, String> envs = envVarSource.getEnvMap();
        for (String key : envs.keySet()) {
            String value = envs.get(key);
            if (!caseSensitive) {
                key = key.toUpperCase(Locale.ENGLISH);
            }
            envVars.put(key, value);
        }
        return envVars;
    }

    /**
     * Set the source object to load the environment variables from.
     * Default implementation should suffice. This is mostly for testing.
     * @param source the EnvVarSource instance that loads the environment variables.
     *
     * @since 3.1.2
     */
    public static void setEnvVarSource(EnvVarSource source) {
        envVarSource = source;
    }

    /**
     * Defines the functionality to load a Map of environment variables.
     *
     * @since 3.1.2
     */
    public interface EnvVarSource {
        public Map<String, String> getEnvMap();
    }

    /**
     * Default implementation to load environment variables.
     *
     * @since 3.1.2
     */
    public static class DefaultEnvVarSource implements EnvVarSource {

        @Override
        public Map<String, String> getEnvMap() {
            return System.getenv();
        }
    }
}
