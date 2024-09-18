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

public abstract class AbstractDelegatingValueSource implements ValueSource {

    private final ValueSource delegate;

    protected AbstractDelegatingValueSource(ValueSource delegate) {
        if (delegate == null) {
            throw new NullPointerException("Delegate ValueSource cannot be null.");
        }

        this.delegate = delegate;
    }

    protected ValueSource getDelegate() {
        return delegate;
    }

    @Override
    public Object getValue(String expression, String delimiterStart, String delimiterEnd) {
        return getDelegate().getValue(expression, delimiterStart, delimiterEnd);
    }

    @Override
    public Object getValue(String expression) {
        return getDelegate().getValue(expression);
    }

    @Override
    public void clearFeedback() {
        delegate.clearFeedback();
    }

    @Override
    public List getFeedback() {
        return delegate.getFeedback();
    }
}
