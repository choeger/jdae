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
package de.tuberlin.uebb.jdae.tests.simulation;

import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.hlmsl.specials.ConstantEquation;
import de.tuberlin.uebb.jdae.hlmsl.specials.ConstantLinear;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.transformation.Causalisation;
import de.tuberlin.uebb.jdae.transformation.InitializationCausalisation;
import de.tuberlin.uebb.jdae.transformation.InitializationMatching;
import de.tuberlin.uebb.jdae.transformation.Matching;
import de.tuberlin.uebb.jdae.transformation.Reduction;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThat;

public class CausalisedSystemTest {

    final Unknown a = new Unknown("a", 1, 0);
    final Unknown b = new Unknown("b", 2, 0);
    final Unknown c = new Unknown("c", 3, 0);
    final Unknown d = new Unknown("d", 4, 0);
    final Equation eq_a = new ConstantEquation(a, 1.0);

    // a - 2b = 0
    final Equation eq_b = new ConstantLinear(0, 0, new double[] { 1.0, -2.0 },
            ImmutableList.of(a, b));

    // b = c
    final Equation eq_c = new ConstantLinear(0, 0, new double[] { 1.0, -1.0 },
            ImmutableList.of(b, c));

    // d = c
    final Equation eq_d = new ConstantLinear(0, 0, new double[] { 1.0, -1.0 },
            ImmutableList.of(d, c));

    final List<Equation> equations = ImmutableList.of(eq_d, eq_c, eq_a, eq_b);
    final Reduction reduction = new Reduction(equations);

    final Matching matching = new Matching(reduction,
            Logger.getLogger("CausalisationTest"));

    @Test
    public void testMatching() {
        assertThat(matching.assignment.length, is(4));
        assertThat(matching.assignment[0], is(3)); // ->d
        assertThat(matching.assignment[1], is(2)); // ->c
        assertThat(matching.assignment[2], is(0)); // ->a
        assertThat(matching.assignment[3], is(1)); // ->b
    }

    final Causalisation causalisation = new Causalisation(reduction, matching);

    @Test
    public void testCausalisation() {
        assertThat(causalisation.computations.size(), is(4));
        assertThat(causalisation.iteratees.get(0),
                containsInAnyOrder(reduction.ctxt.get(a)));
        assertThat(causalisation.iteratees.get(1),
                containsInAnyOrder(reduction.ctxt.get(b)));
        assertThat(causalisation.iteratees.get(2),
                containsInAnyOrder(reduction.ctxt.get(c)));
        assertThat(causalisation.iteratees.get(3),
                containsInAnyOrder(reduction.ctxt.get(d)));
    }

    final InitializationMatching initMatching = new InitializationMatching(
            reduction, causalisation, matching,
            ImmutableList.<GlobalEquation> of(),
            ImmutableMap.<GlobalVariable, Double> of(),
            Logger.getLogger("CausalisationTest"));

    @Test
    public void testInitializationMatching() {
        assertThat(initMatching.assignment.length, is(4));
        assertThat(initMatching.assignment[0], is(3)); // ->d
        assertThat(initMatching.assignment[1], is(2)); // ->c
        assertThat(initMatching.assignment[2], is(0)); // ->a
        assertThat(initMatching.assignment[3], is(1)); // ->b
    }

    final InitializationCausalisation initCausalisation = new InitializationCausalisation(
            initMatching, Logger.getLogger("CausalisationTest"));

    @Test
    public void testInitializationCausalisation() {
        assertThat(initCausalisation.computations.size(), is(4));
        assertThat(initCausalisation.iteratees.get(0),
                containsInAnyOrder(reduction.ctxt.get(a)));
        assertThat(initCausalisation.iteratees.get(1),
                containsInAnyOrder(reduction.ctxt.get(b)));
        assertThat(initCausalisation.iteratees.get(2),
                containsInAnyOrder(reduction.ctxt.get(c)));
        assertThat(initCausalisation.iteratees.get(3),
                containsInAnyOrder(reduction.ctxt.get(d)));
    }
}
