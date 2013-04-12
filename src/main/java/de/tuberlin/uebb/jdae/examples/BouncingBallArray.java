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
import com.google.common.collect.Iterables;

import de.tuberlin.uebb.jdae.dae.Equation;
import de.tuberlin.uebb.jdae.dae.LoadableModel;
import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

public class BouncingBallArray implements LoadableModel {

    public final BouncingBallRadius[] balls = { null, null, null };

    public BouncingBallArray(SimulationRuntime runtime) {
        for (int i = 1; i <= 3; ++i)
            balls[i - 1] = new BouncingBallRadius(i, runtime, this);
    }

    @Override
    public Map<String, Double> initials() {
        return ImmutableMap.of(balls[0].h.toString(), 5.0,
                balls[1].h.toString(), 10.0, balls[2].h.toString(), 20.0);
    }

    @Override
    public Collection<Equation> equations() {
        return ImmutableList.copyOf(Iterables.concat(balls[0].equations(),
                balls[1].equations(), balls[2].equations()));
    }

    @Override
    public String name() {
        return "Array of bouncing balls";
    }

    @Override
    public Collection<EventHandler> events(SolvableDAE ctxt) {
        return ImmutableList.copyOf(Iterables.concat(balls[0].events(ctxt),
                balls[1].events(ctxt), balls[2].events(ctxt)));
    }

}
