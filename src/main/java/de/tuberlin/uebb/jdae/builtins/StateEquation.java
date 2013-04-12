/**
 * 
 */
package de.tuberlin.uebb.jdae.builtins;

import de.tuberlin.uebb.jdae.dae.FunctionalEquation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;

/**
 * @author choeger
 * 
 */
public final class StateEquation extends FunctionalEquation {

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

    public StateEquation(int state, SolvableDAE system) {
        super();
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

}
