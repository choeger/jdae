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

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.ode.events.EventHandler.Action;
import org.apache.commons.math3.util.FastMath;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.tuberlin.uebb.jdae.dae.LoadableModel;
import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.llmsl.BlockEquation;
import de.tuberlin.uebb.jdae.llmsl.BlockVariable;
import de.tuberlin.uebb.jdae.llmsl.ContinuousEvent;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.ExecutionContext;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.llmsl.IBlock;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

public final class StiffHybrid implements LoadableModel {

    public final Unknown x1;
    public final Unknown x2;
    final Unknown dx1;
    final SimulationRuntime runtime;

    public StiffHybrid(SimulationRuntime runtime) {
        super();
        this.runtime = runtime;
        x1 = runtime.newUnknown("x1");
        x2 = runtime.newUnknown("x2");
        dx1 = x1.der();
    }

    int delta = 1;
    public int events = 0;

    final class Equation1 implements Equation {

        @Override
        public Collection<Unknown> unknowns() {
            return ImmutableList.of(dx1);
        }

        @Override
        public GlobalEquation bind(final Map<Unknown, GlobalVariable> ctxt) {
            return new GlobalEquation() {

                final GlobalVariable gdx1 = ctxt.get(dx1);

                @Override
                public boolean canSpecializeFor(GlobalVariable v) {
                    return v.equals(gdx1);
                }

                @Override
                public IBlock specializeFor(final GlobalVariable v,
                        final ExecutableDAE dae) {
                    if (v.equals(gdx1))
                        return new IBlock() {

                            @Override
                            public void exec() {
                                dae.data[gdx1.index][gdx1.der] = FastMath
                                        .sin(10 * dae.time());
                            }

                            @Override
                            public Iterable<GlobalVariable> variables() {
                                return ImmutableList.of(gdx1);
                            }

                        };
                    throw new IllegalArgumentException("Cannot specialize for "
                            + v);
                }

                @Override
                public List<GlobalVariable> need() {
                    return ImmutableList.of(gdx1);
                }

                @Override
                public BlockEquation bind(
                        final Map<GlobalVariable, BlockVariable> blockCtxt) {
                    final BlockVariable bdx1 = blockCtxt.get(gdx1);
                    return new BlockEquation() {

                        @Override
                        public DerivativeStructure exec(ExecutionContext m) {
                            return bdx1.load(m).subtract(
                                    m.time().multiply(10).sin());
                        }
                    };
                }
            };
        }

    }

    final class Equation2 implements Equation {

        @Override
        public Collection<Unknown> unknowns() {
            return ImmutableList.of(x1, x2);
        }

        @Override
        public GlobalEquation bind(final Map<Unknown, GlobalVariable> ctxt) {
            final GlobalVariable gx1 = ctxt.get(x1), gx2 = ctxt.get(x2);
            return new GlobalEquation() {

                @Override
                public List<GlobalVariable> need() {
                    return ImmutableList.of(gx1, gx2);
                }

                @Override
                public boolean canSpecializeFor(GlobalVariable v) {
                    return !gx1.equals(gx2) && (v.equals(gx1) || v.equals(gx2));
                }

                @Override
                public IBlock specializeFor(GlobalVariable v,
                        final ExecutableDAE dae) {
                    if (v.equals(gx1)) {
                        return new IBlock() {

                            @Override
                            public void exec() {
                                dae.data[gx1.index][gx1.der] = dae.data[gx2.index][gx2.der]
                                        - delta;
                            }

                            @Override
                            public Iterable<GlobalVariable> variables() {
                                return ImmutableList.of(gx1);
                            }
                        };
                    } else if (v.equals(gx2)) {
                        return new IBlock() {

                            @Override
                            public void exec() {
                                dae.data[gx2.index][gx2.der] = dae.data[gx1.index][gx1.der]
                                        + delta;
                            }

                            @Override
                            public Iterable<GlobalVariable> variables() {
                                return ImmutableList.of(gx2);
                            }
                        };
                    } else {
                        throw new IllegalArgumentException(
                                "Cannot specialize for " + v);
                    }
                }

                @Override
                public BlockEquation bind(
                        final Map<GlobalVariable, BlockVariable> blockCtxt) {
                    final BlockVariable bx1 = blockCtxt.get(gx1), bx2 = blockCtxt
                            .get(gx2);
                    return new BlockEquation() {

                        @Override
                        public DerivativeStructure exec(ExecutionContext m) {
                            return bx2.load(m).subtract(bx1.load(m).add(delta));
                        }
                    };
                }
            };
        }

    }

    public final class Event1 extends ContinuousEvent {

        final GlobalVariable x1;

        public Event1(GlobalVariable x1) {
            super();
            this.x1 = x1;
        }

        @Override
        public Collection<GlobalVariable> vars() {
            return ImmutableList.of(x1);
        }

        @Override
        public Action handleEvent(boolean increasing, ExecutableDAE dae) {
            if (!increasing) {
                events++;
                delta = delta == 1 ? 0 : 1;
                return Action.STOP;
            }
            return Action.CONTINUE;
        }

        @Override
        public double f(ExecutableDAE dae) {
            final double load = dae.load(x1);
            return load - 0.1;
        }

    }

    public ImmutableList<Equation> equations() {
        return ImmutableList.of(new Equation1(), new Equation2());
    }

    @Override
    public Map<GlobalVariable, Double> initials(
            Map<Unknown, GlobalVariable> ctxt) {
        return ImmutableMap.of(ctxt.get(x1), 1.0, ctxt.get(x2), 0.0);
    }

    @Override
    public String name() {
        return "Stiff Hybrid System";
    }

    @Override
    public Collection<ContinuousEvent> events(
            final Map<Unknown, GlobalVariable> ctxt) {
        final ContinuousEvent e = new Event1(ctxt.get(x1));
        return ImmutableList.of(e);
    }
}
