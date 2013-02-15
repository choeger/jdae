package de.tuberlin.uebb.jdae.simulation;

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;

import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;

public interface SimulationRuntime {

    /**
     * Create a solvable dae instance from a list of equations.
     * 
     * @param equations
     *            the equations that form the model. Note: Any derivatives need
     *            to be collected with {@code derivative_collector}!
     * @return a solvable dae
     */
    public abstract SolvableDAE causalise(List<Equation> equations);

    public Function<Unknown, Unknown> der();

    public abstract void simulateFixedStep(SolvableDAE dae,
            Map<String, Double> inits, double stop_time, int steps);

    public abstract void simulateVariableStep(SolvableDAE dae,
            Map<String, Double> inits, double stop_time, double minStep,
            double maxStep, double absoluteTolerance, double relativeTolerance);

}