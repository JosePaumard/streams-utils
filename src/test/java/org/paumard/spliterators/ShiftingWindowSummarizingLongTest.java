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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by José
 */
public class ShiftingWindowSummarizingLongTest {

    @Test
    public void should_summarize_an_empty_stream_into_a_stream_of_an_empty_stream() {
        // Given
        // a trick to create an empty ORDERED stream
        Stream<Long> longs = Stream.of(1L, 2L, 3L).filter(l -> l == 0);
        int groupingFactor = 3;

        // When
        Stream<LongSummaryStatistics> stream = StreamsUtils.shiftingWindowSummarizingLong(longs, groupingFactor, Long::valueOf);
        long numberOfRolledStreams = stream.count();

        // Then
        assertThat(numberOfRolledStreams).isEqualTo(1);
    }

    @Test
    public void should_summarize_a_non_empty_stream_with_correct_substreams_content() {
        // Given
        Stream<String> strings = Stream.of("2", "4", "2", "4", "2", "4", "2");
        int groupingFactor = 2;
        LongSummaryStatistics stats = new LongSummaryStatistics();
        stats.accept(2L);
        stats.accept(4L);

        // When
        Stream<LongSummaryStatistics> summarizedStream = StreamsUtils.shiftingWindowSummarizingLong(strings, groupingFactor, Long::parseLong);
        List<LongSummaryStatistics> result = summarizedStream.collect(toList());

        // When
        assertThat(result.size()).isEqualTo(6);
        assertThat(result.get(0).toString()).isEqualTo(stats.toString());
        assertThat(result.get(1).toString()).isEqualTo(stats.toString());
        assertThat(result.get(2).toString()).isEqualTo(stats.toString());
        assertThat(result.get(3).toString()).isEqualTo(stats.toString());
        assertThat(result.get(4).toString()).isEqualTo(stats.toString());
        assertThat(result.get(5).toString()).isEqualTo(stats.toString());
    }

    @Test
    public void should_process_a_sorted_stream_correctly_and_in_an_unsorted_stream() {
        // Given
        SortedSet<String> sortedSet = new TreeSet<>(Arrays.asList("2", "4", "2", "4", "2", "4", "2"));
        int groupingFactor = 2;

        // When
        Stream<LongSummaryStatistics> stream = StreamsUtils.shiftingWindowSummarizingLong(sortedSet.stream(), groupingFactor, Long::parseLong);

        // Then
        assertThat(stream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("2", "4", "2", "4", "2", "4", "2");
        int groupingFactor = 3;

        Stream<LongSummaryStatistics> testedStream = StreamsUtils.shiftingWindowSummarizingLong(strings, groupingFactor, Long::parseLong);
        TryAdvanceCheckingSpliterator<LongSummaryStatistics> spliterator = new TryAdvanceCheckingSpliterator<>(testedStream.spliterator());
        Stream<LongSummaryStatistics> monitoredStream = StreamSupport.stream(spliterator, false);

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(5L);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_shifting_stream_on_a_null_stream() {

        StreamsUtils.<String>shiftingWindowSummarizingLong(null, 3, Long::parseLong);
    }

    @Test(expectedExceptions = IllegalArgumentException .class)
    public void should_not_build_a_shifting_stream_with_a_grouping_factor_of_1() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 1;

        // When
        StreamsUtils.shiftingWindowSummarizingLong(strings, groupingFactor, Long::parseLong);
    }
}
