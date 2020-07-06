/**
 * Copyright 2019 Equipment & Tool Institute
 */

package org.etools.j1939_84.bus;

import java.util.Optional;
import java.util.function.Function;

public class Either<L, R> {
    static public <L, R> Either<L, R> nullable(L l, R r) {
        return new Either<>(Optional.ofNullable(l), Optional.ofNullable(r));
    }

    final public Optional<L> left;

    final public Optional<R> right;

    public Either(L left, R right) {
        this(Optional.ofNullable(left), Optional.ofNullable(right));
    }

    private Either(Optional<L> l, Optional<R> r) {
        if (!l.isPresent() ^ r.isPresent()) {
            throw new IllegalArgumentException("Either one must be null.");
        }
        left = l;
        right = r;
    }

    @SuppressWarnings("unchecked")
    public <C> C as(Class<C> cls) {
        return resolve(x -> (C) x, x -> (C) x);
    }

    public <A, B> Either<A, B> map(Function<L, A> lFn, Function<R, B> rFn) {
        return new Either<>(left.map(lFn), right.map(rFn));
    }

    public <T> T resolve(Function<L, T> lFn, Function<R, T> rFn) {
        return left.map(lFn).orElseGet(() -> right.map(rFn).orElseThrow());
    }

    @Override
    public String toString() {
        return resolve(l -> "left: " + l.toString(), r -> "right: " + r.toString());
    }
}
