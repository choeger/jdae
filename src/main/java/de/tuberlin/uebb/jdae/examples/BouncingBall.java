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

import org.apache.commons.math3.ode.events.EventHandler.Action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.tuberlin.uebb.jdae.dae.LoadableModel;
import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.hlmsl.specials.ConstantEquation;
import de.tuberlin.uebb.jdae.hlmsl.specials.ConstantLinear;
import de.tuberlin.uebb.jdae.hlmsl.specials.Equality;
import de.tuberlin.uebb.jdae.llmsl.ContinuousEvent;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
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

    public final Unknown h;

    final Unknown v;

    final Unknown b;

    final Unknown dh;

    final Unknown dv;

    final Equation freeFall;
    final Equation bottom;
    final Equation accel;

    public BouncingBall(SimulationRuntime runtime) {
        super();
        this.runtime = runtime;
        this.h = runtime.newUnknown("h");
        this.v = runtime.newUnknown("v");
        this.b = runtime.newUnknown("b");
        this.dh = h.der();
        this.dv = v.der();

        this.bottom = new ConstantLinear(0.0, -0.5, new double[] { 1, -1 },
                ImmutableList.of(b, h));
        this.freeFall = new ConstantEquation(dv, -9.81);
        this.accel = new Equality(v, dh);
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see de.tuberlin.uebb.jdae.dae.LoadableModel#initials()
     */
    @Override
    public Map<GlobalVariable, Double> initials(
            Map<Unknown, GlobalVariable> ctxt) {
        return ImmutableMap.of(ctxt.get(h), 10.0);
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

    public final class BounceEvent extends ContinuousEvent {

        final GlobalVariable h, v;

        public BounceEvent(GlobalVariable h, GlobalVariable v) {
            super();
            this.h = h;
            this.v = v;
        }

        @Override
        public Collection<GlobalVariable> vars() {
            return ImmutableList.of(this.h);
        }

        @Override
        public Action handleEvent(boolean increasing, ExecutableDAE dae) {
            if (!increasing) {
                events++;

                /* the bounce effect */
                dae.set(v, dae.load(v) * -0.8);
                return Action.STOP;
            }
            return Action.CONTINUE;
        }

        @Override
        public double f(ExecutableDAE dae) {
            final double load = dae.load(this.h);
            return load;
        }

    }

    @Override
    public Collection<ContinuousEvent> events(Map<Unknown, GlobalVariable> ctxt) {
        ContinuousEvent bounce = new BounceEvent(ctxt.get(h), ctxt.get(v));
        return ImmutableList.of(bounce);
    }

}
