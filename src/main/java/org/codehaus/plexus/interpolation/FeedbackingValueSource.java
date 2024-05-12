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

/**
 * Adds feedback on any {@link #getValue(String) getValue(String)} call.
 * <p>One of the obvious usages is to add FeedbackingValueSource as last value source to {@link Interpolator}
 * to add feedback messages indicating not resolved expressions.</p>
 */
public class FeedbackingValueSource extends AbstractValueSource {
    private final String messagePattern;

    public FeedbackingValueSource() {
        this("'${expression}' not resolved");
    }

    /**
     * @param messagePattern could contain <code>${expression}</code> placeholder
     */
    public FeedbackingValueSource(String messagePattern) {
        super(true);
        this.messagePattern = messagePattern;
    }

    @Override
    public Object getValue(String expression) {
        addFeedback(messagePattern.replace("${expression}", expression));
        return null;
    }
}
