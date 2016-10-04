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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by José
 */
public class ShiftingWindowCollectTest {

    @Test
    public void should_collect_an_empty_stream_into_a_stream_of__an_empty_stream() {
        // Given
        // a trick to create an empty ORDERED stream
        Stream<String> strings = Stream.of("one").filter(s -> s.isEmpty());
        int groupingFactor = 3;

        // When
        RollingSpliterator<String> rollingSpliterator = RollingSpliterator.of(strings.spliterator(), groupingFactor);
        long numberOfRolledStreams = StreamSupport.stream(rollingSpliterator, false).count();

        // Then
        assertThat(numberOfRolledStreams).isEqualTo(1);
    }

    @Test
    public void should_collect_a_non_empty_stream_with_correct_substreams_content() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 3;

        // When
        Stream<String> collectedStream = StreamsUtils.shiftingWindowCollect(strings, groupingFactor, joining());
        List<String> result = collectedStream.collect(toList());

        // When
        assertThat(result.size()).isEqualTo(5);
        assertThat(result.get(0)).isEqualTo("123");
        assertThat(result.get(1)).isEqualTo("234");
        assertThat(result.get(2)).isEqualTo("345");
        assertThat(result.get(3)).isEqualTo("456");
        assertThat(result.get(4)).isEqualTo("567");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_rolling_spliterator_on_a_null_spliterator() {

        StreamsUtils.shiftingWindowCollect(null, 3, joining());
    }

    @Test(expectedExceptions = IllegalArgumentException .class)
    public void should_not_build_a_rolling_spliterator_with_a_grouping_factor_of_1() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 1;

        // When
        StreamsUtils.shiftingWindowCollect(strings, groupingFactor, joining());
    }
}
