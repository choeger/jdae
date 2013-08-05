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

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.simulation.SimulationOptions;
import de.tuberlin.uebb.jdae.transformation.Causalisation;
import de.tuberlin.uebb.jdae.transformation.DerivedEquation;
import de.tuberlin.uebb.jdae.transformation.InitializationCausalisation;
import de.tuberlin.uebb.jdae.transformation.Reduction;

public final class ExecutableDAE implements FirstOrderDifferentialEquations {

    public final double[][] data;
    public final DataLayout layout;
    public final IBlock[] blocks;
    public final List<GlobalVariable> states;
    private final Logger logger;
    public final IBlock[] initials;
    public int evaluations;
    public long time;

    public ExecutableDAE(final DataLayout layout, Causalisation causalisation,
            InitializationCausalisation iCausalisation) {
        this.logger = Logger.getLogger(this.getClass().toString());

        this.layout = layout;
        this.states = causalisation.states;
        data = layout.alloc();

        this.blocks = new IBlock[causalisation.computations.size()];
        int i = 0;
        for (Set<Integer> block : causalisation.computations) {
            final Set<DerivedEquation> deriveds = Sets.newHashSet();
            int size = 0;
            for (int k : block) {
                deriveds.add(new DerivedEquation(causalisation.equations[k],
                        causalisation.eqn_derivatives[k]));
                size += causalisation.eqn_derivatives[k] + 1;
            }
            if (size == 1) {
                /* causalisation */
                final GlobalVariable var = causalisation.iteratees.get(i)
                        .iterator().next();
                final GlobalEquation eq = deriveds.iterator().next().eqn;
                if (eq.canSpecializeFor(var)) {
                    this.blocks[i] = eq.specializeFor(var, this);
                } else {
                    this.blocks[i] = new Block(data, layout,
                            ImmutableSet.of(var), deriveds);
                }
                i++;
            } else {
                this.blocks[i] = new Block(data, layout,
                        causalisation.iteratees.get(i++), deriveds);
            }

        }
        i = 0;

        this.initials = new IBlock[iCausalisation.computations.size()];
        for (Set<DerivedEquation> block : iCausalisation.computations)
            initials[i] = new Block(data, layout,
                    iCausalisation.iteratees.get(i++), block);
    }

    public ExecutableDAE(final DataLayout layout, final IBlock[] blocks,
            final IBlock[] initials, final List<GlobalVariable> states) {

        this.logger = Logger.getLogger(this.getClass().toString());
        this.layout = layout;
        this.states = states;
        data = layout.alloc();

        this.blocks = blocks.clone();
        this.initials = initials.clone();
    }

    public void initialize() {
        for (IBlock iB : initials)
            iB.exec();
    }

    public void integrate(final SimulationOptions options) {
        data[0][0] = options.startTime;

        logger.log(Level.INFO, "Starting integration.");
        final double[] stateVector = new double[states.size()];

        writeStates(stateVector);

        while (data[0][0] < options.stopTime) {
            data[0][0] = options.integrator.integrate(this, data[0][0],
                    stateVector, options.stopTime, stateVector);

            writeStates(stateVector);
        }
    }

    private void writeStates(final double[] stateVector) {
        int i = 0;
        for (GlobalVariable state : states)
            stateVector[i++] = load(state);
    }

    @Override
    public int getDimension() {
        return states.size();
    }

    public double load(GlobalVariable v) {
        return data[v.index][v.der];
    }

    public void set(GlobalVariable v, double d) {
        data[v.index][v.der] = d;
    }

    @Override
    public void computeDerivatives(double t, double[] y, double[] yDot)
            throws MaxCountExceededException, DimensionMismatchException {

        final long s = System.currentTimeMillis();
        evaluations++;

        data[0][0] = t;

        for (int i = 0; i < states.size(); i++) {
            set(states.get(i), y[i]);
        }

        for (int i = 0; i < blocks.length; i++) {
            blocks[i].exec();
        }

        for (int i = 0; i < states.size(); i++) {
            yDot[i] = load(states.get(i).der());
        }

        time += (System.currentTimeMillis() - s);
    }

    public void computeDerivativesUpTo(int block, double t, double[] y) {
        data[0][0] = t;
        for (int i = 0; i < states.size(); i++) {
            set(states.get(i), y[i]);
        }

        for (int i = 0; i < block && i < blocks.length; i++) {
            blocks[i].exec();
        }
    }

    public double load(Reduction reduction, Unknown x) {
        return load(reduction.ctxt.get(x));
    }

    public double time() {
        return data[0][0];
    }

}
