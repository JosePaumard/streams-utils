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
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * Created by José
 */
public class RepeatingSpliterator<E> implements Spliterator<E> {

    private final int repeating;
    private final Spliterator<E> spliterator;

    public static <E> RepeatingSpliterator<E> of(Spliterator<E> spliterator, int repeating) {
        Objects.requireNonNull(spliterator);
        if (repeating <= 1) {
            throw new IllegalArgumentException (("Why would you build a repeating spliterator with a repeating factor of less than 2?"));
        }
        if ((spliterator.characteristics() & Spliterator.SIZED) == 0) {
            throw new IllegalArgumentException (("Why would you try to repeat a non-SIZED spliterator?"));
        }

        return new RepeatingSpliterator<>(spliterator, repeating);
    }

    private RepeatingSpliterator(Spliterator<E> spliterator, int repeating) {
        this.spliterator = spliterator;
        this.repeating = repeating;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        boolean hasMore = spliterator.tryAdvance(
                e -> IntStream.range(0, repeating).forEach(i -> action.accept(e))
        );

        return hasMore;
    }

    @Override
    public Spliterator<E> trySplit() {
        Spliterator<E> splitSpliterator = spliterator.trySplit();
        return splitSpliterator == null ? null : new RepeatingSpliterator<>(splitSpliterator, repeating);
    }

    @Override
    public long estimateSize() {
        long estimateSize = spliterator.estimateSize();
        return (estimateSize == Long.MAX_VALUE) || (estimateSize*repeating < estimateSize) ? Long.MAX_VALUE : estimateSize*repeating;
    }

    @Override
    public int characteristics() {
        return this.spliterator.characteristics() | Spliterator.ORDERED;
    }
}
