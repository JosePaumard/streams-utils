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
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupingOnGatingClosedOpenedSpliteratorTest {

    @Test
    public void should_group_an_empty_stream_into_an_empty_stream() {
        // Given
        // a trick to create an empty ORDERED stream
        Stream<String> strings = Stream.of("one").filter(s -> s.isEmpty());
        Predicate<String> open = s -> s.startsWith("o");
        Predicate<String> close = s -> s.startsWith("c");

        // When
        Stream<Stream<String>> groupingOnGatingStream = StreamsUtils.group(strings, open, true, close, false);
        long numberOfGroupedSteams = groupingOnGatingStream.count();

        // Then
        assertThat(numberOfGroupedSteams).isEqualTo(0);
    }

    @Test
    public void should_group_a_non_empty_stream_with_correct_substreams_content() {
        // Given
        Stream<String> strings = Stream.of("1", "o", "2", "3", "c", "4", "5", "6", "o", "7", "8", "c", "9");
        Predicate<String> open = s -> s.startsWith("o");
        Predicate<String> close = s -> s.startsWith("c");

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, open, true, close, false);
        List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(2);
        assertThat(collect.get(0)).containsExactly("o", "2", "3");
        assertThat(collect.get(1)).containsExactly("o", "7", "8");
    }

    @Test
    public void should_group_a_non_empty_stream_with_correct_substreams_content_even_if_last_stream_is_incomplete() {
        // Given
        Stream<String> strings = Stream.of("1", "o", "2", "3", "c", "4", "5", "6", "o", "7", "8", "c", "9", "o");
        Predicate<String> open = s -> s.startsWith("o");
        Predicate<String> close = s -> s.startsWith("c");

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, open, true, close, false);
        List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(3);
        assertThat(collect.get(0)).containsExactly("o", "2", "3");
        assertThat(collect.get(1)).containsExactly("o", "7", "8");
        assertThat(collect.get(2)).containsExactly("o");
    }

    @Test
    public void should_group_a_non_empty_stream_with_correct_substreams_content_even_if_more_than_one_opener() {
        // Given
        Stream<String> strings = Stream.of("1", "o", "o", "2", "3", "c", "4", "5", "6", "o", "7", "8", "c", "9");
        Predicate<String> open = s -> s.startsWith("o");
        Predicate<String> close = s -> s.startsWith("c");

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, open, true, close, false);
        List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(2);
        assertThat(collect.get(0)).containsExactly("o", "o", "2", "3");
        assertThat(collect.get(1)).containsExactly("o", "7", "8");
    }

    @Test
    public void should_group_a_non_empty_stream_with_correct_substreams_content_even_if_more_than_one_closer() {
        // Given
        Stream<String> strings = Stream.of("1", "o", "2", "3", "c", "4", "c", "5", "6", "o", "7", "8", "c", "9");
        Predicate<String> open = s -> s.startsWith("o");
        Predicate<String> close = s -> s.startsWith("c");

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, open, true, close, false);
        List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(2);
        assertThat(collect.get(0)).containsExactly("o", "2", "3");
        assertThat(collect.get(1)).containsExactly("o", "7", "8");
    }

    @Test
    public void should_group_a_non_empty_stream_with_correct_substreams_content_with_opener_and_closer_at_first_and_last_position() {
        // Given
        Stream<String> strings = Stream.of("o", "1", "2", "3", "4", "5", "6", "7", "8", "9", "c");
        Predicate<String> open = s -> s.startsWith("o");
        Predicate<String> close = s -> s.startsWith("c");

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, open, true, close, false);
        List<List<String>> collect = groupingStream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(1);
        assertThat(collect.get(0)).containsExactly("o", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    }
}