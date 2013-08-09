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

public final class EventEvaluator {

    private EventResult result;

    private final EventHandler[] evHdlr;
    private final ExecutableDAE ctxt;
    private final ContinuousEvent[] c_events;
    private final double[][] guards;
    private int current = 0;
    private final double lastAttempt[] = {Double.MIN_VALUE};
    private final int blockStart[] = {0};

    public EventEvaluator(final ExecutableDAE ctxt, ContinuousEvent[] c_events) {
        super();
        this.c_events = c_events;
	this.evHdlr = new EventHandler[c_events.length];
        this.ctxt = ctxt;

        for (int i = 0; i < evHdlr.length; i++)
            evHdlr[i] = new CommonsMathEventHandler(i);

        this.guards = new double[][] { new double[c_events.length], null };
    }

    public EventHandler[] getEventHandlers() {
	return evHdlr;
    }

    /**
     * Collect all events in the current interval. 
     * No refinement will be done.
     */
    public Collection<EventEffect> calculateEffects() {
        final List<EventEffect> effects = Lists.newArrayList();
        final int store = (current + 1) % 2;

        if (guards[store] == null) {
            calculateStep();
            return ImmutableList.of();
        }

        for (int i = 0; i < c_events.length; i++) {
	    final ContinuousEvent cev = c_events[i];
	    guards[store][i] = cev.guard.exec(ctxt.execCtxt).der(0);
	    if (isCandidate(i))
		effects.add(cev.effect);
        }

        return effects;
    }

    private boolean isCandidate(final int i) {
	final int store = (current + 1) % 2;
	final ContinuousEvent cev = c_events[i];
	final double os = Math.signum(guards[current][i]);
	final double ns = Math.signum(guards[store][i]);

	if (os != ns) {
	    switch (cev.direction) {
	    case UP:
		return ns > 0;
	    case DOWN:
                return ns < 0;
	    case BOTH:
		return true;
            }
	} 
	return false;
    }

    private void calculateStep() {
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
	calculateStep();
        this.current = (current + 1) % 2;
    }

    private final class CommonsMathEventHandler implements EventHandler {
	
	final int blockNumber;
	final ContinuousEvent cev;

	public CommonsMathEventHandler(int e) {
	    this.cev = c_events[e];
	    this.blockNumber = ctxt.lastBlock(cev.guardGlobal.need());
	}

	@Override
	public void init(double t0, double[] y0, double t) { }

	@Override
	public double g(double t, double[] y) {
	    if (result != null && result.t == t)
		return 0.0;
	    
	    if (t != lastAttempt[0]) {
		ctxt.setState(t, y);
		blockStart[0] = 0;
		lastAttempt[0] = t;
	    }

	    ctxt.computeDerivatives(blockStart[0], blockNumber);
	    blockStart[0] = Math.max(blockStart[0], blockNumber);

	    final double p = cev.guard.exec(ctxt.execCtxt).der(0);
	    System.out.println("t: " + t + " g: " + p);
	    return p;	
	}

	@Override
	public Action eventOccurred(double t, double[] y, boolean increasing) {
	    System.out.println("Event at t: " + t);
 
	    if (increasing && cev.direction != EventDirection.DOWN) {
		result = new EventResult(t, y, cev.effect);
		return Action.STOP;
	    } else if (!increasing && cev.direction != EventDirection.UP) {
		result = new EventResult(t, y, cev.effect);
		return Action.STOP;
	    }

	    return Action.CONTINUE;
	}

	@Override
	public void resetState(double t, double[] y) { }
    }
}
