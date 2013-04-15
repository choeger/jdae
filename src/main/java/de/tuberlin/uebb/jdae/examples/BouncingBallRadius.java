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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.ode.events.EventHandler;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.tuberlin.uebb.jdae.builtins.DefaultConstantEquation;
import de.tuberlin.uebb.jdae.builtins.EqualityEquation;
import de.tuberlin.uebb.jdae.builtins.SimpleVar;
import de.tuberlin.uebb.jdae.dae.ContinuousEvent;
import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.FunctionalEquation;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

/**
 * The "Hello World"-DAE translated into jdae.
 * 
 * @author Christoph Höger
 * 
 */
public final class BouncingBallRadius {

    public int evaluations = 0;
    public int events = 0;

    final SimulationRuntime runtime;

    final double radius = 0.5;
    final int index;

    public final Unknown e;
    final Unknown h;
    final Unknown v;
    final Unknown b;
    final Unknown dh;
    final Unknown dv;

    final Equation evals, freeFall, accel, bottom;
    private final BouncingBallArray parent;

    public BouncingBallRadius(int idx, SimulationRuntime runtime,
            final BouncingBallArray parent) {
        super();
        this.index = idx;
        this.parent = parent;

        this.e = new SimpleVar(MessageFormat.format("ball[{0}].evals", idx));
        this.b = new SimpleVar(MessageFormat.format("ball[{0}].bottom", idx));
        this.h = new SimpleVar(MessageFormat.format("ball[{0}].h", idx));
        this.v = new SimpleVar(MessageFormat.format("ball[{0}].v", idx));

        this.runtime = runtime;
        this.dh = runtime.der().apply(h);
        this.dv = runtime.der().apply(v);

        this.freeFall = new DefaultConstantEquation(dv, -9.81);

        this.evals = new Equation() {

            @Override
            public Collection<Unknown> canSolveFor(
                    Function<Unknown, Unknown> der) {
                return ImmutableList.of(e, b);
            }

            @Override
            public FunctionalEquation specializeFor(Unknown unknown,
                    SolvableDAE system) {
                final int e_i = system.variables.get(e);
                if (unknown == e) {
                    return new FunctionalEquation() {

                        @Override
                        public int unknown() {
                            return e_i;
                        }

                        @Override
                        public double compute(double time) {
                            return evaluations;
                        }

                    };
                }

                throw new IllegalArgumentException("Cannot solve for "
                        + unknown);
            }

            @Override
            public UnivariateFunction residual(SolvableDAE system) {
                return null; // Should not be needed
            }

        };

        this.bottom = new Equation() {

            @Override
            public Collection<Unknown> canSolveFor(
                    Function<Unknown, Unknown> der) {
                ArrayList<Unknown> pseudodeps = Lists
                        .newArrayListWithCapacity(index);
                for (int i = 0; i < index; i++)
                    pseudodeps.add(parent.balls[i].b);

                return pseudodeps;
            }

            @Override
            public FunctionalEquation specializeFor(final Unknown unknown,
                    final SolvableDAE system) {
                final int bottom_i = system.variables.get(b);
                final int h_i = system.variables.get(h);

                if (unknown == b) {
                    return new FunctionalEquation() {

                        @Override
                        public int unknown() {
                            return bottom_i;
                        }

                        @Override
                        public double compute(double time) {
                            evaluations++;
                            return system.value(h_i, time) - 0.5;
                        }

                    };
                } else {
                    throw new IllegalArgumentException("Cannot solve for "
                            + unknown);
                }
            }

            @Override
            public UnivariateFunction residual(SolvableDAE system) {
                return null;
            }
        };
        this.accel = new EqualityEquation(v, dh);
    }

    public Collection<Equation> equations() {
        return ImmutableList.of(bottom, freeFall, accel, evals);
    }

    public Collection<EventHandler> events(SolvableDAE ctxt) {
        return ImmutableList.<EventHandler> of(new BounceEvent(ctxt));
    }

    public final class BounceEvent extends ContinuousEvent {

        final int v_i, h_i, b_i;
        final SolvableDAE ctxt;
        final FunctionalEquation event;

        public BounceEvent(SolvableDAE pCtxt) {
            this.ctxt = pCtxt;
            v_i = ctxt.variables.get(v);
            h_i = ctxt.variables.get(h);
            b_i = ctxt.variables.get(b);

            event = new FunctionalEventEquation() {

                @Override
                public double compute(double time) {
                    return ctxt.get(b_i).value(time);
                }

            };
        }

        @Override
        public Action eventOccurred(double t, double[] vars, boolean inc) {
            if (!inc) {
                System.out.println("Hit event @" + t);
                events++;

                /* the bounce effect */
                vars[v_i] = -0.8 * vars[v_i];
                ctxt.get(v_i).setValue(t, vars[v_i]);

                /* ensure promise */
                event.setValue(t, 0.0);

                ctxt.stop(t, vars);
                return Action.STOP;
            }
            return Action.CONTINUE;
        }

        @Override
        public double g(double t, double[] vars) {
            ctxt.stateVector = vars;
            return event.value(t);
        }

        @Override
        public void init(double t0, double[] vars, double t) {

        }

        @Override
        public void resetState(double time, double[] vars) {
            vars[v_i] = -0.8 * vars[v_i];
        }

    }

}
