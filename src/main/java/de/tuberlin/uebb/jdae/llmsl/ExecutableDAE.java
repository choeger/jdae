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

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.llmsl.events.ContinuousEvent;
import de.tuberlin.uebb.jdae.llmsl.events.EventEffect;
import de.tuberlin.uebb.jdae.llmsl.events.EventEvaluator;
import de.tuberlin.uebb.jdae.simulation.ResultStorage;
import de.tuberlin.uebb.jdae.simulation.SimulationOptions;
import de.tuberlin.uebb.jdae.transformation.Causalisation;
import de.tuberlin.uebb.jdae.transformation.DerivedEquation;
import de.tuberlin.uebb.jdae.transformation.InitializationCausalisation;
import de.tuberlin.uebb.jdae.transformation.Reduction;

public final class ExecutableDAE implements FirstOrderDifferentialEquations {

    public final double[][] data;
    public final EventEvaluator eventHandler;
    public final DataLayout layout;
    public final IBlock[] blocks;
    public final List<GlobalVariable> states;
    private final Logger logger;
    public final IBlock[] initials;
    private int evaluations;
    public long time;

    public final ExecutionContext execCtxt;

    public int getEvaluations() {
        return evaluations;
    }

    public ExecutableDAE(final DataLayout layout, Causalisation causalisation,
            InitializationCausalisation iCausalisation,
            final ContinuousEvent[] continuousEvents) {
        this.logger = Logger.getLogger(this.getClass().toString());

        this.layout = layout;
        this.states = causalisation.states;
        data = layout.alloc();

        this.blocks = new IBlock[causalisation.computations.size()];
        int i = 0;
        for (TIntObjectMap<Range<Integer>> block : causalisation.computations) {

            final Set<DerivedEquation> deriveds = Sets.newHashSet();
            final TIntObjectIterator<Range<Integer>> blockIter = block
                    .iterator();
            while (blockIter.hasNext()) {
                blockIter.advance();
                final int eq = blockIter.key();
                final Range<Integer> eqRange = blockIter.value();

                deriveds.add(new DerivedEquation(causalisation.equations[eq],
                        eqRange.lowerEndpoint(), eqRange.upperEndpoint()));
            }

            final IBlock block_i;
            /* always prepare a numerical fallback, just in case */
            final IBlock numericalSolution = new Block(data, layout,
                    causalisation.iteratees.get(i), deriveds);

            if (causalisation.iteratees.get(i).size() == 1) {
                /* causalisation */
                final GlobalVariable var = causalisation.iteratees.get(i)
                        .iterator().next();
                final GlobalEquation eq = deriveds.iterator().next().eqn;

                if (eq.canSpecializeFor(var)) {
                    block_i = eq.specializeFor(var, numericalSolution, this);
                } else {
                    block_i = numericalSolution;
                }
            } else {
                block_i = numericalSolution;
            }

            blocks[i++] = block_i;

        }
        i = 0;

        this.initials = new IBlock[iCausalisation.computations.size()];
        for (Set<DerivedEquation> block : iCausalisation.computations)
            initials[i] = new Block(data, layout,
                    iCausalisation.iteratees.get(i++), block);

        this.execCtxt = new ExecutionContext(0, new GlobalVariable[0], data);

	//must be last, calls methods here
        this.eventHandler = new EventEvaluator(this, continuousEvents);
    }

    public ExecutableDAE(final DataLayout layout, final IBlock[] blocks,
            final IBlock[] initials, final List<GlobalVariable> states) {

        this.eventHandler = new EventEvaluator(this, new ContinuousEvent[0]);
        this.logger = Logger.getLogger(this.getClass().toString());
        this.layout = layout;
        this.states = states;
        data = layout.alloc();

        this.blocks = blocks.clone();
        this.initials = initials.clone();
        this.execCtxt = new ExecutionContext(0, new GlobalVariable[0], data);
    }

    public void initialize() {
        for (IBlock iB : initials)
            iB.exec();
    }

    public void integrate(final ResultStorage results,
            final SimulationOptions options) {
        evaluations = 0;
        data[0][0] = options.startTime;

        if (options.integrator != null) {
            integrateApacheCommonsIntegrator(options);
        } else {
            switch (options.inlineIntegrator) {
            case INLINE_FORWARD_EULER:
                integrateInlineForwardEuler(results, options);
                break;
            }
        }
    }

    private void integrateInlineForwardEuler(final ResultStorage results,
            SimulationOptions options) {
        final long s = System.currentTimeMillis();
        double stepSize = options.minStepSize;

        while (data[0][0] < options.stopTime) {
            data[0][0] += stepSize;

            if (options.stopTime - data[0][0] < stepSize)
                stepSize = options.stopTime - data[0][0];

            evaluations++;

            for (int i = 0; i < blocks.length; i++) {
                blocks[i].exec();
            }

            for (GlobalVariable state : states) {
                data[state.index][state.der] += data[state.index][state.der + 1]
                        * stepSize;
            }

            final Collection<EventEffect> effects = eventHandler
                    .calculateEffects();

            if (!effects.isEmpty()) {
                ExecutableDAE nextDae = this;

                for (EventEffect effect : effects) {
                    nextDae = effect.apply(nextDae);
                }

                if (nextDae != this) {
                    throw new RuntimeException(
                            "Cannot handle structural changes yet.");
                }
            }

            /* accept step */
            eventHandler.acceptLastStep();
            //results.addResult(data);
        }

        time += (System.currentTimeMillis() - s);
    }

    public void integrateApacheCommonsIntegrator(final SimulationOptions options) {
        logger.log(Level.INFO, "Starting integration.");

	for (EventHandler handler : eventHandler.getEventHandlers())
	    options.integrator.addEventHandler(handler, options.maxStepSize,
					       options.tolerance, 1000);

        final double[] stateVector = new double[states.size()];

        writeStates(stateVector);

        while (data[0][0] < options.stopTime) {
            data[0][0] = options.integrator.integrate(this, data[0][0],
                    stateVector, options.stopTime, stateVector);

            if (eventHandler.getResult() != null
                    && eventHandler.getResult().t == data[0][0]) {
                final ExecutableDAE next = eventHandler.getResult().effect
                        .apply(this);
                eventHandler.acceptLastStep();
                if (next != this) {
                    throw new RuntimeException(
                            "Cannot handle structural changes yet.");
                }
            }

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
        setState(t, y);

        computeDerivatives(0, blocks.length);

        for (int i = 0; i < states.size(); i++) {
            yDot[i] = load(states.get(i).der());
        }
    }

    public void computeDerivatives(final int from, final int to) {
        final long s = System.currentTimeMillis();

        evaluations++;

        for (int i = from; i < to; i++) {
            blocks[i].exec();
        }

        time += (System.currentTimeMillis() - s);
    }

    public void setState(double t, double[] y) {
        data[0][0] = t;

        for (int i = 0; i < states.size(); i++) {
            set(states.get(i), y[i]);
        }
    }

    public double load(Reduction reduction, Unknown x) {
        return load(reduction.ctxt.get(x));
    }

    public double time() {
        return data[0][0];
    }

    public int lastBlock(final Collection<GlobalVariable> vars) {
        final Set<GlobalVariable> left = Sets.newTreeSet(vars);
        left.removeAll(states);

        int i = 0;
        while (!left.isEmpty() && i < blocks.length) {
            for (GlobalVariable gv : blocks[i].variables())
                left.remove(gv);
            i++;
        }

        if (!left.isEmpty())
            throw new RuntimeException(
                    "The following variables are not computed: "
                            + left.toString());
        return i;
    }

}
