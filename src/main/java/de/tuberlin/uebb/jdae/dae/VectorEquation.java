/**
 * 
 */
package de.tuberlin.uebb.jdae.dae;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.math3.analysis.UnivariateVectorFunction;

import com.google.common.base.Function;

/**
 * @author choeger
 * 
 */
public interface VectorEquation {

    public FunctionalVectorEquation specializeFor(final Set<Unknown> unknown,
            final SolvableDAE system);

    public Collection<Unknown> canSolveFor(final Function<Unknown, Unknown> der);

    public UnivariateVectorFunction residual(final SolvableDAE system);
}
