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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

import de.tuberlin.uebb.jdae.llmsl.ExecutableDAE;
import de.tuberlin.uebb.jdae.llmsl.GlobalVariable;

public final class ResultStorage implements StepHandler {

    private final ExecutableDAE dae;
    public final LinkedList<double[][]> results;

    public ResultStorage(ExecutableDAE dae) {
        super();
        this.dae = dae;
        results = Lists.newLinkedList();
    }

    @Override
    public void handleStep(StepInterpolator arg0, boolean arg1) {
        // if (dae.data[0][0] != arg0.getInterpolatedTime()) {
        // dae.computeDerivatives(arg0.getInterpolatedTime(),
        // arg0.getInterpolatedState(), new double[dae.getDimension()]);
        // }

        addResult(dae.data);
    }

    // TODO Automatisch generierter Methodenstub
    @Override
    public void init(double arg0, double[] arg1, double arg2) {

    }

    public Path toJson(String file) {
        Path f = Paths.get(file);
        try {
            BufferedWriter w = Files.newBufferedWriter(f,
                    Charset.defaultCharset());

            JsonWriter json = new JsonWriter(w);

            JsonArray jResults = new JsonArray();

            for (GlobalVariable var : dae.layout) {
                JsonObject obj = new JsonObject();
                obj.add("label", new JsonPrimitive(var.toString()));
                JsonArray data = new JsonArray();
                for (double[][] step : results) {
                    JsonArray point = new JsonArray();
                    point.add(new JsonPrimitive(step[0][0]));
                    point.add(new JsonPrimitive(step[var.index][var.der]));
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
    public List<double[][]> select(double from, double to, final double step) {
        final List<double[][]> steps = Lists
                .newArrayListWithExpectedSize((int) ((to - from) / step));

        for (double[][] r : results) {
            if (r[0][0] >= from) {
                steps.add(r);
                if ((from += step) >= to) {
                    break;
                }
            }
        }
        return steps;
    }

    public void addResult(double[][] data) {
        while (!results.isEmpty() && (results.peekLast()[0][0] >= data[0][0])) {
            results.removeLast();
        }

        results.add(data);
    }

}
