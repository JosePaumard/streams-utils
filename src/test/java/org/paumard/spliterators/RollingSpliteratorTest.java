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

/**
 * Created by José
 */
public class RollingSpliteratorTest {

    @Test
    public void should_roll_an_empty_stream_into_a_stream_of_an_empty_stream() {
        // Given
        // a trick to create an empty ORDERED stream
        Stream<String> strings = Stream.of("one").filter(String::isEmpty);
        int groupingFactor = 2;

        // When
        Stream<Stream<String>> stream = StreamsUtils.roll(strings, groupingFactor);
        long numberOfRolledStreams = stream.count();

        // Then
        assertThat(numberOfRolledStreams).isEqualTo(1);
    }

    @Test
    public void should_roll_a_non_empty_stream_with_correct_substreams_content() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 3;

        // When
        Stream<Stream<String>> stream = StreamsUtils.roll(strings, groupingFactor);
        List<List<String>> collect = stream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(5);
        assertThat(collect.get(0)).containsExactly("1", "2", "3");
        assertThat(collect.get(1)).containsExactly("2", "3", "4");
        assertThat(collect.get(2)).containsExactly("3", "4", "5");
        assertThat(collect.get(3)).containsExactly("4", "5", "6");
        assertThat(collect.get(4)).containsExactly("5", "6", "7");
    }

    @Test
    public void should_be_able_to_roll_a_sorted_stream_in_an_non_sorted_rolled_stream() {
        // Given
        SortedMap<Long, String> sortedMap = new TreeMap<>();
        sortedMap.put(1L, "ONE");
        sortedMap.put(2L, "TWO");
        sortedMap.put(3L, "THREE");

        // When
        Stream<Stream<Map.Entry<Long, String>>> stream = StreamsUtils.roll(sortedMap.entrySet().stream(), 2);

        // Then
        assertThat(stream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 2;

        Stream<Stream<String>> testedStream = StreamsUtils.roll(strings, groupingFactor);
        TryAdvanceCheckingSpliterator<Stream<String>> spliterator = new TryAdvanceCheckingSpliterator<>(testedStream.spliterator());
        Stream<String> monitoredStream = StreamSupport.stream(spliterator, false).flatMap(Function.identity());

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(12L);
    }

    @Test
    public void should_not_build_a_rolling_spliterator_on_a_null_spliterator() {
        // Given
        Spliterator<Object> spliterator = null;
        int grouping = 3;

        // When
        Throwable throwable = catchThrowable(() -> RollingSpliterator.of(spliterator, grouping));

        // Then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_not_build_a_rolling_spliterator_with_a_grouping_factor_of_1() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 1;

        // When
        Throwable throwable = catchThrowable(() -> RollingSpliterator.of(strings.spliterator(), groupingFactor));

        // Then
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }
}