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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class GroupingSpliteratorTest {

    @Test
    public void should_group_an_empty_stream_into_a_stream_of_an_empty_stream() {
        // Given
        // a trick to create an empty ORDERED stream
        Stream<String> strings = Stream.of("one").filter(s -> s.isEmpty());
        int groupingFactor = 2;

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, groupingFactor);
        long numberOfGroupedSteams = groupingStream.count();

        // Then
        assertThat(numberOfGroupedSteams).isEqualTo(1);
    }

    @Test
    public void should_group_a_non_empty_stream_with_correct_substreams_content() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7", "8", "9");
        int groupingFactor = 3;

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, groupingFactor);
        List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(3);
        assertThat(collect.get(0)).containsExactly("1", "2", "3");
        assertThat(collect.get(1)).containsExactly("4", "5", "6");
        assertThat(collect.get(2)).containsExactly("7", "8", "9");
    }

    @Test
    public void should_group_a_non_empty_stream_with_correct_substreams_content_even_if_last_stream_is_incomplete() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 3;

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, groupingFactor);
        List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(3);
        assertThat(collect.get(0)).containsExactly("1", "2", "3");
        assertThat(collect.get(1)).containsExactly("4", "5", "6");
        assertThat(collect.get(2)).containsExactly("7");
    }

    @Test
    public void should_group_a_sorted_stream_correctly_and_in_an_unsorted_stream() {
        // Given
        SortedSet<String> sortedSet = new TreeSet<>(Arrays.asList("o", "1", "2", "3", "4", "5", "6", "7", "8", "9", "c"));
        int groupingFactor = 3;

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(sortedSet.stream(), groupingFactor);

        // Then
        assertThat(groupingStream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 3;

        Stream<Stream<String>> testedStream = StreamsUtils.group(strings, groupingFactor);
        TryAdvanceCheckingSpliterator<Stream<String>> spliterator = new TryAdvanceCheckingSpliterator<>(testedStream.spliterator());
        Stream<String> monitoredStream = StreamSupport.stream(spliterator, false).flatMap(Function.identity());

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(7L);
    }

    @Test
    public void should_not_build_a_grouping_spliterator_on_a_null_spliterator() {
        // Given
        Stream<String> strings = null;
        int groupingFactor = 3;

        // When
        Throwable throwable = catchThrowable(() -> StreamsUtils.group(strings, groupingFactor));

        // Then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_not_build_a_grouping_spliterator_with_a_grouping_factor_of_1() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 1;

        // When
        Throwable throwable = catchThrowable(() -> StreamsUtils.group(strings, groupingFactor));

        // Then
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }
}