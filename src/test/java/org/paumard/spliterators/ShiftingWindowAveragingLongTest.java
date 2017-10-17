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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by José
 */
public class ShiftingWindowAveragingLongTest {

    @Test
    public void should_average_an_empty_stream_into_a_stream_of_an_empty_stream() {
        // Given
        // a trick to create an empty ORDERED stream
        Stream<Long> longs = Stream.of(1L, 2L, 3L).filter(l -> l == 0);
        int groupingFactor = 3;

        // When
        DoubleStream stream = StreamsUtils.shiftingWindowAveragingLong(longs, groupingFactor, Long::valueOf);
        long numberOfRolledStreams = stream.count();

        // Then
        assertThat(numberOfRolledStreams).isEqualTo(1);
    }

    @Test
    public void should_average_a_non_empty_stream_with_correct_substreams_content() {
        // Given
        Stream<String> strings = Stream.of("2", "4", "2", "4", "2", "4", "2");
        int groupingFactor = 2;

        // When
        DoubleStream averagedStream = StreamsUtils.shiftingWindowAveragingLong(strings, groupingFactor, Long::parseLong);
        List<Double> result = averagedStream.boxed().collect(toList());

        // When
        assertThat(result.size()).isEqualTo(6);
        assertThat(result.get(0)).isEqualTo(3);
        assertThat(result.get(1)).isEqualTo(3);
        assertThat(result.get(2)).isEqualTo(3);
        assertThat(result.get(3)).isEqualTo(3);
        assertThat(result.get(4)).isEqualTo(3);
        assertThat(result.get(5)).isEqualTo(3);
    }

    @Test
    public void should_process_a_sorted_stream_correctly_and_in_an_unsorted_stream() {
        // Given
        SortedSet<String> sortedSet = new TreeSet<>(Arrays.asList("2", "4", "2", "4", "2", "4", "2"));
        int groupingFactor = 2;

        // When
        DoubleStream stream = StreamsUtils.shiftingWindowAveragingLong(sortedSet.stream(), groupingFactor, Long::parseLong);

        // Then
        assertThat(stream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("2", "4", "2", "4", "2", "4", "2");
        int groupingFactor = 3;

        DoubleStream testedStream = StreamsUtils.shiftingWindowAveragingLong(strings, groupingFactor, Long::parseLong);
        TryAdvanceCheckingSpliterator<Double> spliterator = new TryAdvanceCheckingSpliterator<>(testedStream.spliterator());
        Stream<Double> monitoredStream = StreamSupport.stream(spliterator, false);

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    public void should_correctly_call_the_onClose_callbacks_of_the_underlying_streams() {
        // Given
        AtomicBoolean b = new AtomicBoolean(false);
        Stream<String> strings = Stream.of("2", "4", "2", "4", "2", "4", "2").onClose(() -> b.set(true));
        int groupingFactor = 3;

        // When
        StreamsUtils.shiftingWindowAveragingLong(strings, groupingFactor, Long::parseLong).close();

        // Then
        assertThat(b.get()).isEqualTo(true);
    }

    @Test
    public void should_correctly_count_the_elements_of_a_sized_stream() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 3;
        DoubleStream stream = StreamsUtils.shiftingWindowAveragingLong(strings, groupingFactor, Long::parseLong);

        // When
        long count = stream.count();

        // Then
        assertThat(count).isEqualTo(5L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void should_not_build_a_shifting_stream_on_a_non_ordered_stream() {
        // Given
        Stream<String> strings = Set.of("1", "2", "3", "4", "5", "6", "7").stream();

        // When
        StreamsUtils.shiftingWindowAveragingLong(strings, 3, Long::parseLong);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_shifting_stream_on_a_null_stream() {

        StreamsUtils.<String>shiftingWindowAveragingLong(null, 3, Long::parseLong);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void should_not_build_a_shifting_stream_with_a_grouping_factor_of_1() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 1;

        // When
        StreamsUtils.shiftingWindowAveragingLong(strings, groupingFactor, Long::parseLong);
    }
}
