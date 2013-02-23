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

import org.apache.commons.math3.ode.events.EventHandler;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.tuberlin.uebb.jdae.builtins.SimpleVar;
import de.tuberlin.uebb.jdae.dae.ContinuousEvent;
import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.LoadableModel;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

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
        public Equation specialize(SolvableDAE system) {
            return this;
        }

        @Override
        public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
            return ImmutableList.of(dx1);
        }

        @Override
        public double solveFor(int unknown, Unknown v, SolvableDAE system) {
            if (v == dx1) {
                return Math.sin(system.currentTime * 10.0);
            }
            throw new IllegalArgumentException("Cannot solve for " + unknown);
        }

        @Override
        public double lhs(SolvableDAE systemState) {
            return systemState.apply(dx1);
        }

        @Override
        public double rhs(SolvableDAE systemState) {
            return Math.sin(systemState.currentTime * 10.0);
        }
    }

    final class Equation2 implements Equation {

        private int x1_index;

        @Override
        public Equation specialize(SolvableDAE system) {
            x1_index = system.variables.get(x1);
            return this;
        }

        @Override
        public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
            return ImmutableList.of(x1, x2);
        }

        @Override
        public double solveFor(int unknown, Unknown v, SolvableDAE system) {
            if (v == x2) {
                return system.get(x1_index) + delta;
            } else if (v == x1) {
                // actually in general this would be possible
                throw new IllegalArgumentException(
                        "This system should have been completely causalised!"
                                + unknown);
            }

            throw new IllegalArgumentException("Cannot solve for " + unknown);
        }

        @Override
        public double lhs(SolvableDAE systemState) {
            return systemState.apply(dx1);
        }

        @Override
        public double rhs(SolvableDAE systemState) {
            return Math.sin(systemState.currentTime * 10.0);
        }
    }

    public final class Event1 extends ContinuousEvent {

        final int x1;

        public Event1(int x1) {
            super();
            this.x1 = x1;
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
            return y[x1] - 1.1;
        }
    }

    @Test
    public void test() {
        final SolvableDAE dae = runtime.causalise(equations());

        runtime.simulateVariableStep(dae, events(dae), initials(), 10000,
                0.002, 0.01, 0.000001, 0.01);

        assertThat(events, is(15916));
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
        return ImmutableList
                .<EventHandler> of(new Event1(dae.variables.get(x1)));
    }
}
