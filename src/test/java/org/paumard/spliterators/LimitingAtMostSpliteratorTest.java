/*
 * Copyright (C) 2016 José Paumard
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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by José
 */
public class LimitingAtMostSpliteratorTest {

    @Test
    public void should_limit_an_empty_streams_into_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.empty();
        long limit = 10L;

        // When
        Stream<String> limitedStream = StreamsUtils.limitAtMost(strings, limit);
        long count = limitedStream.count();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void should_limit_an_empty_streams_into_an_empty_stream_even_for_a_limit_of_0() {
        // Given
        Stream<String> strings = Stream.empty();
        long limit = 0L;

        // When
        Stream<String> limitedStream = StreamsUtils.limitAtMost(strings, limit);
        long count = limitedStream.count();

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void should_limit_a_longer_stream_correctly() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7", "8", "9");
        long limit = 5L;

        // When
        Stream<String> limitedStream = StreamsUtils.limitAtMost(strings, limit);
        List<String> list = limitedStream.collect(toList());

        // Then
        assertThat(list.size()).isEqualTo(5);
        assertThat(list).asList().containsExactly("1", "2", "3", "4", "5");
    }

    @Test
    public void should_limit_a_shorter_stream_correctly() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7", "8", "9");
        long limit = 50L;

        // When
        Stream<String> limitedStream = StreamsUtils.limitAtMost(strings, limit);
        List<String> list = limitedStream.collect(toList());

        // Then
        assertThat(list.size()).isEqualTo(9);
        assertThat(list).asList().containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9");
    }

    @Test
    public void should_0_limit_a_stream_in_an_empty_stream() {
        // Given
        Stream<String> strings = Stream.of("1", "2", "3", "4", "5", "6", "7", "8", "9");
        long limit = 0L;

        // When
        Stream<String> limitedStream = StreamsUtils.limitAtMost(strings, limit);
        List<String> list = limitedStream.collect(toList());

        // Then
        assertThat(list.size()).isEqualTo(0);
    }
}