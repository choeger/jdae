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

package de.tuberlin.uebb.jdae.simulation;

import java.util.Map;

import org.apache.commons.math3.ode.FirstOrderIntegrator;

public final class SimulationOptions {

    public final double startTime;
    public final double stopTime;
    public final FirstOrderIntegrator integrator;

    public final double minStepSize;
    public final double maxStepSize;

    public final double tolerance;

    public final Map<String, Double> initialValues;

    public SimulationOptions(double startTime, double stopTime, double tol,
            double minStep, double maxStep, FirstOrderIntegrator integrator,
            Map<String, Double> initials) {
        super();
        this.startTime = startTime;
        this.minStepSize = minStep;
        this.maxStepSize = maxStep;
        this.tolerance = tol;
        this.stopTime = stopTime;
        this.integrator = integrator;
        this.initialValues = initials;
    }

}
