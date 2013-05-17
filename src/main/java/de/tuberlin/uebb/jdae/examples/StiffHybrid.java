/*
 * Copyright (C) 2012 uebb.tu-berlin.de.
 *
 * This file is part of modim
 *
 * modim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * modim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with modim. If not, see <http://www.gnu.org/licenses/>.
 */
package de.tuberlin.uebb.jdae.examples;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.ode.events.EventHandler;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.tuberlin.uebb.jdae.builtins.SimpleVar;
import de.tuberlin.uebb.jdae.dae.ContinuousEvent;
import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.FunctionalEquation;
import de.tuberlin.uebb.jdae.dae.LoadableModel;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

public final class StiffHybrid implements LoadableModel {

    final SimulationRuntime runtime;

    public StiffHybrid(SimulationRuntime runtime) {
        super();
        this.runtime = runtime;

        dx1 = runtime.der().apply(x1);
    }

    final Unknown x1 = new SimpleVar("x1");
    final Unknown x2 = new SimpleVar("x2");
    final Unknown dx1;

    int delta = 1;
    public int events = 0;

    final class Equation1 implements Equation {

        @Override
        public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
            return ImmutableList.of(dx1);
        }

        @Override
        public FunctionalEquation specializeFor(Unknown unknown,
                SolvableDAE system) {
            if (unknown == dx1) {
                final int i = system.variables.get(dx1);
                return new FunctionalEquation() {

                    @Override
                    public int unknown() {
                        return i;
                    }

                    @Override
                    public double compute(double time) {
                        return Math.sin(time * 10.0);
                    }
                };
            }
            throw new IllegalArgumentException("Cannot solve for " + unknown);
        }

        @Override
        public UnivariateFunction residual(SolvableDAE system) {
            // should not be needed
            return null;
        }

        @Override
        public FunctionalEquation specializeFor(Unknown unknown,
                SolvableDAE system, int der_index) {
            // should not be needed
            return null;
        }

    }

    final class Equation2 implements Equation {

        @Override
        public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
            return ImmutableList.of(x1, x2);
        }

        @Override
        public FunctionalEquation specializeFor(Unknown unknown,
                SolvableDAE system) {
            if (unknown == x2) {
                final int i = system.variables.get(x2);
                final FunctionalEquation f_x1 = system.get(system.variables
                        .get(x1));
                return new FunctionalEquation() {

                    @Override
                    public int unknown() {
                        return i;
                    }

                    @Override
                    public double compute(double time) {
                        return f_x1.value(time) + delta;
                    }
                };
            } else {
                throw new IllegalArgumentException(
                        "This system should have been completely causalised!"
                                + unknown);
            }
        }

        @Override
        public UnivariateFunction residual(SolvableDAE system) {
            // not needed
            return null;
        }

        @Override
        public FunctionalEquation specializeFor(Unknown unknown,
                SolvableDAE system, int der_index) {
            // should not be needed
            return null;
        }

    }

    public final class Event1 extends ContinuousEvent {

        final int x1;
        final SolvableDAE system;

        public Event1(int x1, SolvableDAE dae) {
            super();
            this.x1 = x1;
            this.system = dae;
        }

        @Override
        public Action eventOccurred(double t, double[] y, boolean increasing) {
            // System.err.println("Event hit t=" + t + " x1=" + y[x1] + " inc="
            // + increasing);
            if (increasing) {
                events++;
                delta = delta == 1 ? 0 : 1;
            }
            return Action.CONTINUE;
        }

        @Override
        public double g(double t, double[] y) {
            final double[] derivatives = new double[y.length];
            system.computeDerivatives(t, y, derivatives);
            return y[x1] - 1.1;
        }
    }

    public ImmutableList<Equation> equations() {
        return ImmutableList.of(new Equation1(), new Equation2());
    }

    @Override
    public Map<String, Double> initials() {
        return ImmutableMap.of("x1", 1.0, "x2", 0.0);
    }

    @Override
    public String name() {
        return "Stiff Hybrid System";
    }

    @Override
    public Collection<EventHandler> events(SolvableDAE dae) {
        return ImmutableList.<EventHandler> of(new Event1(
                dae.variables.get(x1), dae));
    }
}
