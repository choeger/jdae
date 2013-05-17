/**
 * 
 */
package de.tuberlin.uebb.jdae.builtins;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;

import de.tuberlin.uebb.jdae.dae.ADEquation;
import de.tuberlin.uebb.jdae.dae.FunctionalEquation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;

/**
 * @author choeger
 * 
 */
public final class StateEquation extends ADEquation {

    final int state;
    final SolvableDAE system;

    /*
     * (nicht-Javadoc)
     * 
     * @see org.apache.commons.math3.analysis.UnivariateFunction#value(double)
     */
    @Override
    public double compute(double t) {
        return system.stateVector[state];
    }

    @Override
    public double value(double t) {
        return system.stateVector[state];
    }

    @Override
    public DerivativeStructure value(DerivativeStructure t) {
        return compute(t);
    }

    public StateEquation(int state, SolvableDAE system) {
        super(1);
        this.state = state;
        this.system = system;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.dae.FunctionalEquation#unknown()
     */
    @Override
    public int unknown() {
        return state;
    }

    @Override
    public DerivativeStructure compute(DerivativeStructure time) {
        if (time.getOrder() <= 1) {
            final FunctionalEquation derEq = system.get(state
                    + system.dimension);
            if (derEq instanceof ADEquation) {
                return ((ADEquation) derEq).value(time);
            } else {
                return new DerivativeStructure(1, 1, system.stateVector[state],
                        derEq.value(time.getValue()));
            }
        }
        throw new UnsupportedOperationException(
                "Cannot compute derivative of order " + time.getOrder()
                        + " of a state variable!");
    }

}
