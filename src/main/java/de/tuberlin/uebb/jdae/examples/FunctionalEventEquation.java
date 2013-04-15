package de.tuberlin.uebb.jdae.examples;

import de.tuberlin.uebb.jdae.dae.FunctionalEquation;

public abstract class FunctionalEventEquation extends FunctionalEquation {

    @Override
    public double value(double time) {
        if (time == lastTime)
            return lastValue;

        return compute(time);
    }

    @Override
    public int unknown() {
        return -1; // there are no event variables
    }
}
