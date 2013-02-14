/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tuberlin.uebb.jdae.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Equivalence;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

/**
 * A mutable {@link Equivalence} implementation allowing equivalence classes to
 * be merged, as in a <a
 * href="http://en.wikipedia.org/wiki/Union_find">union-find algorithm</a>.
 * 
 * <p>
 * Upon creation, a {@code UnionFindEquivalence} represents the same equivalence
 * relation as {@link Equivalence#equals()}. However, the equivalence classes
 * for {@code a} and {@code b} can be merged using
 * {@link #merge(Object, Object)}.
 * 
 * <p>
 * Conceptually, if all objects (up to {@linkplain Object#equals(Object)
 * equality} represent nodes in a graph and {@link #merge(Object, Object) merge}
 * adds edges between two objects, then
 * {@link UnionFindEquivalence#equivalent(Object, Object)} returns {@code true}
 * if the objects are in the same connected component.
 * 
 * @author Louis Wasserman
 */
@GwtCompatible
public final class UnionFindEquivalence<T> extends Equivalence<T> {
    private static final class Partition {
        private Partition parent = this;
        private int rank = 0;

        private Partition representative() {
            Partition a = this;
            Partition b = this.parent;
            while (a != b) {
                Partition c = b.parent;
                a.parent = c;
                a = b;
                b = c;
            }
            this.parent = a;
            return a;
        }

        boolean merge(Partition p) {
            Partition rep1 = representative();
            Partition rep2 = p.representative();
            if (rep1 == rep2) {
                return false;
            } else if (rep1.rank < rep2.rank) {
                Partition tmp = rep1;
                rep1 = rep2;
                rep2 = tmp;
            } else if (rep1.rank == rep2.rank) {
                rep1.parent = rep2;
                rep2.rank++;
                return true;
            }
            // rep1.rank > rep2.rank
            rep2.parent = rep1;
            return true;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(representative());
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof Partition) {
                Partition p = (Partition) obj;
                return representative() == p.representative();
            }
            return false;
        }
    }

    public static <T> UnionFindEquivalence<T> create() {
        return new UnionFindEquivalence<T>();
    }

    private final Map<T, Partition> map;

    private UnionFindEquivalence() {
        this.map = Maps.newHashMap();
    }

    @Override
    protected boolean doEquivalent(T a, T b) {
        if (Objects.equal(a, b)) {
            return true;
        } else {
            Partition pA = map.get(a);
            Partition pB = map.get(b);
            return pA != null && pA.equals(pB);
        }
    }

    @Override
    protected int doHash(T t) {
        Partition p = map.get(t);
        if (p == null) {
            /*
             * t is conceptually in its own partition, which we identify by t's
             * hash code.
             */
            return (t == null) ? 0 : t.hashCode();
        } else {
            return p.hashCode();
        }
    }

    /**
     * Merges the equivalence classes of {@code a} and {@code b}.
     * 
     * <p>
     * Specifically, if before this call, {@code equivalent(x, y)} returned
     * {@code false}, {@code equivalent(a, x)} returned {@code true}, and
     * {@code equivalent(b, y)} returned {@code true}, then after this call
     * {@code equivalent(x, y)} will return {@code true}.
     * 
     * @return {@code true} if this equivalence relation changed as a result of
     *         this call
     */
    public boolean merge(@Nullable T a, @Nullable T b) {
        if (Objects.equal(a, b)) {
            return false;
        }
        Partition pA = map.get(a);
        Partition pB = map.get(b);

        if (pA == null) {
            if (pB == null) {
                Partition p = new Partition();
                map.put(a, p);
                map.put(b, p);
                return true;
            } else {
                map.put(a, pB);
                return true;
            }
        } else if (pB == null) {
            map.put(b, pA);
            return true;
        } else {
            return pA.merge(pB);
        }
    }

    @Override
    public String toString() {
        Map<T, T> reps = getRepresentatives(map.keySet(), Ordering.arbitrary());
        return reps.toString();
    }

    /**
     * 
     * @param variables
     * @param comp
     * @return
     */
    public ImmutableMap<T, T> getRepresentatives(final Set<T> keys,
            final Comparator<? super T> comp) {

        final ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        final Map<Partition, List<T>> partitions = Maps.newHashMap();

        for (T key : keys) {
            final Partition partition = map.get(key);
            if (partition != null) {
                Partition representative = partition.representative();
                List<T> list = partitions.get(representative);
                if (list == null) {
                    partitions.put(representative, list = Lists.newArrayList());
                }
                list.add(key);
            } else {
                /* singleton */
                builder.put(key, key);
            }
        }

        for (List<T> klass : partitions.values()) {
            final T min = Collections.min(klass, comp);
            for (T t : klass) {
                builder.put(t, min);
            }
        }
        return builder.build();
    }
}
