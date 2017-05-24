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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.paumard.streams.StreamsUtils.roll;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * @author José
 */
public class WeavingSpliterator<E> implements Spliterator<E> {

    private final Spliterator<E>[] spliterators;
    private final ArrayDeque<E> elements = new ArrayDeque<>();
    private boolean firstGroup = true;
    private boolean moreElements;

    @SafeVarargs
    public static <E> WeavingSpliterator<E> of(Spliterator<E>... spliterators) {
        Objects.requireNonNull(spliterators);
        if (spliterators.length < 2) {
            throw new IllegalArgumentException("Why would you weave less than 2 spliterators?");
        }
        if (Stream.of(spliterators).mapToInt(Spliterator::characteristics).reduce(Spliterator.ORDERED, (i1, i2) -> i1 & i2) == 0) {
            throw new IllegalArgumentException("Why would you want to traverse non ordered spliterators?");
        }

        return new WeavingSpliterator<>(spliterators);
    }

    @SafeVarargs
    private WeavingSpliterator(Spliterator<E>... spliterators) {
        this.spliterators = spliterators;
    }

    private void consumeOneElementOnEachSpliterator() {
        Deque<E> elementsWave = new ArrayDeque<>();
        moreElements = true;
        for (int i = 0; i < spliterators.length && moreElements; i++) {
            moreElements = spliterators[i].tryAdvance(elementsWave::addLast);
        }
        if (moreElements) {
            elements.addAll(elementsWave);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        if (firstGroup) {
            consumeOneElementOnEachSpliterator();
            firstGroup = false;
        }
        if (!elements.isEmpty() && moreElements) {
            action.accept(elements.pop());
            return moreElements;
        }
        if (moreElements) {
            consumeOneElementOnEachSpliterator();
        }
        if (!elements.isEmpty() && moreElements) {
            action.accept(elements.pop());
            return moreElements;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<E> trySplit() {
        WeavingSpliterator<E>[] splitSpliterators = Stream.of(spliterators).map(Spliterator::trySplit).toArray(WeavingSpliterator[]::new);
        return Stream.of(splitSpliterators).noneMatch(Objects::isNull) ? new WeavingSpliterator<>(splitSpliterators) : null;
    }

    @Override
    public long estimateSize() {
        return (hasMaxValueSize() || hasSumOverflowed()) ? Long.MAX_VALUE : Stream.of(spliterators).mapToLong(Spliterator::estimateSize).sum();
    }

    private boolean hasSumOverflowed() {
        Stream<Stream<Long>> rolledStream = roll(Stream.of(spliterators).map(Spliterator::estimateSize), 2);
        return rolledStream.map(stream -> stream.collect(toList())).allMatch(list -> list.get(0) < list.get(1));
    }

    private boolean hasMaxValueSize() {
        return Stream.of(spliterators).mapToLong(Spliterator::estimateSize).anyMatch(l -> l == Long.MAX_VALUE);
    }

    @Override
    public int characteristics() {
        return Stream.of(spliterators)
                .mapToInt(Spliterator::characteristics)
                .reduce(0xFFFFFFFF, (i1, i2) -> i1 & i2)
                & ~Spliterator.SORTED;
    }
}