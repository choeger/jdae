package de.tuberlin.uebb.jdae.examples;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.ode.events.EventHandler;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.tuberlin.uebb.jdae.builtins.EqualityEquation;
import de.tuberlin.uebb.jdae.builtins.SimpleVar;
import de.tuberlin.uebb.jdae.dae.ADEquation;
import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.FunctionalEquation;
import de.tuberlin.uebb.jdae.dae.LoadableModel;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

public final class SimpleHigherIndexExample implements LoadableModel {

    final class DiffEquation extends FunctionalEquation {
        int solveFor;
        FunctionalEquation other;

        public DiffEquation(int solveFor, FunctionalEquation other) {
            super();
            this.solveFor = solveFor;
            this.other = other;
        }

        @Override
        public int unknown() {
            return solveFor;
        }

        @Override
        public double compute(double time) {
            return time - other.value(time);
        }
    }

    final class ADDiffEquation extends ADEquation {

        public ADDiffEquation(int target, int order, ADEquation other) {
            super(order);
            this.other = other;
            this.solveFor = target;
        }

        int solveFor;
        ADEquation other;

        @Override
        public int unknown() {
            return solveFor;
        }

        @Override
        public DerivativeStructure compute(DerivativeStructure time) {
            final DerivativeStructure result = time.subtract(other.value(time));
            return result;
        }

    }

    public final Unknown x, y, dx, dy;
    public final Equation eq1, eq2;

    public SimpleHigherIndexExample(final SimulationRuntime runtime) {
        super();
        this.x = new SimpleVar("x");
        this.y = new SimpleVar("y");
        this.dx = runtime.der().apply(x);
        this.dy = runtime.der().apply(y);

        eq1 = new EqualityEquation(dx, dy);
        eq2 = new Equation() {

            @Override
            public FunctionalEquation specializeFor(Unknown unknown,
                    SolvableDAE system) {
                if (unknown == x) {
                    return new DiffEquation(system.variables.get(x),
                            system.get(y));
                } else if (unknown == y) {
                    return new DiffEquation(system.variables.get(y),
                            system.get(x));
                } else
                    throw new IllegalArgumentException();
            }

            @Override
            public Collection<Unknown> canSolveFor(
                    Function<Unknown, Unknown> der) {
                return ImmutableList.of(x, y);
            }

            @Override
            public UnivariateFunction residual(SolvableDAE system) {
                return null;
            }

            @Override
            public FunctionalEquation specializeFor(Unknown unknown,
                    SolvableDAE system, int der_index) {
                if (der_index == 0)
                    return specializeFor(unknown, system);
                else {
                    if (unknown == x) {
                        return new ADDiffEquation(system.variables.get(x),
                                der_index, (ADEquation) system.get(y));
                    } else if (unknown == y) {
                        return new ADDiffEquation(system.variables.get(y),
                                der_index, (ADEquation) system.get(x));
                    } else
                        throw new IllegalArgumentException();
                }
            }

        };
    }

    @Override
    public Map<String, Double> initials() {
        return ImmutableMap.of("x", 0.0, "y", 0.0);
    }

    @Override
    public Collection<Equation> equations() {
        return ImmutableList.of(eq1, eq2);
    }

    @Override
    public String name() {
        return "SimpleIndex2";
    }

    @Override
    public Collection<EventHandler> events(SolvableDAE ctxt) {
        return ImmutableList.of();
    }

}
