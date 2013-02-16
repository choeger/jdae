package de.tuberlin.uebb.jdae.builtins;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.tuberlin.uebb.jdae.dae.Unknown;

/**
 * This class yields a Function<Unknown, Unknown> that holds all derivative
 * relations. The LoadingCache needs to be populated once.
 * 
 * @author choeger
 * 
 */
public final class DerivativeCollector extends CacheLoader<Unknown, Unknown> {

    @Override
    public Unknown load(final Unknown base) throws Exception {
        return new Unknown() {
            @Override
            public String toString() {
                return "der(" + base + ")";
            }

            @Override
            public boolean isDerivative() {
                return true;
            }
        };
    }

    public static final LoadingCache<Unknown, Unknown> derivatives() {
        return CacheBuilder.newBuilder().build(new DerivativeCollector());
    }

}
