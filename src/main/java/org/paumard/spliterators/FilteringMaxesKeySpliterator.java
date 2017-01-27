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
import java.util.stream.Stream;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * @author José
 */
public class FilteringMaxesKeySpliterator<E> implements Spliterator<E> {

    private final Spliterator<E> spliterator;
    private final Comparator<? super E> comparator;
    private final int numberOfMaxes;

    public static <E> FilteringMaxesKeySpliterator<E> of(
            Spliterator<E> spliterator,
            int numberOfMaxes,
            Comparator<? super E> comparator) {
        Objects.requireNonNull(spliterator);
        Objects.requireNonNull(comparator);
        if (numberOfMaxes < 2) {
            throw new IllegalArgumentException("numberOfMaxes should not be less than 2?");
        }

        return new FilteringMaxesKeySpliterator<>(spliterator, numberOfMaxes, comparator);
    }

    private FilteringMaxesKeySpliterator(
            Spliterator<E> spliterator,
            int numberOfMaxes,
            Comparator<? super E> comparator) {
        this.spliterator = spliterator;
        this.numberOfMaxes = numberOfMaxes;
        this.comparator = comparator;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {

        boolean hasMore = true;
        InsertionTab<E> insertionTab = new InsertionTab<>(this.numberOfMaxes, this.comparator);
        while (hasMore) {
            hasMore = spliterator.tryAdvance(insertionTab);
        }

        Stream<E> result = insertionTab.getResult();
        result.forEach(action);

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

    private static class InsertionTab<T> implements Consumer<T> {

        private final T[] tab;
        private final Map<T, Stream.Builder<T>> map;
        private int currentIndex;
        private final Comparator<? super T> comparator;

        @SuppressWarnings("unchecked")
        public InsertionTab(int maxN, Comparator<? super T> comparator) {
            this.comparator = comparator;
            this.map = new TreeMap<>(comparator);
            this.tab = (T[]) Array.newInstance(Object.class, maxN);
            this.currentIndex = 0;
        }

        public void accept(T t) {
            if (currentIndex < tab.length) {
                insertInDecreasingOrder(tab, t, currentIndex);
                addToResults(map, t);
                currentIndex++;
                return;
            }
            int compare = comparator.compare(tab[tab.length - 1], t);
            if (compare < 0) {
                addToResults(map, t);
                if (comparator.compare(tab[tab.length - 1], tab[tab.length - 2]) != 0) {
                    removeFromResults(map, tab[tab.length - 1]);
                }
                insertInDecreasingOrder(tab, t, tab.length - 1);
            } else if (compare > 0) {
                removeFromResults(map, t);
            } else if (compare == 0) {
                map.get(t).add(t);
            }
        }

        private void insertInDecreasingOrder(T[] tab, T t, int maxIndex) {
            int index = 0;
            while(index < maxIndex) {
                if (comparator.compare(tab[index], t) > 0) {
                    index++;
                } else {
                    for (int i = maxIndex ; i > index ; i--) {
                        tab[i] = tab[i - 1];
                    }
                    break;
                }
            }
            tab[index] = t;
        }

        private void removeFromResults(Map<T, Stream.Builder<T>> map, T key) {
            map.remove(key);
        }

        private void addToResults(Map<T, Stream.Builder<T>> map, T t) {
            map.computeIfAbsent(t, key -> Stream.builder()).add(t);
        }

        public Stream<T> getResult() {
            return map.entrySet().stream().flatMap(entry -> entry.getValue().build());
        }
    }
}