/**
 * 
 */
package de.tuberlin.uebb.jdae.simulation;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;

import de.tuberlin.uebb.jdae.dae.Unknown;

/**
 * @author choeger
 * 
 */
public interface DerivativeRelation extends Function<Unknown, Unknown> {

    public Set<Unknown> domain();

    public Map<Unknown, Unknown> asMap();

}
