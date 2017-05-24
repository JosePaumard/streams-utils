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
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * @author José
 */
public class GroupingOnSplittingSpliterator<E> implements Spliterator<Stream<E>> {

    private final Spliterator<E> spliterator;
    private final Predicate<? super E> splitter;
    private final boolean included;

    private Stream.Builder<E> currentBuidler = Stream.builder();
    private Stream.Builder<E> nextBuilder = Stream.builder();
    private boolean gateOpen = false;
    private boolean groupReady = false;
    private boolean builderReady = true;

    public static <E> GroupingOnSplittingSpliterator<E> of(
            Spliterator<E> spliterator,
            Predicate<? super E> splitter, boolean included) {
        Objects.requireNonNull(spliterator);
        Objects.requireNonNull(splitter);

        if ((spliterator.characteristics() & Spliterator.ORDERED) == 0) {
            throw new IllegalArgumentException("Why would you build a grouping on gating spliterator on a non-ordered spliterator?");
        }

        return new GroupingOnSplittingSpliterator<E>(spliterator, splitter, included);
    }

    private GroupingOnSplittingSpliterator(
            Spliterator<E> spliterator,
            Predicate<? super E> splitter, boolean included) {
        this.spliterator = spliterator;
        this.splitter = splitter;
        this.included = included;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Stream<E>> action) {

        boolean hasMore = true;
        while (hasMore && !groupReady) {
            hasMore = spliterator.tryAdvance(e -> {
                if (gateOpen && splitter.test(e)) {
                    if (included) {
                        nextBuilder.add(e);
                    }
                    groupReady = true;
                }
                if (gateOpen && !splitter.test(e)) {
                    currentBuidler.add(e);
                    builderReady = false;
                }
                if (!gateOpen && splitter.test(e)) {
                    gateOpen = true;
                    if (included) {
                        currentBuidler.add(e);
                    }
                    builderReady = false;
                }
            });
        }

        if ((groupReady || !hasMore) && !builderReady) {
            action.accept(currentBuidler.build());
            currentBuidler = nextBuilder;
            nextBuilder = Stream.builder();
            groupReady = false;
        }


        return hasMore;
    }

    @Override
    public Spliterator<Stream<E>> trySplit() {
        Spliterator<E> splitSpliterator = this.spliterator.trySplit();
        return splitSpliterator == null ? null : new GroupingOnSplittingSpliterator<>(splitSpliterator, splitter, included);
    }

    @Override
    public long estimateSize() {
        return 0L;
    }

    @Override
    public int characteristics() {
        // this spliterator is already ordered
        return spliterator.characteristics() & ~Spliterator.SORTED;
    }
}