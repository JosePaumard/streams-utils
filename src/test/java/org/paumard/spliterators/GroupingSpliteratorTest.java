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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_grouping_spliterator_on_a_null_spliterator() {

        Stream<Stream<String>> groupingStream = StreamsUtils.group(null, 3);
    }

    @Test(expectedExceptions = IllegalArgumentException .class)
    public void should_not_build_a_grouping_spliterator_with_a_grouping_factor_of_1() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 1;

        // When
        Stream<Stream<String>> groupingStream = StreamsUtils.group(strings, 1);
    }
}