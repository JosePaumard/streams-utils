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
import java.util.function.Predicate;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * @author José
 */
public class GatingSpliterator<E> implements Spliterator<E> {

    private final Spliterator<E> spliterator;
    private final Predicate<? super E> gate;
    private boolean gateIsOpenned = false;

    public static <E> GatingSpliterator<E> of(Spliterator<E> spliterator, Predicate<? super E> gate) {
        Objects.requireNonNull(spliterator);
        Objects.requireNonNull(gate);

        return new GatingSpliterator<>(spliterator, gate);
    }

    private GatingSpliterator(Spliterator<E> spliterator, Predicate<? super E> gate) {
        this.spliterator = spliterator;
        this.gate = gate;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {

        boolean hasMore = false;
        if (!gateIsOpenned) {
            hasMore = spliterator.tryAdvance(e -> {
                if (gate.test(e)) {
                    gateIsOpenned = true;
                    action.accept(e);
                }
            });
            if (hasMore && gateIsOpenned) {
                return true;
            }
        }
        while (!gateIsOpenned && hasMore) {
            hasMore = spliterator.tryAdvance(e -> {
                if (gate.test(e)) {
                    gateIsOpenned = true;
                    action.accept(e);
                }
            });
            if (hasMore && gateIsOpenned) {
                return true;
            }
        }
        if (gateIsOpenned) {
            hasMore = spliterator.tryAdvance(action);
        }

        return hasMore;
    }

    @Override
    public Spliterator<E> trySplit() {
        Spliterator<E> split = this.spliterator.trySplit();
        return split == null ? null : new GatingSpliterator<>(split, gate);
    }

    @Override
    public long estimateSize() {
        if (gateIsOpenned) {
            return this.spliterator.estimateSize();
        } else {
            return 0;
        }
    }

    @Override
    public int characteristics() {
        return this.spliterator.characteristics() & ~Spliterator.SIZED & ~Spliterator.SUBSIZED;
    }

    @Override
    public Comparator<? super E> getComparator() {
        return this.spliterator.getComparator();
    }
}