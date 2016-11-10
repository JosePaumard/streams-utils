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

import org.paumard.streams.StreamsUtils;

import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * @author José
 */
public class LimitingAtMostSpliterator<E> implements Spliterator<E> {

    private final Spliterator<E> spliterator;
    private final AtomicBoolean hasMore = new AtomicBoolean(true);
    private final AtomicLong number = new AtomicLong(0L);
    private final long limit;

    public static <E> LimitingAtMostSpliterator<E> of(Spliterator<E> spliterator, long limit) {
        Objects.requireNonNull(spliterator);
        if (limit < 0L) {
            throw new IllegalArgumentException("Limit should be greater than 0.");
        }

        return new LimitingAtMostSpliterator<E>(spliterator, limit);
    }

    private LimitingAtMostSpliterator(Spliterator<E> spliterator, long limit) {
        this.spliterator = spliterator;
        this.limit = limit;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {

        if (hasMore.get()) {
            return spliterator.tryAdvance(e -> {
                if (hasMore.get() && number.incrementAndGet() <= limit) {
                    action.accept(e);
                } else {
                    hasMore.set(false);
                }
            });
        } else {
            return false;
        }
    }

    @Override
    public Spliterator<E> trySplit() {
        Spliterator<E> split = this.spliterator.trySplit();
        return split == null ? null : new LimitingAtMostSpliterator<>(split, limit);
    }

    @Override
    public long estimateSize() {
        return limit - number.get();
    }

    @Override
    public int characteristics() {
        return this.spliterator.characteristics() & ~Spliterator.SIZED & ~Spliterator.SUBSIZED;
    }
}