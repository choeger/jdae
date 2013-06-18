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
package de.tuberlin.uebb.jdae.llmsl;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.events.EventHandler.Action;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

public abstract class ContinuousEvent {

    public abstract Collection<GlobalVariable> vars();

    public abstract Action handleEvent(boolean increasing, ExecutableDAE dae);

    public abstract double f(ExecutableDAE dae);

    public int lastBlock(final ExecutableDAE dae,
            final Collection<GlobalVariable> vars) {
        final Set<GlobalVariable> left = Sets.newTreeSet(vars);
        left.removeAll(dae.states);

        int i = 0;
        while (!left.isEmpty() && i < dae.blocks.length) {
            for (GlobalVariable gv : dae.blocks[i].variables())
                left.remove(gv);
            i++;
        }

        if (!left.isEmpty())
            throw new RuntimeException(
                    "The following variables are not computed: "
                            + left.toString());
        return i;
    }

    public static Function<ContinuousEvent, EventHandler> instantiation(
            final ExecutableDAE dae) {
        return new Function<ContinuousEvent, EventHandler>() {
            public EventHandler apply(ContinuousEvent ev) {
                return ev.instantiate(dae);
            }
        };
    }

    public EventHandler instantiate(final ExecutableDAE dae) {
        final int block = lastBlock(dae, vars());

        return new EventHandler() {

            private double last = Double.NaN;

            @Override
            public void init(double t0, double[] y0, double t) {
            }

            @Override
            public double g(double t, double[] y) {
                if (last == t)
                    return 0.0;

                dae.computeDerivativesUpTo(block, t, y);
                return f(dae);
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                dae.computeDerivativesUpTo(block, t, y);
                last = t;
                return handleEvent(increasing, dae);
            }

            @Override
            public void resetState(double t, double[] y) {
            }

        };
    }
}
