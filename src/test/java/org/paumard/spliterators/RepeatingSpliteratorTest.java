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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by José
 */
public class RepeatingSpliteratorTest {

    @Test
    public void should_return_an_empty_stream_when_called_with_an_empty_stream() {
        // Given
        Stream<String> stream = Stream.empty();
        int repeating = 2;

        // When
        Stream<String> repeatingStream = StreamsUtils.repeat(stream, repeating);

        // Then
        assertThat(repeatingStream.count()).isEqualTo(0L);
    }

    @Test
    public void should_return_a_repeating_stream_when_called_on_a_non_empty_stream() {
        // Given
        Stream<String> stream = Stream.of("a", "b", "c");
        int repeating = 2;

        // When
        Stream<String> repeatingStream = StreamsUtils.repeat(stream, repeating);
        List<String> result = repeatingStream.collect(Collectors.toList());

        // Then
        assertThat(result).containsExactly("a", "a", "b", "b", "c", "c");
    }

    @Test
    public void should_return_a_repeating_stream_when_called_on_a_non_empty_stream_3x() {
        // Given
        Stream<String> stream = Stream.of("a", "b", "c");
        int repeating = 3;

        // When
        Stream<String> repeatingStream = StreamsUtils.repeat(stream, repeating);
        List<String> result = repeatingStream.collect(Collectors.toList());

        // Then
        assertThat(result).containsExactly("a", "a", "a", "b", "b", "b", "c", "c", "c");
    }

    @Test
    public void should_repeat_a_sorted_stream_correctly_and_in_an_unsorted_stream() {
        // Given
        SortedSet<String> sortedSet = new TreeSet<>(Arrays.asList("a", "b", "c"));
        int repeating = 2;

        // When
        Stream<String> stream = StreamsUtils.repeat(sortedSet.stream(), repeating);

        // Then
        assertThat(stream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three");
        Stream<String> repeatingStream = StreamsUtils.repeat(strings, 2);
        TryAdvanceCheckingSpliterator<String> spliterator = new TryAdvanceCheckingSpliterator<>(repeatingStream.spliterator());
        Stream<String> monitoredStream = StreamSupport.stream(spliterator, false);

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(6L);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_repeating_spliterator_on_a_null_spliterator() {

        Stream<String> repeatingStream = StreamsUtils.repeat(null, 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void should_not_build_a_repeating_spliterator_with_a_repeating_factor_of_1() {
        // Given
        Stream<String> stream = Stream.of("a1", "a2");

        // When
        Stream<String> repeatingStream = StreamsUtils.repeat(stream, 1);

    }
}