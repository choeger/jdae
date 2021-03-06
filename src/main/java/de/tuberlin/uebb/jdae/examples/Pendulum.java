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
import de.tuberlin.uebb.jdae.diff.total.TDRegister;
import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.hlmsl.specials.ConstantEquation;
import de.tuberlin.uebb.jdae.llmsl.BlockEquation;
import de.tuberlin.uebb.jdae.llmsl.BlockVariable;
import de.tuberlin.uebb.jdae.llmsl.DirectBlock;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.ExecutionContext;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.llmsl.IBlock;
import de.tuberlin.uebb.jdae.llmsl.events.ContinuousEvent;
import de.tuberlin.uebb.jdae.simulation.SimulationRuntime;

/**
 * A cartesian pendulum model
 * 
 * @author Christoph Höger
 * 
 */
public final class Pendulum implements LoadableModel {

    public final Unknown x, y, F;

    public Pendulum(final SimulationRuntime runtime) {
        this.x = runtime.newUnknown("x");
        this.y = runtime.newUnknown("y");
        this.F = runtime.newUnknown("F");
    }

    @Override
    public Map<GlobalVariable, Double> initials(
            Map<Unknown, GlobalVariable> ctxt) {
        return ImmutableMap.of(ctxt.get(x), 0.1);
    }

    @Override
    public Collection<Equation> equations() {
        return ImmutableList.of(new XAccelEquation(), new YAccelEquation(),
                new LengthEquation());
    }

    @Override
    public Collection<Equation> initialEquations() {
        return ImmutableList.of((Equation) new ConstantEquation(y, -0.9));
    }

    public String name() {
        return "Pendulum";
    }

    public Collection<ContinuousEvent> events(Map<Unknown, GlobalVariable> ctxt) {
        return ImmutableList.of();
    }

    /**
     * d²x/dt = F*x
     */
    public final class XAccelEquation implements Equation {
        @Override
        public Collection<Unknown> unknowns() {
            return ImmutableList.of(x, x.der(2), F);
        }

        @Override
        public GlobalEquation bind(final Map<Unknown, GlobalVariable> ctxt) {
            return new XAccelGlobalEquation(ctxt.get(x), ctxt.get(F));
        }

        @Override
        public String toString() {
            return String.format("%s = %s * %s", x.der(2), F, x);
        }
    }

    public static final class XAccelGlobalEquation extends GlobalEquation {
        public final GlobalVariable x, F;

        public XAccelGlobalEquation(GlobalVariable x, GlobalVariable F) {
            this.x = x;
            this.F = F;
        }

        public List<GlobalVariable> need() {
            return ImmutableList.of(x, x.der(2), F);
        }

        public BlockEquation bind(final Map<GlobalVariable, BlockVariable> ctxt) {
            return new XAccelBlockEquation(ctxt.get(x), ctxt.get(F));
        }

        public boolean canSpecializeFor(GlobalVariable v) {
            return v.isOneOf(x, x.der(2), F);
        }

        public IBlock specializeFor(GlobalVariable v, final IBlock alt,
                final ExecutableDAE dae) {
            if (v.equals(x)) {
                return new DirectBlock(x, alt, dae) {
                    public double computeValue() {
                        return dae.load(x.der(2)) / dae.load(F);
                    }
                };
            } else if (v.equals(F)) {
                return new DirectBlock(F, alt, dae) {
                    public double computeValue() {
                        return dae.load(x.der(2)) / dae.load(x);
                    }
                };
            } else if (v.equals(x.der(2))) {
                return new DirectBlock(x.der(2), alt, dae) {
                    public double computeValue() {
                        return dae.load(F) * dae.load(x);
                    }
                };
            }
            throw new IllegalArgumentException("Cannot specialize for " + v);
        }

        @Override
        public String toString() {
            return String.format("%s = %s * %s", x.der(2), F, x);
        }

    }

    public static final class XAccelBlockEquation implements BlockEquation {
        public final BlockVariable x, F;

        public XAccelBlockEquation(BlockVariable x, BlockVariable F) {
            this.x = x;
            this.F = F;
        }

        public TDNumber exec(final ExecutionContext m) {
            final TDRegister reg1 = new TDRegister(x.load(m));
            reg1.mult(F.load(m));
            reg1.mult(-1);
            reg1.add(x.der(m, 2).load(m));
            return reg1.unsafe();
        }
    }

    /**
     * d²y/dt = F*y - g
     */
    public final class YAccelEquation implements Equation {
        @Override
        public Collection<Unknown> unknowns() {
            return ImmutableList.of(x, y.der(2), F);
        }

        @Override
        public GlobalEquation bind(final Map<Unknown, GlobalVariable> ctxt) {
            return new YAccelGlobalEquation(ctxt.get(y), ctxt.get(F));
        }

        @Override
        public String toString() {
            return String.format("%s = %s * %s", y.der(2), F, y);
        }
    }

    public static final class YAccelGlobalEquation extends GlobalEquation {
        public final GlobalVariable y, F;
        private final static double g = 9.81;

        public YAccelGlobalEquation(GlobalVariable y, GlobalVariable F) {
            this.y = y;
            this.F = F;
        }

        public BlockEquation bind(final Map<GlobalVariable, BlockVariable> ctxt) {
            return new YAccelBlockEquation(ctxt.get(y), ctxt.get(F));
        }

        public List<GlobalVariable> need() {
            return ImmutableList.of(y, y.der(2), F);
        }

        public boolean canSpecializeFor(GlobalVariable v) {
            return v.isOneOf(y, y.der(2), F);
        }

        public IBlock specializeFor(GlobalVariable v, final IBlock alt,
                final ExecutableDAE dae) {
            if (v.equals(y)) {
                return new DirectBlock(y, alt, dae) {
                    public double computeValue() {
                        return (dae.load(y.der(2)) + g) / dae.load(F);
                    }
                };
            } else if (v.equals(F)) {
                return new DirectBlock(F, alt, dae) {
                    public double computeValue() {
                        return (dae.load(y.der(2)) + g) / dae.load(y);
                    }
                };
            } else if (v.equals(y.der(2))) {
                return new DirectBlock(y.der(2), alt, dae) {
                    public double computeValue() {
                        return dae.load(F) * dae.load(y) - g;
                    }
                };
            }
            throw new IllegalArgumentException("Cannot specialize for " + v);
        }

        @Override
        public String toString() {
            return String.format("%s = %s * %s - %f", y.der(2), F, y, g);
        }

    }

    public final static class YAccelBlockEquation implements BlockEquation {
        public final BlockVariable y, F;
        private final static double g = 9.81;

        public YAccelBlockEquation(BlockVariable y, BlockVariable F) {
            this.y = y;
            this.F = F;
        }

        public TDNumber exec(final ExecutionContext m) {
            final TDRegister reg1 = new TDRegister(y.load(m));
            reg1.mult(F.load(m));
            reg1.add(-g);
            reg1.mult(-1);
            reg1.add(y.der(m, 2).load(m));
            return reg1.unsafe();
        }
    }

    /**
     * x² + y² = 1 as a general equation
     */
    public final class LengthEquation implements Equation {

        @Override
        public Collection<Unknown> unknowns() {
            return ImmutableList.of(x, y);
        }

        @Override
        public GlobalEquation bind(final Map<Unknown, GlobalVariable> ctxt) {
            return new LengthGlobalEquation(ctxt.get(x), ctxt.get(y));
        }

        @Override
        public String toString() {
            return String.format("%s² + %s² = 1", x, y);
        }
    }

    /**
     * x² + y² = 1 as a global equation (after reduction of equivalences)
     */
    public final static class LengthGlobalEquation extends GlobalEquation {

        public final GlobalVariable x, y;

        public LengthGlobalEquation(GlobalVariable x, GlobalVariable y) {
            this.x = x;
            this.y = y;
        }

        public List<GlobalVariable> need() {
            return ImmutableList.of(x, y);
        }

        public BlockEquation bind(final Map<GlobalVariable, BlockVariable> ctxt) {
            return new LengthBlockEquation(ctxt.get(x), ctxt.get(y));
        }

        public boolean canSpecializeFor(GlobalVariable v) {
            return v.equals(x) || v.equals(y);
        }

        public IBlock specializeFor(GlobalVariable v, final IBlock alt,
                final ExecutableDAE dae) {
            if (v.equals(x)) {
                return new DirectBlock(x, alt, dae) {
                    public double computeValue() {
                        return (dae.load(x) < 0 ? -1 : 1)
                                * Math.sqrt(1 - Math.pow(dae.load(y), 2));
                    }
                };
            } else if (v.equals(y)) {
                return new DirectBlock(y, alt, dae) {
                    public double computeValue() {
                        return (dae.load(y) < 0 ? -1 : 1)
                                * Math.sqrt(1 - Math.pow(dae.load(x), 2));
                    }
                };
            }

            throw new IllegalArgumentException("Cannot specialize for " + v);
        }

        @Override
        public String toString() {
            return String.format("%s² + %s² = 1", x, y);
        }
    }

    /**
     * x² + y² = 1 as a block equation (after causalisation)
     */
    public static final class LengthBlockEquation implements BlockEquation {

        public final BlockVariable x, y;

        public LengthBlockEquation(BlockVariable x, BlockVariable y) {
            this.x = x;
            this.y = y;
        }

        public TDNumber exec(final ExecutionContext m) {
            final TDRegister reg1 = new TDRegister(x.load(m));
            reg1.square();
            final TDRegister reg2 = new TDRegister(y.load(m));
            reg2.square();
            reg1.add(reg2.unsafe());
            reg1.add(-1);

            return reg1.unsafe();
        }
    }

}
