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
package de.tuberlin.uebb.jdae.dae;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.tuberlin.uebb.jdae.hlmsl.Equation;
import de.tuberlin.uebb.jdae.hlmsl.Unknown;
import de.tuberlin.uebb.jdae.llmsl.GlobalEquation;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;
import de.tuberlin.uebb.jdae.llmsl.events.ContinuousEvent;

/**
 * @author choeger
 * 
 */
public interface LoadableModel {

    public Map<GlobalVariable, Double> initials(
            Map<Unknown, GlobalVariable> ctxt);

    public Collection<Equation> equations();

    public String name();

    public Collection<ContinuousEvent> events(Map<Unknown, GlobalVariable> ctxt);

    public List<GlobalEquation> initialEquations(Map<Unknown, GlobalVariable> ctxt);
    
}
