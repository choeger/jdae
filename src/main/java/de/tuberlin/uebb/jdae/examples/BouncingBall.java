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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.tuberlin.uebb.jdae.builtins.ConstantLinearEquation;
import de.tuberlin.uebb.jdae.builtins.DefaultConstantEquation;
import de.tuberlin.uebb.jdae.builtins.EqualityEquation;
import de.tuberlin.uebb.jdae.builtins.SimpleVar;
import de.tuberlin.uebb.jdae.dae.ContinuousEvent;
import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.FunctionalEquation;
import de.tuberlin.uebb.jdae.dae.LoadableModel;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

/**
 * The "Hello World"-DAE translated into jdae.
 * 
 * @author Christoph Höger
 * 
 */
public class BouncingBall implements LoadableModel {

    public int events = 0;

    final SimulationRuntime runtime;

    final Unknown h = new SimpleVar("h");
    final Unknown v = new SimpleVar("v");
    final Unknown b = new SimpleVar("b");

    final Unknown dh, dv;

    final Equation freeFall;
    final Equation bottom;
    final Equation accel;

    public BouncingBall(SimulationRuntime runtime) {
        super();
        this.runtime = runtime;
        this.dh = runtime.der().apply(h);
        this.dv = runtime.der().apply(v);

        this.bottom = ConstantLinearEquation.builder().add(b, 1).add(h, -1)
                .addConstant(-0.5).build();

        this.freeFall = new DefaultConstantEquation(dv, -9.81);
        this.accel = new EqualityEquation(v, dh);
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.dae.LoadableModel#initials()
     */
    @Override
    public Map<String, Double> initials() {
        return ImmutableMap.of("h", 10.0);
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.dae.LoadableModel#equations()
     */
    @Override
    public Collection<Equation> equations() {
        return ImmutableList.of(freeFall, accel, bottom);
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.dae.LoadableModel#name()
     */
    @Override
    public String name() {
        return "Bouncing Ball";
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.dae.LoadableModel#events(de.tuberlin.uebb.jdae.
     * dae.SolvableDAE)
     */
    @Override
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
