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
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * See the documentation and patterns to be used in this class in the {@link StreamsUtils} factory class.
 *
 * @author José
 */
public class ValidatingSpliterator<E, R> implements Spliterator<R> {

    private final Spliterator<E> spliterator;
    private final Function<E, R> transformIfValid, transformIfNotValid;
    private final Predicate<E> validator;

    public static class Builder<E, R> {

        private Spliterator<E> spliterator;
        private Function<E, R> transformIfValid, transformIfNotValid;
        private Predicate<E> validator;

        public Builder() {
        }

        public Builder<E, R> with(Spliterator<E> spliterator) {
            this.spliterator = Objects.requireNonNull(spliterator);
            return this;
        }

        public Builder<E, R> validatedBy(Predicate<E> validator) {
            this.validator = Objects.requireNonNull(validator);
            return this;
        }

        public Builder<E, R> withValidFunction(Function<E, R> validFunction) {
            this.transformIfValid = Objects.requireNonNull(validFunction);
            return this;
        }

        public Builder<E, R> withNotValidFunction(Function<E, R> notValidFunction) {
            this.transformIfNotValid = Objects.requireNonNull(notValidFunction);
            return this;
        }

        public ValidatingSpliterator<E, R> build() {
            return new ValidatingSpliterator<>(spliterator, validator, transformIfValid, transformIfNotValid);
        }
    }

    private ValidatingSpliterator(
            Spliterator<E> spliterator, Predicate<E> validator,
            Function<E, R> transformIfValid, Function<E, R> transformIfNotValid) {
        this.spliterator = spliterator;
        this.validator = validator;
        this.transformIfValid = transformIfValid;
        this.transformIfNotValid = transformIfNotValid;
    }

    @Override
    public boolean tryAdvance(Consumer<? super R> action) {

        return this.spliterator.tryAdvance(
               e -> {
                   if (validator.test(e)) {
                       action.accept(transformIfValid.apply(e));
                   } else {
                       action.accept(transformIfNotValid.apply(e));
                   }
               }
        );
    }

    @Override
    public Spliterator<R> trySplit() {
        Spliterator<E> split = this.spliterator.trySplit();
        return split == null ? null : new ValidatingSpliterator<>(split, validator, transformIfValid, transformIfNotValid);
    }

    @Override
    public long estimateSize() {
        return spliterator.estimateSize();
    }

    @Override
    public int characteristics() {
        return this.spliterator.characteristics();
    }
}