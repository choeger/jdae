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

package de.tuberlin.uebb.jdae.llmsl.events;

import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.ode.events.EventHandler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.events.ContinuousEvent.EventDirection;

public final class EventEvaluator implements EventHandler {

    private EventResult result;

    private final ExecutableDAE ctxt;
    private final ContinuousEvent[] c_events;
    private final double[][] guards;
    private int current = 0;

    public EventEvaluator(final ExecutableDAE ctxt, ContinuousEvent[] c_events) {
        super();
        this.c_events = c_events;
        this.ctxt = ctxt;
        this.guards = new double[][] { new double[c_events.length], null };
    }

    public Collection<EventEffect> calculateEffects() {
        final List<EventEffect> effects = Lists.newArrayList();
        final int store = (current + 1) % 2;

        if (guards[store] == null) {
            calculateFirstStep();
            return ImmutableList.of();
        }

        for (int i = 0; i < c_events.length; i++) {
            final ContinuousEvent cev = c_events[i];
            guards[store][i] = cev.guard.exec(ctxt.execCtxt).der(0);
            final double os = Math.signum(guards[current][i]);
            final double ns = Math.signum(guards[store][i]);

            if (os != ns) {
                switch (cev.direction) {
                case UP:
                    if (ns > 0)
                        effects.add(cev.effect);
                    break;
                case DOWN:
                    if (ns < 0)
                        effects.add(cev.effect);
                    break;
                case BOTH:
                    effects.add(cev.effect);
                }
            }
        }

        return effects;
    }

    private void calculateFirstStep() {
        final int store = (current + 1) % 2;
        guards[store] = new double[c_events.length];
        for (int i = 0; i < c_events.length; i++) {
            final ContinuousEvent cev = c_events[i];
            guards[store][i] = cev.guard.exec(ctxt.execCtxt).der(0);
        }
    }

    public EventResult getResult() {
        return result;
    }

    public void acceptLastStep() {
        this.current = (current + 1) % 2;
    }

    @Override
    public void init(double t0, double[] y0, double t) {
        final int store = (current + 1) % 2;
        if (guards[store] == null) {
            calculateFirstStep();
            acceptLastStep();
        }
    }

    @Override
    public double g(double t, double[] y) {
        if (result != null && result.t == t)
            return 0.0;

        final int store = (current + 1) % 2;
        final int cand = getCandidate(t, y, store);
        if (cand == -1)
            return 0;

        System.out.println("t: " + t + " g: " + guards[store][cand]);
        return guards[store][cand];
    }

    public int getCandidate(double t, double[] y, final int store) {
        double g = Double.MAX_VALUE;
        int cand = -1;

        ctxt.computeDerivatives(t, y);

        for (int i = 0; i < c_events.length; i++) {
            final ContinuousEvent cev = c_events[i];
            guards[store][i] = cev.guard.exec(ctxt.execCtxt).der(0);

            final double p = guards[store][i] * guards[store][i];
            if (g > p) {
                g = p;
                cand = i;
            }

        }
        return cand;
    }

    @Override
    public Action eventOccurred(double t, double[] y, boolean increasing) {
        System.out.println("Event at t: " + t);
        final int store = (current + 1) % 2;
        final int cand = getCandidate(t, y, store);

        result = null;
        guards[store][cand] = 0;

        if (increasing && c_events[cand].direction != EventDirection.DOWN) {
            result = new EventResult(t, y, c_events[cand].effect);
            return Action.STOP;
        } else if (!increasing && c_events[cand].direction != EventDirection.UP) {
            result = new EventResult(t, y, c_events[cand].effect);
            return Action.STOP;
        }

        return Action.CONTINUE;
    }

    @Override
    public void resetState(double t, double[] y) {
    }
}
