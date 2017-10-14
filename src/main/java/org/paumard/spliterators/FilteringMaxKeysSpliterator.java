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

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * @author José
 */
public class FilteringMaxKeysSpliterator<E> implements Spliterator<E> {

    private final Spliterator<E> spliterator;
    private final Comparator<? super E> comparator;
    private final int numberOfMaxes;
    private boolean hasMore = true;
    private Iterator<E> maxes;
    private boolean maxesBuilt = false;

    public static <E> FilteringMaxKeysSpliterator<E> of(
            Spliterator<E> spliterator,
            int numberOfMaxes,
            Comparator<? super E> comparator) {
        Objects.requireNonNull(spliterator);
        Objects.requireNonNull(comparator);
        if (numberOfMaxes < 2) {
            throw new IllegalArgumentException("numberOfMaxes should not be less than 2?");
        }

        return new FilteringMaxKeysSpliterator<>(spliterator, numberOfMaxes, comparator);
    }

    private FilteringMaxKeysSpliterator(
            Spliterator<E> spliterator,
            int numberOfMaxes,
            Comparator<? super E> comparator) {
        this.spliterator = spliterator;
        this.numberOfMaxes = numberOfMaxes;
        this.comparator = comparator;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {

        InsertionTab<E> insertionTab = new InsertionTab<>(this.numberOfMaxes, this.comparator);
        while (hasMore) {
            hasMore = spliterator.tryAdvance(insertionTab);
        }

        if (!maxesBuilt) {
            maxes = insertionTab.getResult().collect(Collectors.toList()).iterator();
            maxesBuilt = true;
        }
        if (maxesBuilt && maxes.hasNext()) {
            action.accept(maxes.next());
            return true;
        }

        return false;
    }

    @Override
    public Spliterator<E> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        long maxSize = spliterator.estimateSize();
        return maxSize >= numberOfMaxes ? numberOfMaxes : maxSize;
    }

    @Override
    public int characteristics() {
        return spliterator.characteristics();
    }

    @Override
    public Comparator<? super E> getComparator() {
        return this.spliterator.getComparator();
    }

    private static class InsertionTab<T> implements Consumer<T> {

        private final T[] tab;
        private int currentIndex;
        private final Comparator<? super T> comparator;

        @SuppressWarnings("unchecked")
        public InsertionTab(int maxN, Comparator<? super T> comparator) {
            this.comparator = comparator;
            this.tab = (T[]) Array.newInstance(Object.class, maxN);
            this.currentIndex = 0;
        }

        public void accept(T t) {
            if (currentIndex < tab.length) {
                insertInDecreasingOrder(tab, t, currentIndex);
                currentIndex++;
                return;
            }
            if (tab[tab.length - 1] == null || comparator.compare(tab[tab.length - 1], t) < 0) {
                insertInDecreasingOrder(tab, t, tab.length - 1);
            }
        }

        private void insertInDecreasingOrder(T[] tab, T t, int maxIndex) {
            int index = 0;
            while(index < maxIndex) {
                if (comparator.compare(tab[index], t) > 0) {
                    index++;
                } else if (comparator.compare(tab[index], t) < 0){
                    for (int i = maxIndex ; i > index ; i--) {
                        tab[i] = tab[i - 1];
                    }
                    break;
                } else {
                    break;
                }
            }
            tab[index] = t;
        }

        private Stream<T> getResult() {
            return StreamsUtils.interrupt(Arrays.stream(tab), Objects::isNull);
        }
    }
}