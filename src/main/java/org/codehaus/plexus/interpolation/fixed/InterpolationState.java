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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.interpolation.RecursionInterceptor;
import org.codehaus.plexus.interpolation.SimpleRecursionInterceptor;

/**
 * AN error collector contains the errors accumulated during an interpolation.
 * It is stateful.
 */
public class InterpolationState {
    private final List<String> messages = new ArrayList<String>();
    private final List<Throwable> causes = new ArrayList<Throwable>();

    public void addFeedback(String message, Throwable cause) {
        messages.add(message);
        causes.add(cause);
    }

    public List asList() {
        ArrayList<Object> items = new ArrayList<Object>();
        for (int i = 0; i < messages.size(); i++) {
            String msg = messages.get(i);
            if (msg != null) items.add(msg);
            Throwable cause = causes.get(i);
            if (cause != null) items.add(cause);
        }
        return items.size() > 0 ? items : null;
    }

    public void clear() {
        messages.clear();
        causes.clear();
        unresolvable.clear();
        recursionInterceptor.clear();
        root = null;
    }

    final Set<String> unresolvable = new HashSet<String>();
    RecursionInterceptor recursionInterceptor = new SimpleRecursionInterceptor();

    public void setRecursionInterceptor(RecursionInterceptor recursionInterceptor) {
        this.recursionInterceptor = recursionInterceptor;
    }

    FixedStringSearchInterpolator root = null;
}
