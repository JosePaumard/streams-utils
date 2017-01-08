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
public class GroupingOnGatingSpliterator<E> implements Spliterator<Stream<E>> {

    private final Spliterator<E> spliterator;
    private final Predicate<? super E> open;
    private final Predicate<? super E> close;
    private final boolean openingElementIncluded;
    private final boolean closingElementIncluded;

    private Stream.Builder<E> builder = Stream.builder();
    private boolean gateOpen = false;
    private boolean groupReady = false;
    private boolean builderEmpty = true;

    public static <E> GroupingOnGatingSpliterator<E> of(
            Spliterator<E> spliterator,
            Predicate<? super E> open, boolean openingElementIncluded,
            Predicate<? super E> close, boolean closingElementIncluded) {
        Objects.requireNonNull(spliterator);
        Objects.requireNonNull(open);
        Objects.requireNonNull(close);

        if ((spliterator.characteristics() & Spliterator.ORDERED) == 0) {
            throw new IllegalArgumentException("Why would you build a grouping on gating spliterator on a non-ordered spliterator?");
        }

        return new GroupingOnGatingSpliterator<E>(spliterator, open, openingElementIncluded, close, closingElementIncluded);
    }

    private GroupingOnGatingSpliterator(
            Spliterator<E> spliterator,
            Predicate<? super E> open, boolean openingElementIncluded,
            Predicate<? super E> close, boolean closingElementIncluded) {
        this.spliterator = spliterator;
        this.open = open;
        this.openingElementIncluded = openingElementIncluded;
        this.close = close;
        this.closingElementIncluded = closingElementIncluded;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Stream<E>> action) {

        boolean hasMore = true;
        while (hasMore && !groupReady) {
            hasMore = spliterator.tryAdvance(e -> {
                if (gateOpen && !close.test(e)) {
                    builder.add(e);
                    builderEmpty = false;
                }
                if (!gateOpen && open.test(e)) {
                    gateOpen = true;
                    if (openingElementIncluded) {
                        builder.add(e);
                        builderEmpty = false;
                    }
                }
                if (gateOpen && close.test(e)) {
                    if (closingElementIncluded) {
                        builder.add(e);
                        builderEmpty = false;
                    }
                    gateOpen = false;
                    groupReady = true;
                }
            });
        }

        if ((groupReady || !hasMore) && !builderEmpty) {
            action.accept(builder.build());
            builder = Stream.builder();
            builderEmpty = true;
            groupReady = false;
        }


        return hasMore;
    }

    @Override
    public Spliterator<Stream<E>> trySplit() {
        Spliterator<E> splitSpliterator = this.spliterator.trySplit();
        return splitSpliterator == null ? null : new GroupingOnGatingSpliterator<>(splitSpliterator, open, openingElementIncluded, close, closingElementIncluded);
    }

    @Override
    public long estimateSize() {
        return 0L;
    }

    @Override
    public int characteristics() {
        // this spliterator is already ordered
        return spliterator.characteristics();
    }
}