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

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * @author José
 */
public class FilteringAllMaxSpliterator<E> implements Spliterator<E> {

    private final Spliterator<E> spliterator;
    private final Comparator<? super E> comparator;

    private Stream.Builder<E> builder = Stream.builder();
    private E currentMax;

    public static <E> FilteringAllMaxSpliterator<E> of(
            Spliterator<E> spliterator,
            Comparator<? super E> comparator) {
        Objects.requireNonNull(spliterator);
        Objects.requireNonNull(comparator);

        return new FilteringAllMaxSpliterator<>(spliterator, comparator);
    }

    private FilteringAllMaxSpliterator(
            Spliterator<E> spliterator,
            Comparator<? super E> comparator) {
        this.spliterator = spliterator;
        this.comparator = comparator;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {

        boolean hasMore = true;
        while (hasMore) {
            hasMore = spliterator.tryAdvance(e -> {
                if (currentMax == null) {
                    currentMax = e;
                    builder.add(e);
                } else if (comparator.compare(currentMax, e) == 0) {
                    builder.add(e);
                } else if (comparator.compare(currentMax, e) < 0) {
                    currentMax = e;
                    builder = Stream.builder();
                    builder.add(e);
                }
            });
        }

        builder.build().forEach(action);
        return false;
    }

    @Override
    public Spliterator<E> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 0L;
    }

    @Override
    public int characteristics() {
        return spliterator.characteristics();
    }

    @Override
    public Comparator<? super E> getComparator() {
        return this.spliterator.getComparator();
    }
}