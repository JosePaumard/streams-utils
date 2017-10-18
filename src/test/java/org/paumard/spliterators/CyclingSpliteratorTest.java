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
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.paumard.streams.StreamsUtils.cycle;
import static org.paumard.streams.StreamsUtils.zip;

/**
 * Created by José
 */
public class CyclingSpliteratorTest {

    @Test
    public void should_cycle_an_empty_stream_into_a_stream_of_streams() {
        // Given
        Stream<String> strings = Stream.empty();

        // When
        long n = StreamSupport.stream(CyclingSpliterator.of(strings.spliterator()), false).limit(10).count();

        // Then
        assertThat(n).isEqualTo(10);
    }

    @Test
    public void should_cycle_an_empty_stream_into_a_stream_of_empty_streams() {
        // Given
        Stream<String> strings = Stream.empty();

        // When
        Stream<String> stream = StreamSupport.stream(CyclingSpliterator.of(strings.spliterator()), false).limit(10).flatMap(identity());
        // Then
        assertThat(stream.count()).isEqualTo(0);
    }

    @Test
    public void should_cycle_a_non_empty_stream_into_a_stream_of_streams() {
        // Given
        Stream<String> strings = Stream.of("one", "two");

        // When
        long n = StreamSupport.stream(CyclingSpliterator.of(strings.spliterator()), false).limit(10).count();
        // Then
        assertThat(n).isEqualTo(10);
    }

    @Test
    public void should_cycle_a_non_empty_stream_into_a_stream_of_non_empty_streams() {
        // Given
        Stream<String> strings = Stream.of("one", "two");

        // When
        List<String> list = StreamSupport.stream(CyclingSpliterator.of(strings.spliterator()), false).limit(3).flatMap(identity()).collect(toList());
        // Then
        assertThat(list).containsExactly("one", "two", "one", "two", "one", "two");
    }

    @Test
    public void should_be_able_to_cycle_a_sorted_stream_in_an_non_sorted_cycle_stream() {
        // Given
        SortedSet<String> sortedSet = new TreeSet<>(Arrays.asList("one", "two", "three"));

        // When
        Stream<String> stream = StreamsUtils.cycle(sortedSet.stream());

        // Then
        assertThat(stream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test
    public void should_not_build_a_cycling_spliterator_on_a_null_spliterator() {
        // Then
        assertThatNullPointerException().isThrownBy(() -> CyclingSpliterator.of(null));
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three");
        Stream<String> cyclingStream = StreamsUtils.cycle(strings).limit(2);
        TryAdvanceCheckingSpliterator<String> spliterator = new TryAdvanceCheckingSpliterator<>(cyclingStream.spliterator());
        Stream<String> monitoredStream = StreamSupport.stream(spliterator, false);

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    public void should_solve_fizzbuzz_correctly() {

        // When
        Stream<String> fizzBuzz =
                zip(
                        IntStream.range(0, 101).boxed(),
                        zip(
                                cycle(Stream.of("fizz", "", "")),
                                cycle(Stream.of("buzz", "", "", "", "")),
                                String::concat
                        ),
                        (i, string) -> string.isEmpty() ? i.toString() : string
                );
        List<String> fizzBuzzList = fizzBuzz.skip(1).limit(20).collect(toList());

        // Then
        assertThat(fizzBuzzList).containsExactly(
                "1", "2", "fizz", "4", "buzz", "fizz", "7", "8", "fizz",
                "buzz", "11", "fizz", "13", "14", "fizzbuzz", "16", "17", "fizz", "19",
                "buzz");
    }
}