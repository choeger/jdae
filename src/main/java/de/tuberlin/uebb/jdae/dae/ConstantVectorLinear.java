/**
 * 
 */
package de.tuberlin.uebb.jdae.dae;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.math3.analysis.UnivariateVectorFunction;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

import de.tuberlin.uebb.jdae.builtins.LinearFunctionalVectorEquation;

/**
 * @author choeger
 * 
 */
public class ConstantVectorLinear implements VectorEquation {

    private final Collection<ConstantLinear> parts;
    private final Set<Unknown> variables;

    public ConstantVectorLinear(Collection<ConstantLinear> parts) {
        super();
        this.parts = parts;
        this.variables = Sets.newHashSet();
        for (ConstantLinear part : parts)
            variables.addAll(part.unknowns());
    }

    @Override
    public FunctionalVectorEquation specializeFor(Set<Unknown> unknowns,
            SolvableDAE system) {

        final Set<Unknown> rest = Sets.difference(variables, unknowns);

        final ConstantLinear a_parts[] = parts.toArray(new ConstantLinear[] {});
        final FunctionalEquation fmap[][] = new FunctionalEquation[parts.size()][];

        for (int i = 0; i < a_parts.length; i++) {
            final int size = a_parts[i].unknowns().size();
            final FunctionalEquation feq[] = new FunctionalEquation[size];
            for (int j = 0; j < size; j++) {
                final Unknown u = a_parts[i].unknowns().get(j);
                if (rest.contains(u)) {
                    feq[j] = system.get(u);
                }
            }
            fmap[i] = feq;
        }

        final int[] unknowns_idx = new int[unknowns.size()];
        final Iterator<Unknown> iter = unknowns.iterator();
        int i = 0;
        while (iter.hasNext()) {
            unknowns_idx[i++] = system.variables.get(iter.hasNext());
        }

        return new LinearFunctionalVectorEquation(unknowns_idx, fmap, a_parts);
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.dae.VectorEquation#canSolveFor(com.google.common
     * .base.Function)
     */
    @Override
    public Collection<Unknown> canSolveFor(Function<Unknown, Unknown> der) {
        return variables;
    }

    /*
     * (nicht-Javadoc)
     * 
     * @see
     * de.tuberlin.uebb.jdae.dae.VectorEquation#residual(de.tuberlin.uebb.jdae
     * .dae.SolvableDAE)
     */
    @Override
    public UnivariateVectorFunction residual(SolvableDAE system) {
        // TODO Automatisch generierter Methodenstub
        return null;
    }

}
