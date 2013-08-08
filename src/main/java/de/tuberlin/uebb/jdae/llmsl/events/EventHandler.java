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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;

public final class EventHandler {

    private final ContinuousEvent[] c_events;
    private final double[][] guards;
    private int current = 0;

    public EventHandler(ContinuousEvent[] c_events) {
        super();
        this.c_events = c_events;
        this.guards = new double[][] { new double[c_events.length], null };
    }

    public Collection<EventEffect> calculateEffects(ExecutableDAE ctxt) {
        final List<EventEffect> effects = Lists.newArrayList();
        final int store = (current + 1) % 2;

        if (guards[store] == null) {
            guards[store] = new double[c_events.length];
            for (int i = 0; i < c_events.length; i++) {
                final ContinuousEvent cev = c_events[i];
                guards[store][i] = cev.guard.exec(ctxt.execCtxt).der(0);
            }
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

    public void acceptLastStep() {
        this.current = (current + 1) % 2;
    }
}
