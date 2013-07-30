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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.ode.events.EventHandler.Action;

import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.diff.total.TDNumber;
import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.hlmsl.specials.ConstantEquation;
import de.tuberlin.uebb.jdae.hlmsl.specials.Equality;
import de.tuberlin.uebb.jdae.llmsl.BlockEquation;
import de.tuberlin.uebb.jdae.llmsl.BlockVariable;
import de.tuberlin.uebb.jdae.llmsl.ContinuousEvent;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.ExecutionContext;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
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

    final int index;

    public final Unknown e, h, v, b, dh, dv;

    final Equation evals, freeFall, accel, bottom;

    public BouncingBallRadius(int idx, SimulationRuntime runtime) {
        super();
        this.index = idx;

        this.e = runtime.newUnknown(MessageFormat
                .format("ball[{0}].evals", idx));
        this.b = runtime.newUnknown(MessageFormat.format("ball[{0}].bottom",
                idx));
        this.h = runtime.newUnknown(MessageFormat.format("ball[{0}].h", idx));
        this.v = runtime.newUnknown(MessageFormat.format("ball[{0}].v", idx));

        this.runtime = runtime;
        this.dh = h.der();
        this.dv = v.der();

        this.freeFall = new ConstantEquation(dv, -9.81);

        this.evals = new Equation() {

            @Override
            public Collection<Unknown> unknowns() {
                return ImmutableList.of(e);
            }

            @Override
            public GlobalEquation bind(final Map<Unknown, GlobalVariable> ctxt) {
                return new GlobalEquation() {
                    final GlobalVariable ge = ctxt.get(e);

                    @Override
                    public List<GlobalVariable> need() {
                        return ImmutableList.of(ge);
                    }

                    @Override
                    public BlockEquation bind(
                            final Map<GlobalVariable, BlockVariable> blockCtxt) {
                        return new BlockEquation() {
                            final BlockVariable be = blockCtxt.get(ge);

                            @Override
                            public TDNumber exec(ExecutionContext m) {
                                return be.load(m).subtract(evaluations);
                            }
                        };
                    }
                };
            }

        };

        this.bottom = new Equation() {

            @Override
            public Collection<Unknown> unknowns() {
                return ImmutableList.of(b, h);
            }

            @Override
            public GlobalEquation bind(final Map<Unknown, GlobalVariable> ctxt) {
                return new GlobalEquation() {
                    final GlobalVariable gb = ctxt.get(b), gh = ctxt.get(h);

                    @Override
                    public List<GlobalVariable> need() {
                        return ImmutableList.of(gb, gh);
                    }

                    @Override
                    public BlockEquation bind(
                            final Map<GlobalVariable, BlockVariable> blockCtxt) {
                        return new BlockEquation() {
                            final BlockVariable bb = blockCtxt.get(gb),
                                    bh = blockCtxt.get(gh);

                            @Override
                            public TDNumber exec(ExecutionContext m) {
                                evaluations++;
                                return bb.load(m).subtract(bh.load(m)).add(0.5);
                            }
                        };
                    }
                };
            }
        };
        this.accel = new Equality(v, dh);
    }

    public Collection<Equation> equations() {
        return ImmutableList.of(bottom, freeFall, accel, evals);
    }

    public Collection<ContinuousEvent> events(Map<Unknown, GlobalVariable> ctxt) {
        return ImmutableList.of((ContinuousEvent) new BounceEvent(ctxt.get(b),
                ctxt.get(v)));
    }

    public final class BounceEvent extends ContinuousEvent {

        final GlobalVariable b, v;

        public BounceEvent(GlobalVariable b, GlobalVariable v) {
            super();
            this.b = b;
            this.v = v;
        }

        @Override
        public Collection<GlobalVariable> vars() {
            return ImmutableList.of(b);
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
            return dae.load(b);
        }

    }

}
