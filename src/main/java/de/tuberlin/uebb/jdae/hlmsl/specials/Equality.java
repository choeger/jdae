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

package de.tuberlin.uebb.jdae.hlmsl.specials;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.llmsl.specials.EqualityGlobalEquation;

public final class Equality implements Equation {

    public final Unknown lhs, rhs;

    public Equality(Unknown lhs, Unknown rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public GlobalEquation bind(Map<Unknown, GlobalVariable> ctxt) {
        return new EqualityGlobalEquation(ctxt.get(lhs), ctxt.get(rhs));
    }

    @Override
    public Collection<Unknown> unknowns() {
        return ImmutableList.of(lhs, rhs);
    }

}