/*
 * Copyright (C) 2016 José Paumard
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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by José
 */
public class CrossProductOrderedSpliterator<E> implements Spliterator<Map.Entry<E, E>> {

    private Spliterator<E> spliterator;
    private List<E> buffer = new ArrayList<>();
    private UnaryOperator<Long> estimateSize;

    private final Function<Consumer<? super Map.Entry<E, E>>, BiConsumer<E, E>> function;
    private boolean hasMore = true;
    private Iterator<Map.Entry<E, E>> iterator;
    private boolean consumingIterator = false;

    public static <E> CrossProductOrderedSpliterator<E> ordered(Spliterator<E> spliterator, Comparator<E> comparator) {
        return new CrossProductOrderedSpliterator<>(
                spliterator,
                a -> (e1, e2) -> {
                    int compare = comparator.compare(e2, e1);
                    if (compare > 0) {
                        a.accept(new AbstractMap.SimpleImmutableEntry<>(e1, e2));
                    } else if (compare < 0) {
                        a.accept(new AbstractMap.SimpleImmutableEntry<>(e2, e1));
                    }
                },
                estimateSize -> (estimateSize == Long.MAX_VALUE) || (estimateSize * (estimateSize - 1) / 2 < estimateSize) ?
                        Long.MAX_VALUE : estimateSize * (estimateSize - 1) / 2);
    }

    public static <E> CrossProductOrderedSpliterator<E> noDoubles(Spliterator<E> spliterator) {
        return new CrossProductOrderedSpliterator<>(
                spliterator,
                a -> (e1, e2) -> {
                    if (!e1.equals(e2)) {
                        a.accept(new AbstractMap.SimpleImmutableEntry<>(e1, e2));
                        a.accept(new AbstractMap.SimpleImmutableEntry<>(e2, e1));
                    }
                },
                estimateSize -> (estimateSize == Long.MAX_VALUE) || (estimateSize * (estimateSize - 1) < estimateSize) ?
                        Long.MAX_VALUE : (estimateSize * (estimateSize - 1)));
    }

    public static <E> CrossProductOrderedSpliterator<E> of(Spliterator<E> spliterator) {
        return new CrossProductOrderedSpliterator<>(
                spliterator,
                a -> (e1, e2) -> {
                    if (e1.equals(e2)) {
                        a.accept(new AbstractMap.SimpleImmutableEntry<>(e1, e2));
                    } else {
                        a.accept(new AbstractMap.SimpleImmutableEntry<>(e1, e2));
                        a.accept(new AbstractMap.SimpleImmutableEntry<>(e2, e1));
                    }
                },
                estimateSize -> (estimateSize == Long.MAX_VALUE) || (estimateSize * estimateSize < estimateSize) ?
                        Long.MAX_VALUE : estimateSize * estimateSize
        );
    }

    private CrossProductOrderedSpliterator(
            Spliterator<E> spliterator,
            Function<Consumer<? super Map.Entry<E, E>>, BiConsumer<E, E>> function,
            UnaryOperator<Long> estimateSize) {

        this.spliterator = spliterator;
        this.function = function;
        this.estimateSize = estimateSize;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Map.Entry<E, E>> action) {

        Stream.Builder<Map.Entry<E, E>> builder = Stream.builder();
        if (consumingIterator) {
            if (iterator.hasNext()) {
                action.accept(iterator.next());
                return true;
            } else {
                consumingIterator = false;
            }
        }
        if (hasMore) {
            fillBuilder(builder);
            consumingIterator = true;
        }

        List<Map.Entry<E, E>> entryList = builder.build().collect(Collectors.toList());
        while (entryList.isEmpty() && hasMore) {
            builder = Stream.builder();
            fillBuilder(builder);
            entryList = builder.build().collect(Collectors.toList());
        }
        iterator = entryList.iterator();
        if (iterator.hasNext()) {
            action.accept(iterator.next());
            return true;
        }

        return false;
    }

    private void fillBuilder(Stream.Builder<Map.Entry<E, E>> builder) {
        BiConsumer<E, E> biConsumer = function.apply(builder::add);
        hasMore = spliterator.tryAdvance(
                e1 -> {
                    buffer.add(e1);
                    buffer.forEach(e2 -> biConsumer.accept(e1, e2));
                }
        );
    }

    @Override
    public Spliterator<Map.Entry<E, E>> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        long estimateSize = this.spliterator.estimateSize();
        return this.estimateSize.apply(estimateSize);
    }

    @Override
    public int characteristics() {
        return this.spliterator.characteristics() & ~Spliterator.SORTED;
    }
}
