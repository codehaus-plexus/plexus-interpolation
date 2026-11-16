package org.codehaus.plexus.interpolation;

import java.util.List;

import org.codehaus.plexus.interpolation.fixed.FixedStringSearchInterpolator;
import org.codehaus.plexus.interpolation.fixed.InterpolationState;

/**
 * A value source that allows a fixed interpolator to be injected into
 * a regular interpolator. This value source encapsulates state, so even though
 * the fixed interpolator can be used as a singleton, a single FixedInterpolatorValueSource
 * can only belong to one interpolator any given time.
 */
public class FixedInterpolatorValueSource implements ValueSource {

    private final FixedStringSearchInterpolator fixedStringSearchInterpolator;
    private final InterpolationState errorCollector = new InterpolationState();

    public FixedInterpolatorValueSource(FixedStringSearchInterpolator fixedStringSearchInterpolator) {
        this.fixedStringSearchInterpolator = fixedStringSearchInterpolator;
    }

    @Override
    public Object getValue(String expression) {
        return fixedStringSearchInterpolator.getValue(expression, errorCollector);
    }

    @Override
    public List getFeedback() {
        return errorCollector.asList();
    }

    @Override
    public void clearFeedback() {
        errorCollector.clear();
    }
}
