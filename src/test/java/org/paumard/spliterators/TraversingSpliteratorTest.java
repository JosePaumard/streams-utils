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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by José
 */
public class TraversingSpliteratorTest {


    @Test
    public void should_a_return_stream_of_empty_stream_if_provided_streams_are_empty() {
        // Given
        // a trick to create an empty ORDERED stream
        Stream<String> streamA = Stream.of("one").filter(String::isEmpty);
        Stream<String> streamB = Stream.of("one").filter(String::isEmpty);

        // When
        Stream<Stream<String>> traversingStream = StreamsUtils.traverse(streamA, streamB);

        // Then
        List<List<String>> collect =
        traversingStream.map(str -> str.collect(Collectors.toList()))
                        .collect(Collectors.toList());

        assertThat(collect.size()).isEqualTo(1);
        assertThat(collect.get(0)).isEmpty();
    }

    @Test
    public void should_traverse_two_streams_into_a_traversed_stream() {
        // Given
        Stream<String> streamA = Stream.of("a1", "a2", "a3");
        Stream<String> streamB = Stream.of("b1", "b2", "b3");

        // When
        Stream<Stream<String>> traversingStream = StreamsUtils.traverse(streamA, streamB);

        // Then
        List<List<String>> strings =
                traversingStream.map(str -> str.collect(Collectors.toList()))
                                .collect(Collectors.toList());

        assertThat(strings.size()).isEqualTo(3);
        assertThat(strings.get(0)).containsSequence("a1", "b1");
        assertThat(strings.get(1)).containsSequence("a2", "b2");
        assertThat(strings.get(2)).containsSequence("a3", "b3");
    }

    @Test
    public void should_traverse_two_streams_and_skip_elements_if_a_stream_is_longer_than_the_other() {
        // Given
        Stream<String> streamA = Stream.of("a1", "a2");
        Stream<String> streamB = Stream.of("b1", "b2", "b3");

        // When
        Stream<Stream<String>> traversingStream = StreamsUtils.traverse(streamA, streamB);

        // Then
        List<List<String>> strings =
                traversingStream.map(str -> str.collect(Collectors.toList()))
                        .collect(Collectors.toList());

        assertThat(strings.size()).isEqualTo(2);
        assertThat(strings.get(0)).containsExactly("a1", "b1");
        assertThat(strings.get(1)).containsExactly("a2", "b2");
    }

    @Test
    public void should_two_sorted_streams_correctly_and_in_an_unsorted_stream() {
        // Given
        Stream<String> streamA = new TreeSet<>(Arrays.asList("a", "b", "c")).stream();
        Stream<String> streamB = new TreeSet<>(Arrays.asList("0", "1", "2")).stream();

        // When
        Stream<Stream<String>> stream = StreamsUtils.traverse(streamA, streamB);

        // Then
        assertThat(stream.spliterator().characteristics() & Spliterator.SORTED).isEqualTo(0);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_transversal_spliterator_on_a_null_spliterator() {

        Stream<Stream<String>> traversingStream = StreamsUtils.traverse(null);
    }

    @Test(expectedExceptions = IllegalArgumentException .class)
    public void should_not_build_a_transversal_spliterator_on_only_one_spliterator() {
        // Given
        Stream<String> streamA = Stream.of("a1", "a2");

        // When
        Stream<Stream<String>> traversingStream = StreamsUtils.traverse(streamA);
    }
}