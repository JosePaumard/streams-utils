/*
 * Copyright (C) 2015 José Paumard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.paumard.spliterators;

import org.paumard.streams.StreamsUtils;

import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * Created by José
 */
public class TraversingSpliterator<E> implements Spliterator<Stream<E>> {

    private final Spliterator<E>[] spliterators;
    private final AtomicBoolean firstGroup = new AtomicBoolean(true);

    @SafeVarargs
    public static <E> TraversingSpliterator<E> of(Spliterator<E>... spliterators) {
        Objects.requireNonNull(spliterators);
        if (spliterators.length < 2) {
            throw new IllegalArgumentException ("Why would you want to traverse less than two streams?");
        }
        if (Stream.of(spliterators).mapToInt(Spliterator::characteristics).reduce(Spliterator.ORDERED, (i1, i2) -> i1 & i2) == 0) {
            throw new IllegalArgumentException ("Why would you want to traverse non ordered spliterators?");
        }

        return new TraversingSpliterator<>(spliterators);
    }

    @SafeVarargs
    private TraversingSpliterator(Spliterator<E>... spliterators) {
        this.spliterators = spliterators;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Stream<E>> action) {
        Stream.Builder<E> builder = Stream.builder();
        boolean hasMore = true;
        for (Spliterator<E> spliterator : spliterators) {
            hasMore &= spliterator.tryAdvance(builder::add);
        }
        if (hasMore) {
            action.accept(builder.build());
            firstGroup.getAndSet(false);
        }
        if (!hasMore && firstGroup.getAndSet(false))
            action.accept(Stream.<E>empty());
        return hasMore;
    }


    @Override
    public Spliterator<Stream<E>> trySplit() {
        @SuppressWarnings("unchecked")
        TraversingSpliterator<E>[] spliterators =
                Stream.of(this.spliterators).map(Spliterator::trySplit).toArray(TraversingSpliterator[]::new);
        return new TraversingSpliterator<>((Spliterator<E>[]) spliterators);
    }

    @Override
    public long estimateSize() {
        return Stream.of(spliterators).mapToLong(Spliterator::estimateSize).min().getAsLong();
    }

    @Override
    public int characteristics() {
        return Stream.of(spliterators).mapToInt(Spliterator::characteristics).reduce(0xFFFFFFFF, (i1, i2) -> i1 & i2);
    }
}