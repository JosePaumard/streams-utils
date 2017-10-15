/*
 * Copyright (C) 2017 José Paumard
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

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by José
 */
public class AccumulatingSpliteratorTest {

    @Test
    public void should_accumulate_an_empty_stream_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.of("0", "0");
        strings = strings.filter(s -> s.length() > 10); // trick to create an empty ordered stream

        // When
        Stream<String> accumulate = StreamsUtils.accumulate(strings, String::concat);

        // Then
        assertThat(accumulate.count()).isEqualTo(0L);
    }

    @Test
    public void should_accumulate_a_singleton_stream_into_the_same_stream() {
        // Given
        Stream<String> strings = Stream.of("one");

        // When
        Stream<String> accumulate = StreamsUtils.accumulate(strings, String::concat);

        // Then
        assertThat(accumulate.collect(toList())).containsExactly("one");
    }

    @Test
    public void should_accumulate_a_stream_into_the_correct_stream() {
        // Given
        Stream<Integer> integers = Stream.of(1, 1, 1, 1, 1);

        // When
        Stream<Integer> accumulate = StreamsUtils.accumulate(integers, Integer::sum);

        // Then
        assertThat(accumulate.collect(toList())).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    public void should_conform_to_specified_trySplit_behavior() {
        // Given
        Stream<String> strings = Stream.of("one", "two", "three");
        Stream<String> accumulatingStream = StreamsUtils.accumulate(strings, String::concat);
        TryAdvanceCheckingSpliterator<String> spliterator = new TryAdvanceCheckingSpliterator<>(accumulatingStream.spliterator());
        Stream<String> monitoredStream = StreamSupport.stream(spliterator, false);

        // When
        long count = monitoredStream.count();

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    public void should_correctly_count_the_elements_of_a_sized_stream() {
        // Given
        Stream<Integer> integers = Stream.of(1, 1, 1, 1, 1);
        Stream<Integer> accumulate = StreamsUtils.accumulate(integers, Integer::sum);

        // When
        long count = accumulate.count();

        // Then
        assertThat(count).isEqualTo(5);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void should_not_build_an_accumulate_stream_on_a_non_ordered_stream() {

        // Given
        Map<Integer, String> map = Map.of(1, "1", 2, "2");
        Stream<Integer> accumulate = map.keySet().stream();

        // When
        Stream<Integer> stream = StreamsUtils.accumulate(accumulate, Integer::sum);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_an_accumulate_stream_on_a_null_stream() {

        StreamsUtils.accumulate(null, Integer::sum);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_an_accumulate_stream_on_a_null_operator() {

        StreamsUtils.accumulate(Stream.of(1, 1, 1, 1, 1), null);
    }
}