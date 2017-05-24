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

/**
 * Created by José
 */
public class CrossProductOrderedSpliterator<E> implements Spliterator<Map.Entry<E, E>> {

    private Spliterator<E> spliterator;
    private List<E> buffer = new ArrayList<>();

    private final Function<Consumer<? super Map.Entry<E, E>>, BiConsumer<E, E>> function;

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
                });
    }

    public static <E> CrossProductOrderedSpliterator<E> noDoubles(Spliterator<E> spliterator) {
        return new CrossProductOrderedSpliterator<>(
                spliterator,
                a -> (e1, e2) -> {
                    if (!e1.equals(e2)) {
                        a.accept(new AbstractMap.SimpleImmutableEntry<>(e1, e2));
                        a.accept(new AbstractMap.SimpleImmutableEntry<>(e2, e1));
                    }
                });
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
                }
        );
    }

    private CrossProductOrderedSpliterator(
            Spliterator<E> spliterator,
            Function<Consumer<? super Map.Entry<E, E>>, BiConsumer<E, E>> function) {

        this.spliterator = spliterator;
        this.function = function;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Map.Entry<E, E>> action) {

        BiConsumer<E, E> biConsumer = function.apply(action);

        return spliterator.tryAdvance(
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
        return (estimateSize == Long.MAX_VALUE) || (estimateSize * estimateSize / 2 < estimateSize) ?
                Long.MAX_VALUE : estimateSize * estimateSize / 2;
    }

    @Override
    public int characteristics() {
        return this.spliterator.characteristics() & ~Spliterator.SORTED;
    }
}
