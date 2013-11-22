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
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.tuberlin.uebb.jdae.dae.LoadableModel;
import de.tuberlin.uebb.jdae.diff.total.TDNumber;
import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.hlmsl.specials.Equality;
import de.tuberlin.uebb.jdae.llmsl.BlockEquation;
import de.tuberlin.uebb.jdae.llmsl.BlockVariable;
import de.tuberlin.uebb.jdae.llmsl.ExecutionContext;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.llmsl.events.ContinuousEvent;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

public final class SimpleHigherIndexExample implements LoadableModel {

    public final Unknown x, y, dx, dy;
    public final Equation eq1, eq2;

    public SimpleHigherIndexExample(final SimulationRuntime runtime) {
        super();
        this.x = runtime.newUnknown("x");
        this.y = runtime.newUnknown("y");
        this.dx = x.der();
        this.dy = y.der();

        eq1 = new Equality(dx, dy);
        eq2 = new Equation() {

            @Override
            public Collection<Unknown> unknowns() {
                return ImmutableList.of(x, y);
            }

            @Override
            public GlobalEquation bind(final Map<Unknown, GlobalVariable> ctxt) {

                return new GlobalEquation() {

                    final GlobalVariable gx = ctxt.get(x), gy = ctxt.get(y);

                    @Override
                    public List<GlobalVariable> need() {
                        return ImmutableList.of(gx, gy);
                    }

                    public String toString() {
                        return String.format("%s + %s = time", gx, gy);
                    }

                    @Override
                    public BlockEquation bind(
                            final Map<GlobalVariable, BlockVariable> blockCtxt) {
                        return new BlockEquation() {
                            final BlockVariable bx = blockCtxt.get(gx),
                                    by = blockCtxt.get(gy);

                            @Override
                            public TDNumber exec(ExecutionContext m) {
                                final TDNumber load1 = bx.load(m);
                                final TDNumber load2 = by.load(m);
                                final TDNumber time = m.time();
                                return load1.add(load2).subtract(time);
                            }
                        };
                    }
                };
            }

        };
    }

    @Override
    public Map<GlobalVariable, Double> initials(
            Map<Unknown, GlobalVariable> ctxt) {
        return ImmutableMap.of(ctxt.get(x), 0.0, ctxt.get(y), 0.0);
    }

    @Override
    public Collection<Equation> equations() {
        return ImmutableList.of(eq1, eq2);
    }

    @Override
    public String name() {
        return "SimpleIndex2";
    }

    @Override
    public Collection<ContinuousEvent> events(Map<Unknown, GlobalVariable> ctxt) {
        return ImmutableList.of();
    }

    @Override
    public List<GlobalEquation> initialEquations(
            Map<Unknown, GlobalVariable> ctxt) {
        // TODO Automatisch generierter Methodenstub
        return null;
    }

}
