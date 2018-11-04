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

import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by José
 */
public class ZippingSpliteratorTest {

    @Test
    public void should_zip_empty_streams_into_a_stream_of_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.empty();
        Stream<Integer> ints = Stream.empty();
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;

        // When
        Stream<String> zippedStream = StreamsUtils.zip(strings, ints, zip);
        long count = zippedStream.count();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void should_zip_two_correct_streams_together() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three");
        Stream<Integer> ints = Stream.of(1, 2, 3);
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;

        // When
        Stream<String> zippedStream = StreamsUtils.zip(strings, ints, zip);
        List<String> list = zippedStream.collect(toList());

        // Then
        assertThat(list.size()).isEqualTo(3);
        assertThat(list).containsExactly("one - 1", "two - 2", "three - 3");
    }

    @Test
    public void should_zip_two_streams_together_when_the_first_is_too_long() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three", "four");
        Stream<Integer> ints = Stream.of(1, 2, 3);
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;

        // When
        Stream<String> zippedStream = StreamsUtils.zip(strings, ints, zip);
        List<String> list = zippedStream.collect(toList());

        // Then
        assertThat(list.size()).isEqualTo(3);
        assertThat(list).containsExactly("one - 1", "two - 2", "three - 3");
    }

    @Test
    public void should_zip_two_streams_together_when_the_second_is_too_long() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three");
        Stream<Integer> ints = Stream.of(1, 2, 3, 4);
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;

        // When
        Stream<String> zippedStream = StreamsUtils.zip(strings, ints, zip);
        List<String> list = zippedStream.collect(toList());

        // Then
        assertThat(list.size()).isEqualTo(3);
        assertThat(list).containsExactly("one - 1", "two - 2", "three - 3");
    }

    @Test
    public void should_zip_two_streams_together_when_the_first_is_infinite() {
        // Given
        Stream<Integer> ints = IntStream.iterate(1, i -> i + 1).boxed();
        Stream<String> strings = Stream.of("one", "two", "three", "four");
        BiFunction<Integer, String, String> zip = (i, s) -> i + " - " + s;

        // When
        Stream<String> zippedStream = StreamsUtils.zip(ints, strings, zip);
        List<String> list = zippedStream.collect(toList());

        // Then
        assertThat(list.size()).isEqualTo(4);
        assertThat(list).containsExactly("1 - one", "2 - two", "3 - three", "4 - four");
    }

    @Test
    public void should_zip_two_streams_together_when_the_second_is_infinite() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three");
        Stream<Integer> ints = IntStream.iterate(1, i -> i + 1).boxed();
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;

        // When
        Stream<String> zippedStream = StreamsUtils.zip(strings, ints, zip);
        List<String> list = zippedStream.collect(toList());

        // Then
        assertThat(list.size()).isEqualTo(3);
        assertThat(list).containsExactly("one - 1", "two - 2", "three - 3");
    }

    @Test
    public void should_zip_a_sorted_stream_correctly_and_in_an_unsorted_stream() {
        // Given
        Stream<String> sortedStream1 = new TreeSet<>(Arrays.asList("one", "two", "three")).stream();
        Stream<String> sortedStream2 = new TreeSet<>(Arrays.asList("one", "two", "three")).stream();
        BiFunction<String, String, String> zip = (s1, s2) -> s1 + " - " + s2;

        // When
        Stream<String> zippedStream = StreamsUtils.zip(sortedStream1, sortedStream2, zip);

        // Then
        assertThat(zippedStream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three");
        Stream<Integer> ints = Stream.of(1, 2, 3);
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;

        Stream<String> zippedStream = StreamsUtils.zip(strings, ints, zip);
        TryAdvanceCheckingSpliterator<String> spliterator = new TryAdvanceCheckingSpliterator<>(zippedStream.spliterator());
        Stream<String> monitoredStream = StreamSupport.stream(spliterator, false);

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    public void should_correctly_call_the_onClose_callbacks_of_the_underlying_streams() {
        // Given
        AtomicInteger count = new AtomicInteger(0);
        Stream<String> strings = Stream.of("one", "two", "three").onClose(count::incrementAndGet);
        Stream<Integer> ints = Stream.of(1, 2, 3).onClose(count::incrementAndGet);
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;

        // When
        StreamsUtils.zip(strings, ints, zip).close();

        // Then
        assertThat(count.get()).isEqualTo(2);
    }

    @Test
    public void should_correctly_count_the_elements_of_a_sized_stream_with_same_length() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three", "four");
        Stream<Integer> ints = Stream.of(1, 2, 3, 4);
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;
        Stream<String> stream = StreamsUtils.zip(strings, ints, zip);

        // When
        long count = stream.count();

        // Then
        assertThat(count).isEqualTo(4L);
    }

    @Test
    public void should_correctly_count_the_elements_of_a_sized_stream_with_different_length() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three", "four");
        Stream<Integer> ints = Stream.of(1, 2, 3, 4, 5, 6);
        BiFunction<String, Integer, String> zip = (s, i) -> s + " - " + i;
        Stream<String> stream = StreamsUtils.zip(strings, ints, zip);

        // When
        long count = stream.count();

        // Then
        assertThat(count).isEqualTo(4L);
    }
}