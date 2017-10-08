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

import org.paumard.spliterators.util.TryAdvanceCheckingSpliterator;
import org.paumard.streams.StreamsUtils;
import org.testng.annotations.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by José
 */
public class ValidatingSpliteratorTest {

    @Test
    public void should_validate_empty_streams_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.empty();
        Predicate<String> validator = String::isEmpty;

        // When
        Stream<String> validateStream =
                StreamsUtils.validate(strings, validator, Function.identity(), Function.identity());
        long count = validateStream.count();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void should_validated_a_stream_correctly() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three");
        Predicate<String> validator = s -> s.length() == 3;
        Function<String, String> transformIfValid = String::toUpperCase;
        Function<String, String> transformIfNotValid = s -> "-";

        // When
        Stream<String> validateStream =
                StreamsUtils.validate(strings, validator, transformIfValid, transformIfNotValid);
        List<String> list = validateStream.collect(toList());

        // Then
        assertThat(list.size()).isEqualTo(3);
        assertThat(list).containsExactly("ONE", "TWO", "-");
    }

    @Test
    public void should_validate_a_sorted_stream_correctly_and_in_a_sorted_stream() {
        // Given
        Stream<String> sortedStream = new TreeSet<>(Arrays.asList("a", "b", "c")).stream();
        Predicate<String> validator = s -> s.length() == 3;
        Function<String, String> transformIfValid = String::toUpperCase;
        Function<String, String> transformIfNotValid = s -> "-";

        // When
        Stream<String> stream =
                StreamsUtils.validate(sortedStream, validator, transformIfValid, transformIfNotValid);

        // Then
        assertThat(stream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three");
        Predicate<String> validator = s -> s.length() == 3;
        Function<String, String> transformIfValid = String::toUpperCase;
        Function<String, String> transformIfNotValid = s -> "-";

        Stream<String> testedStream =
                StreamsUtils.validate(strings, validator, transformIfValid, transformIfNotValid);
        TryAdvanceCheckingSpliterator<String> spliterator = new TryAdvanceCheckingSpliterator<>(testedStream.spliterator());
        Stream<String> monitoredStream = StreamSupport.stream(spliterator, false);

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(3L);
    }
}