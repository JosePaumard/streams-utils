/*
 * Copyright (C) 2017 José Paumard
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

package org.paumard.spliterators.util;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TryAdvanceCheckingSpliterator<T> implements Spliterator<T> {

        private Spliterator<T> spliterator;

        public TryAdvanceCheckingSpliterator(Spliterator<T> spliterator) {
            this.spliterator = spliterator;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            AtomicBoolean hasBeenCalled = new AtomicBoolean(false);

            Consumer<T> consumer = t -> {
                if (hasBeenCalled.getAndSet(true)) {
                    throw new IllegalStateException("Double call on the passed consumer");
                } else {
                    action.accept(t);
                }
            };


            boolean hasMore = this.spliterator.tryAdvance(consumer);
            if (!hasMore && hasBeenCalled.get()) {
                throw new IllegalStateException("The passed consumer has been called and the returned value is false");
            }
            if (hasMore && !hasBeenCalled.get()) {
                throw new IllegalStateException("The passed consumer has not been called and the returned value is true");
            }
            return hasMore;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return this.spliterator.estimateSize();
        }

        @Override
        public int characteristics() {
            return this.spliterator.characteristics();
        }

    @Override
    public Comparator<? super T> getComparator() {
        return this.spliterator.getComparator();
    }
}