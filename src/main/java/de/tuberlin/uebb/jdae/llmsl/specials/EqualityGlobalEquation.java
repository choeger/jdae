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

package de.tuberlin.uebb.jdae.llmsl.specials;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import de.tuberlin.uebb.jdae.diff.total.TDNumber;
import de.tuberlin.uebb.jdae.llmsl.BlockEquation;
import de.tuberlin.uebb.jdae.llmsl.BlockVariable;
import de.tuberlin.uebb.jdae.llmsl.ExecutionContext;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;

public final class EqualityGlobalEquation extends GlobalEquation {

    public final GlobalVariable lhs, rhs;

    public EqualityGlobalEquation(GlobalVariable lhs, GlobalVariable rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public List<GlobalVariable> need() {
        return ImmutableList.of(lhs, rhs);
    }

    @Override
    public BlockEquation bind(final Map<GlobalVariable, BlockVariable> blockCtxt) {
        return new BlockEquation() {

            final BlockVariable l = blockCtxt.get(lhs), r = blockCtxt.get(rhs);

            @Override
            public TDNumber exec(ExecutionContext m) {
                return l.load(m).subtract(r.load(m));
            }
        };
    }

    public String toString() {
        return String.format("%s = %s", lhs, rhs);
    }

}
