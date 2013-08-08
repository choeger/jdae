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

package de.tuberlin.uebb.jdae.llmsl.events;

import de.tuberlin.uebb.jdae.llmsl.BlockEquation;
import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;

/**
 * @author choeger
 * 
 */
public final class Reinit implements EventEffect {

    public final GlobalVariable target;
    private final BlockEquation val;

    public Reinit(GlobalVariable target, GlobalEquation val) {
        super();
        this.target = target;
        this.val = val.bindIdentity();
    }

    @Override
    public ExecutableDAE apply(ExecutableDAE source) {
        final double d = val.exec(source.execCtxt).der(0);
        source.set(target, d);
        return source;
    }
}
