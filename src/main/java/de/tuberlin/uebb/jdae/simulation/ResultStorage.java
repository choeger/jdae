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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

import de.tuberlin.uebb.jdae.dae.SolvableDAE;
import de.tuberlin.uebb.jdae.dae.Unknown;

public final class ResultStorage implements StepHandler {

    public static final class Step {
        public final double time;
        public final double[] states;
        public final double[] algebraics;
        public final double[] derivatives;

        public Step(double time, double[] derivatives, double[] states,
                double[] algebraics) {
            super();
            this.time = time;
            this.states = states;
            this.derivatives = derivatives;
            this.algebraics = algebraics;
        }

        public String toString() {
            return String.format(Locale.ENGLISH,
                    "derivatives: %s algebraics: %s states: %s", states,
                    derivatives, algebraics);

        }
    }

    private final SolvableDAE dae;
    public final List<Step> results;

    public ResultStorage(SolvableDAE dae, int estimatedSteps) {
        super();
        this.dae = dae;
        results = Lists.newArrayListWithCapacity(estimatedSteps);
    }

    @Override
    public void handleStep(StepInterpolator arg0, boolean arg1) {
        results.add(new Step(arg0.getInterpolatedTime(), Arrays.copyOf(
                arg0.getInterpolatedDerivatives(), dae.dimension), Arrays
                .copyOf(arg0.getInterpolatedState(), dae.dimension), Arrays
                .copyOf(dae.algebraics, dae.algebraics.length)));
    }

    @Override
    public void init(double arg0, double[] arg1, double arg2) {
        // TODO Automatisch generierter Methodenstub

    }

    public Path toJson(String file) {
        Path f = Paths.get(file);
        try {
            BufferedWriter w = Files.newBufferedWriter(f,
                    Charset.defaultCharset());

            JsonWriter json = new JsonWriter(w);

            JsonArray jResults = new JsonArray();

            for (Unknown u : dae.variables.keySet()) {
                JsonObject obj = new JsonObject();
                obj.add("label", new JsonPrimitive(u.toString()));
                JsonArray data = new JsonArray();
                for (Step step : results) {
                    JsonArray point = new JsonArray();
                    point.add(new JsonPrimitive(step.time));
                    point.add(new JsonPrimitive(dae.valueAt(step, u)));
                    data.add(point);
                }
                obj.add("data", data);
                jResults.add(obj);
            }

            (new Gson()).toJson(jResults, json);

            w.close();
            return f;
        } catch (IOException e) {
            // TODO Automatisch generierter Erfassungsblock
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Select a view of the result set
     * 
     * @param from
     *            the start time of the view
     * @param to
     *            the end time of the view
     * @param step
     *            the maximal step size between to data points in the view
     * @return a list of data points of this result
     */
    public List<Step> select(double from, final double to, final double step) {
        final List<Step> steps = Lists
                .newArrayListWithExpectedSize((int) ((to - from) / step));

        for (Step r : results) {
            if (r.time >= from) {
                steps.add(r);
                if ((from += step) >= to)
                    break;
            }
        }

        return steps;
    }

    public Iterable<Unknown> variables() {
        return dae.variables.keySet();
    }

    public double valueAt(Step step, Unknown u) {
        return dae.valueAt(step, u);
    }
}
