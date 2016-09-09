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

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by José
 */
public class RollingSpliteratorTest {

    @Test
    public void should_roll_an_empty_stream_into_a_stream_of_an_empty_stream() {
        // Given
        // a trick to create an empty ORDERED stream
        Stream<String> strings = Stream.of("one").filter(s -> s.isEmpty());
        int groupingFactor = 2;

        // When
        RollingSpliterator<String> rollingSpliterator = RollingSpliterator.of(strings.spliterator(), groupingFactor);
        long numberOfRolledStreams = StreamSupport.stream(rollingSpliterator, false).count();

        // Then
        assertThat(numberOfRolledStreams).isEqualTo(1);
    }

    @Test
    public void should_roll_a_non_empty_stream_with_correct_substreams_content() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 3;

        // When
        RollingSpliterator<String> rollingSpliterator = RollingSpliterator.of(strings.spliterator(), groupingFactor);
        Stream<Stream<String>> stream = StreamSupport.stream(rollingSpliterator, false);
        List<List<String>> collect = stream.map(st -> st.collect(Collectors.toList())).collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(5);
        assertThat(collect.get(0)).isEqualTo(Arrays.asList("1", "2", "3"));
        assertThat(collect.get(1)).isEqualTo(Arrays.asList("2", "3", "4"));
        assertThat(collect.get(2)).isEqualTo(Arrays.asList("3", "4", "5"));
        assertThat(collect.get(3)).isEqualTo(Arrays.asList("4", "5", "6"));
        assertThat(collect.get(4)).isEqualTo(Arrays.asList("5", "6", "7"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_rolling_spliterator_on_a_null_spliterator() {

        RollingSpliterator<String> rollingSpliterator = RollingSpliterator.of(null, 3);
    }

    @Test(expectedExceptions = IllegalArgumentException .class)
    public void should_not_build_a_rolling_spliterator_with_a_grouping_factor_of_1() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 1;

        // When
        RollingSpliterator<String> rollingSpliterator = RollingSpliterator.of(strings.spliterator(), groupingFactor);
    }
}