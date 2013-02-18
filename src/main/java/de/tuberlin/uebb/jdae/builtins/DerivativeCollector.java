package de.tuberlin.uebb.jdae.builtins;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import de.tuberlin.uebb.jdae.dae.Unknown;
import de.tuberlin.uebb.jdae.simulation.DerivativeRelation;

/**
 * This class yields a Function<Unknown, Unknown> that holds all derivative
 * relations. The LoadingCache needs to be populated once.
 * 
 * @author choeger
 * 
 */
public final class DerivativeCollector implements DerivativeRelation {
    final Map<Unknown, Unknown> map = Maps.newHashMap();

    @Override
    public Unknown apply(Unknown x) {
        final Unknown d = map.get(x);
        if (d == null) {
            final Unknown mDerVar = new SimpleDer(x.toString());
            map.put(x, mDerVar);
            return mDerVar;
        }
        return d;
    }

    @Override
    public Set<Unknown> domain() {
        return map.keySet();
    }

    @Override
    public Map<Unknown, Unknown> asMap() {
        return map;
    }
}
