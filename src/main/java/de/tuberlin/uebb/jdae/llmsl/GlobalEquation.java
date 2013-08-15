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
import java.util.Map;

import com.google.common.collect.Sets;
import com.google.common.base.Function;

import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.transformation.DerivedEquation;

public abstract class GlobalEquation {

    public abstract List<GlobalVariable> need();

    public abstract BlockEquation bind(
            final Map<GlobalVariable, BlockVariable> blockCtxt);

    public BlockEquation bindIdentity() {
        return bind(BlockConstant.globalCtxt(Sets.newTreeSet(need())));
    }

    public DerivedEquation der(int i) {
        return new DerivedEquation(this, i);
    }

    public boolean canSpecializeFor(GlobalVariable v) {
        return false;
    }

    public IBlock specializeFor(GlobalVariable v, IBlock alt, ExecutableDAE dae) {
        throw new RuntimeException("Cannot specialize for " + v);
    }

    public static Function<Equation, GlobalEquation> bindFrom(final Map<Unknown, GlobalVariable> ctxt) {
	return new Function<Equation, GlobalEquation>() {
	    public GlobalEquation apply(Equation eq) {
		return eq.bind(ctxt);
	    }
	};
    }
}
