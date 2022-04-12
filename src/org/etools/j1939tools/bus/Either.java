/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939tools.bus;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class Either<L, R> {
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

    static public <L, R> Either<L, R> nullable(L l, R r) {
        return new Either<>(Optional.ofNullable(l), Optional.ofNullable(r));
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Either) {
            Either<?, ?> that = (Either<?, ?>) o;
            return left.equals(that.left) && right.equals(that.right);
        }
        return false;
    }

    @Override
    public String toString() {
        return resolve(l -> "left: " + l.toString(), r -> "right: " + r.toString());
    }

    public <A, B> Either<A, B> map(Function<L, A> lFn, Function<R, B> rFn) {
        return new Either<>(left.map(lFn), right.map(rFn));
    }

    @SuppressWarnings("unchecked")
    public <T> T resolve() {
        return left.map(x -> (T) x).orElseGet(() -> right.map(x -> (T) x).orElseThrow());
    }

    public <T> T resolve(Function<L, T> lFn, Function<R, T> rFn) {
        return left.map(lFn).orElseGet(() -> right.map(rFn).orElseThrow());
    }
}
