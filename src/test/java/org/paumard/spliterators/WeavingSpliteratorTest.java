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
import java.util.stream.StreamSupport;

import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by José
 */
public class WeavingSpliteratorTest {

    @Test
    public void should_weave_empty_streams_into_a_stream_of_an_empty_stream() {
        // Given
        // a trick to create an empty ORDERED stream
        Stream<String> strings1 = Stream.of("one").filter(s -> s.isEmpty());
        Stream<String> strings2 = Stream.of("one").filter(s -> s.isEmpty());

        // When
        Stream<String> weavingStream = StreamsUtils.weave(strings1, strings2);

        // Then
        assertThat(weavingStream.count()).isEqualTo(0);
    }

    @Test
    public void should_weave_a_non_empty_stream_with_correct_substreams_content() {
        // Given
        Stream<String> strings1 = Stream.of( "1",  "2",  "3",  "4");
        Stream<String> strings2 = Stream.of("11", "12", "13", "14");

        // When
        Stream<String> weavingStream = StreamsUtils.weave(strings1, strings2);
        List<String> collect = weavingStream.collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(8);
        assertThat(collect).isEqualTo(Arrays.asList("1", "11", "2", "12", "3", "13", "4", "14"));
    }

    @Test
    public void should_weave_a_non_empty_stream_with_correct_substreams_content_of_different_sizes() {
        // Given
        Stream<String> strings1 = Stream.of( "1",  "2",  "3",  "4");
        Stream<String> strings2 = Stream.of("11", "12", "13", "14", "15");

        // When
        Stream<String> weavingStream = StreamsUtils.weave(strings1, strings2);
        List<String> collect = weavingStream.collect(Collectors.toList());

        // When
        assertThat(collect.size()).isEqualTo(8);
        assertThat(collect).isEqualTo(Arrays.asList("1", "11", "2", "12", "3", "13", "4", "14"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void should_not_build_a_weaving_spliterator_on_null() {

        Stream<String> weavingStream = StreamsUtils.weave(null);
    }

    @Test(expectedExceptions = IllegalArgumentException .class)
    public void should_not_build_a_weaving_spliterator_on_less_than_two_spliterators() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7");
        int groupingFactor = 1;

        // When
        Stream<String> weavingStream = StreamsUtils.weave(strings);
    }
}