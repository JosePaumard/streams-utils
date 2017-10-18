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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class GroupingOnSplittingSpliteratorTest {

    @Test
    public void should_group_an_empty_stream_into_an_empty_stream() {
        // Given
        // a trick to create an empty ORDERED stream
        Stream<String> strings = Stream.of("one").filter(String::isEmpty);
        Predicate<String> splitter = s -> s.startsWith("o");

        // When
        Stream<Stream<String>> groupingOnSplittingStream = StreamsUtils.group(strings, splitter, true);
        long numberOfGroupedSteams = groupingOnSplittingStream.count();

        // Then
        assertThat(numberOfGroupedSteams).isEqualTo(0);
    }

    @Test
    public void should_group_an_stream_without_splitter_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7", "8", "9");
        Predicate<String> splitter = s -> s.startsWith("o");

        // When
        Stream<Stream<String>> groupingOnSplittingStream = StreamsUtils.group(strings, splitter);
        long numberOfGroupedSteams = groupingOnSplittingStream.count();

        // Then
        assertThat(numberOfGroupedSteams).isEqualTo(0);
    }

    @Test
    public void should_group_a_non_empty_stream_with_correct_substreams_content() {
        // Given
        Stream<String> strings = Stream.of("1", "o", "2", "3", "4", "5", "6", "o", "7", "8", "o", "9");
        Predicate<String> splitter = s -> s.startsWith("o");

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, splitter);
        List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(3);
        assertThat(collect.get(0)).containsExactly("o", "2", "3", "4", "5", "6");
        assertThat(collect.get(1)).containsExactly("o", "7", "8");
        assertThat(collect.get(2)).containsExactly("o", "9");
    }

    @Test
    public void should_group_a_non_empty_stream_with_correct_substreams_content_even_if_more_than_one_opener() {
        // Given
        Stream<String> strings = Stream.of("1", "o", "o", "2", "3", "o", "4", "5", "6", "o", "7", "8", "9");
        Predicate<String> splitter = s -> s.startsWith("o");

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, splitter);
        List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(4);
        assertThat(collect.get(0)).containsExactly("o");
        assertThat(collect.get(1)).containsExactly("o", "2", "3");
        assertThat(collect.get(2)).containsExactly("o", "4", "5", "6");
        assertThat(collect.get(3)).containsExactly("o", "7", "8", "9");
    }

    @Test
    public void should_group_a_non_empty_stream_with_correct_substreams_content_with_splitter_at_first_position() {
        // Given
        Stream<String> strings = Stream.of("o", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        Predicate<String> splitter = s -> s.startsWith("o");

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, splitter);
        List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(1);
        assertThat(collect.get(0)).containsExactly("o", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    }

    @Test
    public void should_group_a_sorted_stream_correctly_and_in_an_unsorted_stream() {
        // Given
        SortedSet<String> sortedSet = new TreeSet<>(Arrays.asList("o", "1", "2", "3", "4", "5", "6", "7", "8", "9", "c"));
        Predicate<String> splitter = s -> s.startsWith("o");

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(sortedSet.stream(), splitter);

        // Then
        assertThat(groupingStream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("o", "1", "2", "3", "4", "5", "6", "7", "8", "9", "c");
        Predicate<String> splitter = s -> s.startsWith("o");

        Stream<Stream<String>> testedStream = StreamsUtils.group(strings, splitter);
        TryAdvanceCheckingSpliterator<Stream<String>> spliterator = new TryAdvanceCheckingSpliterator<>(testedStream.spliterator());
        Stream<String> monitoredStream = StreamSupport.stream(spliterator, false).flatMap(Function.identity());

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(11L);
    }

    @Test
    public void should_not_build_a_grouping_spliterator_on_a_null_spliterator() {
        // Given
        Stream<String> strings = null;
        Predicate<String> splitter = s -> s.startsWith("o");

        // When
        Throwable throwable = catchThrowable(() -> StreamsUtils.group(strings, splitter));

        // Then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_not_build_a_grouping_spliterator_on_a_null_opening_predicate() {
        // Given
        Stream<String> strings = Stream.of("one").filter(String::isEmpty);
        Predicate<? super String> splitter = null;

        // When
        Throwable throwable = catchThrowable(() -> StreamsUtils.group(strings, splitter));

        // Then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }
}